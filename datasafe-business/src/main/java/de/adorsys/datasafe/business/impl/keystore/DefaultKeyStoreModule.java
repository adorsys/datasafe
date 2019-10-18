package de.adorsys.datasafe.business.impl.keystore;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.keystore.PublicKeySerde;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyStoreConfig;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.keystore.PublicKeySerdeImplRuntimeDelegatable;

import javax.annotation.Nullable;

/**
 * This module provides keystore management operations.
 */
@Module
public abstract class DefaultKeyStoreModule {

    @Provides
    static KeyStoreConfig cmsEncryptionConfig(@Nullable EncryptionConfig config) {
        if (null == config) {
            return EncryptionConfig.builder().build().getKeystore();
        }

        return config.getKeystore();
    }

    /**
     * Default public key serializer.
     */
    @Binds
    public abstract PublicKeySerde publicKeySerde(PublicKeySerdeImplRuntimeDelegatable impl);

    /**
     * If no external configuration provided ({@link KeyStoreConfig}), BouncyCastle BCFKS key store type is
     * used by default.
     */
    @Binds
    public abstract KeyStoreService keyStoreService(KeyStoreServiceImplRuntimeDelegatable impl);
}
