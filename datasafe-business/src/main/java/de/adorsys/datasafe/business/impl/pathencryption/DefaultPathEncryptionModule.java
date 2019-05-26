package de.adorsys.datasafe.business.impl.pathencryption;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.DefaultPathDigestConfig;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.DefaultPathEncryption;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.SymmetricPathEncryptionServiceImpl;

/**
 * This module is responsible for providing CMS pathencryption of document.
 */
@Module
public abstract class DefaultPathEncryptionModule {

    @Provides
    static DefaultPathDigestConfig digestConfig() {
        return new DefaultPathDigestConfig();
    }

    @Binds
    abstract PathEncryptionConfig config(DefaultPathEncryption config);

    @Binds
    abstract PathEncryption pathEncryption(PathEncryptionImpl impl);

    @Binds
    abstract SymmetricPathEncryptionService bucketPathEncryptionService(SymmetricPathEncryptionServiceImpl impl);
}
