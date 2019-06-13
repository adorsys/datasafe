package de.adorsys.datasafe.simple.adapter.impl;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.encrypiton.api.pathencryption.PathEncryption;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.*;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.extern.slf4j.Slf4j;

/**
 * This module is responsible for providing noop pathencryption of document.
 */

@Module
abstract class CustomPathEncryptionModule {
    /**
     * Default path digest that specifies AES and SHA-256 for path encryption.
     */
    @Provides
    static DefaultPathDigestConfig digestConfig() {
        return new DefaultPathDigestConfig();
    }

    /**
     * Default path encryption that uses Base64-urlsafe path serialization
     */
    @Binds
    abstract PathEncryptionConfig config(DefaultPathEncryptionRuntimeDelegatable config);

    /**
     * By default simply use
     * {@link de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService} to get key
     * and pass path with key to {@link SymmetricPathEncryptionService}
     */
    @Binds
    abstract PathEncryption pathEncryption(SwitchablePathEncryptionImplRuntimeDelegatable impl);

    /**
     * Default symmetric path encryption that encrypts URI segment-by-segment.
     */
    @Binds
    abstract SymmetricPathEncryptionService bucketPathEncryptionService(SymmetricPathEncryptionServiceImplRuntimeDelegatable impl);
}