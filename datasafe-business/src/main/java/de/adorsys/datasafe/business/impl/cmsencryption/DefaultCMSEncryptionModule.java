package de.adorsys.datasafe.business.impl.cmsencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.DefaultCMSEncryptionConfig;

/**
 * This module is responsible for providing CMS encryption of document.
 */
@Module
public abstract class DefaultCMSEncryptionModule {

    /**
     * Default CMS-encryption config using AES256_CBC.
     */
    @Binds
    abstract CMSEncryptionConfig defaultCMSEncryptionConfig(DefaultCMSEncryptionConfig defaultCMSEncryptionConfig);

    /**
     * Default BouncyCastle based CMS encryption for document.
     */
    @Binds
    abstract CMSEncryptionService cmsEncryptionService(CMSEncryptionServiceImplRuntimeDelegatable impl);
}
