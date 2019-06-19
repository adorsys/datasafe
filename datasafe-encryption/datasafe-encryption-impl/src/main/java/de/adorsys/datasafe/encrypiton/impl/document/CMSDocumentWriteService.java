package de.adorsys.datasafe.encrypiton.impl.document;

import com.google.common.collect.ImmutableList;
import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
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

        FanOutStream dfsSink = new FanOutStream(
                recipientsWithInbox.values().stream().map(writeService::write).collect(Collectors.toList())
        );

        OutputStream encryptionSink = cms.buildEncryptionOutputStream(
                dfsSink,
                recipientsWithInbox.keySet()
        );

        return new CloseCoordinatingStream(encryptionSink, ImmutableList.of(encryptionSink, dfsSink));
    }

    @Override
    public OutputStream write(AbsoluteLocation<PrivateResource> location, SecretKeyIDWithKey secretKey) {

        OutputStream dfsSink = writeService.write(location);

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
    private static final class FanOutStream extends OutputStream {

        private final List<OutputStream> destinations;

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
            for (OutputStream destination : destinations) {
                destination.close();
            }
        }
    }
}
