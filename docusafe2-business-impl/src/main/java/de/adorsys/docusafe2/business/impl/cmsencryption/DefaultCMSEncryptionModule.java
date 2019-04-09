package de.adorsys.docusafe2.business.impl.cmsencryption;

import dagger.Module;
import dagger.Provides;
import de.adorsys.docusafe2.business.api.cmsencryption.CMSEncryptionService;
import de.adorsys.docusafe2.business.impl.cmsencryption.services.CMSEncryptionServiceImpl;

@Module
public class DefaultCMSEncryptionModule {

    @Provides
    public CMSEncryptionService cmsEncryptionService() {
        return new CMSEncryptionServiceImpl();
    }
}
