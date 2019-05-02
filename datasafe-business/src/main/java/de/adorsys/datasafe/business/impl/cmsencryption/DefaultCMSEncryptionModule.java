package de.adorsys.datasafe.business.impl.cmsencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.impl.encryption.cmsencryption.CMSEncryptionServiceImpl;

/**
 * This module is responsible for providing CMS pathencryption of document.
 */
@Module
public abstract class DefaultCMSEncryptionModule {

    @Binds
    abstract CMSEncryptionService cmsEncryptionService(CMSEncryptionServiceImpl impl);
}
