package de.adorsys.datasafe.simple.adapter.impl.pathencryption;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.simple.adapter.api.legacy.pathencryption.LegacyPathEncryptionConfig;
import de.adorsys.datasafe.simple.adapter.api.legacy.pathencryption.LegacySymmetricPathEncryptionService;
import de.adorsys.datasafe.simple.adapter.impl.legacy.pathencryption.LegacyPathDigestConfig;
import de.adorsys.datasafe.simple.adapter.impl.legacy.pathencryption.LegacyPathEncryptor;
import de.adorsys.datasafe.simple.adapter.impl.legacy.pathencryption.LegacyIntegrityPreservingUriEncryption;

/**
 * This module is responsible for providing pathencryption of document.
 */
@Module
public abstract class LegacyPathEncryptionModule {

    /**
     * Default path digest that specifies AES and SHA-256 for path encryption.
     */
    @Provides
    static LegacyPathDigestConfig digestConfig() {
        return new LegacyPathDigestConfig();
    }

    /**
     * Default path encryption that uses Base64-urlsafe path serialization
     */
    @Binds
    abstract LegacyPathEncryptionConfig config(LegacyPathEncryptor config);

    /**
     * By default simply use
     * {@link de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService} to get key
     * and pass path with key to {@link LegacySymmetricPathEncryptionService}
     */
    @Binds
    abstract PathEncryption pathEncryption(SwitchablePathEncryptionImpl impl);

    /**
     * Default symmetric path encryption that encrypts URI segment-by-segment.
     */
    @Binds
    abstract LegacySymmetricPathEncryptionService legacySymmetricPathEncryptionService(LegacyIntegrityPreservingUriEncryption impl);
}
