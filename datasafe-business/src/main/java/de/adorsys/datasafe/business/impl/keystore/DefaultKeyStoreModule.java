package de.adorsys.datasafe.business.impl.keystore;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.keystore.PublicKeySerde;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.keystore.PublicKeySerdeImplRuntimeDelegatable;
import de.adorsys.keymanagement.api.Juggler;
import de.adorsys.keymanagement.api.config.keystore.KeyStoreConfig;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;

import javax.annotation.Nullable;

/**
 * This module provides keystore management operations.
 */
@Module
public abstract class DefaultKeyStoreModule {

    /**
     * Configures default keystore type and encryption, can be overridden by withConfig method.
     */
    @Provides
    static KeyStoreConfig keyStoreConfig(@Nullable EncryptionConfig config) {
        if (null == config) {
            return EncryptionConfig.builder().build().getKeystore();
        }

        return config.getKeystore();
    }

    /**
     * Includes default bouncy castle key management implementation
     */
    @Provides
    static Juggler juggler(KeyStoreConfig config) {
        return DaggerBCJuggler.builder().keyStoreConfig(config).build();
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
