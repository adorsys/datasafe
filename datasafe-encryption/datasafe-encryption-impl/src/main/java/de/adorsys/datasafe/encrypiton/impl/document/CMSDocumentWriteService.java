package de.adorsys.datasafe.encrypiton.impl.document;

import com.google.common.collect.ImmutableList;
import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.WithCallback;
import de.adorsys.datasafe.types.api.utils.CustomizableByteArrayOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Writes CMS-encrypted document to DFS.
 */
@RuntimeDelegate
public class CMSDocumentWriteService implements EncryptedDocumentWriteService {

    private final StorageWriteService writeService;
    private final CMSEncryptionService cms;

    @Inject
    public CMSDocumentWriteService(StorageWriteService writeService,
                                   CMSEncryptionService cms) {
        this.writeService = writeService;
        this.cms = cms;
    }

    @Override
    public OutputStream write(Map<PublicKeyIDWithPublicKey, AbsoluteLocation> recipientsWithInbox) {

        int maxChunkSize = recipientsWithInbox.values().stream()
                .map(writeService::flushChunkSize)
                .filter(Optional::isPresent)
                .mapToInt(Optional::get)
                .max()
                .orElse(-1);

        List<OutputStream> recipients = recipientsWithInbox.values().stream()
                .map(it -> writeService.write(WithCallback.noCallback(it)))
                .collect(Collectors.toList());

        FanOutStream dfsSink = maxChunkSize > 0 ?
                new ChunkableFanOutStream(recipients, maxChunkSize) : new FanOutStream(recipients);

        OutputStream encryptionSink = cms.buildEncryptionOutputStream(
                dfsSink,
                recipientsWithInbox.keySet()
        );

        return new CloseCoordinatingStream(encryptionSink, ImmutableList.of(encryptionSink, dfsSink));
    }

    @Override
    public OutputStream write(
            WithCallback<AbsoluteLocation<PrivateResource>, ResourceWriteCallback> locationWithCallback,
            SecretKeyIDWithKey secretKey
    ) {

        OutputStream dfsSink = writeService.write(
                WithCallback.<AbsoluteLocation, ResourceWriteCallback>builder()
                        .wrapped(locationWithCallback.getWrapped())
                        .callbacks(locationWithCallback.getCallbacks())
                        .build()
        );

        OutputStream encryptionSink = cms.buildEncryptionOutputStream(
                dfsSink,
                secretKey.getSecretKey(),
                secretKey.getKeyID()
        );

        return new CloseCoordinatingStream(encryptionSink, ImmutableList.of(encryptionSink, dfsSink));
    }

    /**
     * This class fixes issue that bouncy castle does not close underlying stream - example: DFS stream
     * when wrapping it.
     */
    @RequiredArgsConstructor
    private static final class CloseCoordinatingStream extends OutputStream {

        private final OutputStream streamToWrite;
        private final List<OutputStream> streamsToClose;

        @Override
        public void write(int b) throws IOException {
            streamToWrite.write(b);
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            streamToWrite.write(bytes, off, len);
        }

        @Override
        @SneakyThrows
        public void close() {
            super.close();
            streamsToClose.forEach(CloseCoordinatingStream::doClose);
        }


        @SneakyThrows
        private static void doClose(OutputStream stream) {
            stream.close();
        }
    }

    /**
     * Emits each byte from source stream to multiple destinations (fan-out). Used to send each encrypted
     * byte to multiple recipients.
     */
    @RequiredArgsConstructor
    private static class FanOutStream extends OutputStream {

        protected final List<OutputStream> destinations;

        @Override
        public void write(int b) throws IOException {
            for (OutputStream destination : destinations) {
                destination.write(b);
            }
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            for (OutputStream destination : destinations) {
                destination.write(bytes, off, len);
            }
        }

        @Override
        @SneakyThrows
        public void close() {
            super.close();
            Iterator<OutputStream> dest = destinations.iterator();
            while (dest.hasNext()) {
                dest.next().close();
                dest.remove();
            }
        }
    }

    /**
     * Buffered fan-out stream, so that same data won't get replicated multiple times for chunked consumers.
     * Such consumers retain buffer that is equal to chunk size, in order to eliminate this extra buffer
     * this class can be used (assuming all-equal chunk size).
     */
    private static class ChunkableFanOutStream extends FanOutStream {

        private final int chunkSize;
        private final CustomizableByteArrayOutputStream os;

        private ChunkableFanOutStream(List<OutputStream> destinations, int chunkSize) {
            super(destinations);

            this.chunkSize = chunkSize;
            this.os = new CustomizableByteArrayOutputStream(32, Integer.MAX_VALUE - 1, 0.5);
        }

        @Override
        public void write(int b) throws IOException {
            if (!needsFlush(1)) {
                os.write(b);
                return;
            }

            os.write(b);
            doFlush();
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            if (!needsFlush(len)) {
                os.write(bytes, off, len);
                return;
            }

            os.write(bytes, off, len);
            doFlush();
        }

        @Override
        @SneakyThrows
        public void close() {
            if (os.size() == 0) {
                super.close();
                return;
            }

            // when closing stream immediately it is ok not to write in chunks - memory will
            // be retained only for 1 destination
            byte[] tailChunk = os.getBufferOrCopy();
            int size = os.size();

            Iterator<OutputStream> dest = destinations.iterator();
            while (dest.hasNext()) {
                OutputStream destination = dest.next();
                destination.write(tailChunk, 0, size);
                destination.close();
                dest.remove();
            }
        }

        private void doFlush() throws IOException {
            byte[] bytes = os.getBufferOrCopy();
            int size = os.size();

            // write only in chunks of declared size
            int chunksToWrite = size / chunkSize;
            int written = 0;
            for (int chunkNum = 0; chunkNum < chunksToWrite; chunkNum++) {
                super.write(bytes, written, chunkSize);
                written += chunkSize;
            }

            // retain tail bytes, non proportional to `chunkSize`:
            os.reset();
            if (written < size) {
                os.write(bytes, written, size - written);
            }
        }

        private boolean needsFlush(int addedBytes) {
            return os.size() + addedBytes > chunkSize;
        }
    }
}
