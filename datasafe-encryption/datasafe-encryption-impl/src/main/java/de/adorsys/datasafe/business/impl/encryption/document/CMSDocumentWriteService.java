package de.adorsys.datasafe.business.impl.encryption.document;

import com.google.common.collect.ImmutableList;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.business.api.profile.keys.PublicKeyService;
import de.adorsys.datasafe.business.api.storage.StorageWriteService;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteResourceLocation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Write CMS-encrypted document to DFS.
 */
public class CMSDocumentWriteService implements EncryptedDocumentWriteService {

    private final StorageWriteService writeService;
    private final PublicKeyService publicKeyService;
    private final CMSEncryptionService cms;

    @Inject
    public CMSDocumentWriteService(StorageWriteService writeService, PublicKeyService publicKeyService,
                                   CMSEncryptionService cms) {
        this.writeService = writeService;
        this.publicKeyService = publicKeyService;
        this.cms = cms;
    }

    @Override
    @SneakyThrows
    public OutputStream write(WriteRequest<UserID, AbsoluteResourceLocation<?>> request) {

        OutputStream dfsSink = writeService.write(request.getLocation());
        PublicKeyIDWithPublicKey withId = publicKeyService.publicKey(request.getOwner());

        OutputStream encryptionSink = cms.buildEncryptionOutputStream(
                dfsSink,
                withId.getPublicKey(),
                withId.getKeyID()
        );

        return new CloseCoordinatingStream(encryptionSink, ImmutableList.of(encryptionSink, dfsSink));
    }

    /**
     * This class fixes issue that bouncy castle does not close underlying stream - i.e. DFS stream
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
}
