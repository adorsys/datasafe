package de.adorsys.datasafe.business.impl.cmsencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.encryption.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.api.types.cobertura.CoberturaIgnore;
import de.adorsys.datasafe.business.impl.encryption.cmsencryption.CMSEncryptionConfig;
import de.adorsys.datasafe.business.impl.encryption.cmsencryption.CMSEncryptionServiceImpl;
import de.adorsys.datasafe.business.impl.encryption.cmsencryption.DefaultCMSEncryptionConfig;

/**
 * This module is responsible for providing CMS pathencryption of document.
 */
@Module
public abstract class DefaultCMSEncryptionModule {

    @CoberturaIgnore
    @Binds
    abstract CMSEncryptionConfig defaultCMSEncryptionConfig(DefaultCMSEncryptionConfig defaultCMSEncryptionConfig);

    @CoberturaIgnore
    @Binds
    abstract CMSEncryptionService cmsEncryptionService(CMSEncryptionServiceImpl impl);
}
