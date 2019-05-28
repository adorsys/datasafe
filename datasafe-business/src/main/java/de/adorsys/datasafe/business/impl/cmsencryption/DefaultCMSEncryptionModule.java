package de.adorsys.datasafe.business.impl.cmsencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionServiceImpl;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.DefaultCMSEncryptionConfig;

/**
 * This module is responsible for providing CMS pathencryption of document.
 */
@Module
public abstract class DefaultCMSEncryptionModule {

    @Binds
    abstract CMSEncryptionConfig defaultCMSEncryptionConfig(DefaultCMSEncryptionConfig defaultCMSEncryptionConfig);

    @Binds
    abstract CMSEncryptionService cmsEncryptionService(CMSEncryptionServiceImpl impl);
}
