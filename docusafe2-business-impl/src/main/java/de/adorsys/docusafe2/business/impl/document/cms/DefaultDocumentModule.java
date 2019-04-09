package de.adorsys.docusafe2.business.impl.document.cms;

import dagger.Module;
import dagger.Provides;
import de.adorsys.docusafe2.business.api.cmsencryption.CMSEncryptionService;
import de.adorsys.docusafe2.business.api.credentials.BucketAccessService;
import de.adorsys.docusafe2.business.api.credentials.DFSCredentialsService;
import de.adorsys.docusafe2.business.api.dfs.DFSConnectionService;
import de.adorsys.docusafe2.business.api.document.DocumentReadService;
import de.adorsys.docusafe2.business.api.document.DocumentWriteService;
import de.adorsys.docusafe2.business.api.keystore.PrivateKeyService;
import de.adorsys.docusafe2.business.api.keystore.PublicKeyService;
import de.adorsys.docusafe2.business.api.profile.UserProfileService;
import de.adorsys.docusafe2.business.impl.credentials.BucketAccessServiceImpl;
import de.adorsys.docusafe2.business.impl.credentials.PrivateKeyServiceImpl;
import de.adorsys.docusafe2.business.impl.credentials.PublicKeyServiceImpl;

@Module
public class DefaultDocumentModule {

    @Provides
    public DocumentReadService documentReadService(
            DFSConnectionService connectionService, CMSEncryptionService encryptionService) {
        return new CMSDocumentReadService(connectionService, encryptionService);
    }

    @Provides
    public DocumentWriteService documentWriteService(
            DFSConnectionService connectionService, CMSEncryptionService encryptionService) {
        return new CMSDocumentWriteService(connectionService, encryptionService);
    }
}
