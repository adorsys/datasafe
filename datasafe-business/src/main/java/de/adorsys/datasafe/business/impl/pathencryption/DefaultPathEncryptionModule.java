package de.adorsys.datasafe.business.impl.pathencryption;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.business.api.encryption.pathencryption.PathEncryption;
import de.adorsys.datasafe.business.api.encryption.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.DefaultPathEncryption;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.PathEncryptionConfig;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.business.impl.encryption.pathencryption.encryption.SymmetricPathEncryptionServiceImpl;

/**
 * This module is responsible for providing CMS pathencryption of document.
 */
@Module
public abstract class DefaultPathEncryptionModule {

    @Binds
    abstract PathEncryptionConfig config(DefaultPathEncryption config);

    @Binds
    abstract PathEncryption pathEncryption(PathEncryptionImpl impl);

    @Binds
    abstract SymmetricPathEncryptionService bucketPathEncryptionService(SymmetricPathEncryptionServiceImpl impl);
}
