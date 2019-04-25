package de.adorsys.datasafe.business.impl.document.cms;

import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.encryption.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.business.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.business.api.storage.StorageReadService;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.io.InputStream;

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
    public InputStream read(ReadRequest<UserIDAuth> request) {
        return cms.buildDecryptionInputStream(
                readService.read(request.getLocation()),
                privateKeyService.keystore(request.getOwner())
        );
    }
}
