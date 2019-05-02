package de.adorsys.datasafe.business.impl.pathencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.encryption.pathencryption.encryption.SymmetricPathEncryptionService;

/**
 * This module is responsible for providing CMS pathencryption of document.
 */
@Module
public abstract class DefaultPathEncryptionModule {

    @Binds
    abstract PathEncryption pathEncryption(PathEncryptionImpl impl);

    @Binds
    abstract SymmetricPathEncryptionService bucketPathEncryptionService(SymmetricPathEncryptionServiceImpl impl);
}
