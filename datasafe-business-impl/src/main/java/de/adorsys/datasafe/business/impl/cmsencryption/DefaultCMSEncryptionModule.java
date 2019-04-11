package de.adorsys.datasafe.business.impl.cmsencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.business.impl.cmsencryption.services.CMSEncryptionServiceImpl;

/**
 * This module is responsible for providing CMS encryption of document.
 */
@Module
public abstract class DefaultCMSEncryptionModule {

    @Binds
    abstract CMSEncryptionService cmsEncryptionService(CMSEncryptionServiceImpl impl);
}
