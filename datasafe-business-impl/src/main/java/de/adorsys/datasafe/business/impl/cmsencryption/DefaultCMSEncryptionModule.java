package de.adorsys.datasafe.business.impl.cmsencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.types.CMSEncryptionConfig;
import de.adorsys.datasafe.business.impl.cmsencryption.services.CMSEncryptionServiceImpl;
import de.adorsys.datasafe.business.impl.cmsencryption.services.DefaultCMSEncryptionConfig;

/**
 * This module is responsible for providing CMS encryption of document.
 */
@Module
public abstract class DefaultCMSEncryptionModule {

    @Binds
    abstract CMSEncryptionConfig defaultCMSEncryptionConfig(DefaultCMSEncryptionConfig defaultCMSEncryptionConfig);

    @Binds
    abstract CMSEncryptionService cmsEncryptionService(CMSEncryptionServiceImpl impl);
}
