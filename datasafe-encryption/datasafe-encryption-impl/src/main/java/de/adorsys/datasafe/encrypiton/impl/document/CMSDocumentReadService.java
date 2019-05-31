package de.adorsys.datasafe.encrypiton.impl.document;

import com.google.common.collect.ImmutableList;
import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.storage.api.actions.StorageReadService;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Read CMS-encrypted document location DFS.
 */
public class CMSDocumentReadService implements EncryptedDocumentReadService {

    private final StorageReadService readService;
    private final PrivateKeyService privateKeyService;
    private final CMSEncryptionService cms;

    @Inject
    public CMSDocumentReadService(StorageReadService readService, PrivateKeyService privateKeyService,
                                  CMSEncryptionService cms) {
        this.readService = readService;
        this.privateKeyService = privateKeyService;
        this.cms = cms;
    }

    @Override
    @SneakyThrows
    public InputStream read(ReadRequest<UserIDAuth, AbsoluteLocation<PrivateResource>> request) {
        InputStream dfsSource = readService.read(request.getLocation());

        InputStream encryptionSource = cms.buildDecryptionInputStream(
                dfsSource,
                keyId -> privateKeyService.keyById(request.getOwner(), keyId)
        );

        return new CloseCoordinatingStream(encryptionSource, ImmutableList.of(encryptionSource, dfsSource));
    }

    /**
     * This class fixes issue that bouncy castle does not close underlying stream - i.e. DFS stream
     * when wrapping it.
     */
    @RequiredArgsConstructor
    private static final class CloseCoordinatingStream extends InputStream {

        private final InputStream streamToRead;
        private final List<InputStream> streamsToClose;

        @Override
        public int read() throws IOException {
            return streamToRead.read();
        }

        @Override
        @SneakyThrows
        public void close() {
            super.close();
            streamsToClose.forEach(CloseCoordinatingStream::doClose);
        }

        @SneakyThrows
        private static void doClose(InputStream stream) {
            stream.close();
        }
    }
}
