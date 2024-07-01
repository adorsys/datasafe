package de.adorsys.datasafe.business.impl.cmsencryption;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.encryption.CmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionServiceImplRuntimeDelegatable;

import javax.annotation.Nullable;

/**
 * This module is responsible for providing CMS encryption of document.
 */
@Module
public abstract class DefaultCMSEncryptionModule {

    /**
     * Default CMS-encryption config using AES256_GCM.
     */
    @Provides
    static CmsEncryptionConfig cmsEncryptionConfig(@Nullable EncryptionConfig config) {
        if (null == config) {
            return EncryptionConfig.builder().build().getCms();
        }

        return config.getCms();
    }

    /**
     * Default BouncyCastle based CMS encryption for document.
     */
    @Binds
    abstract CMSEncryptionService cmsEncryptionService(CMSEncryptionServiceImplRuntimeDelegatable impl);
}
