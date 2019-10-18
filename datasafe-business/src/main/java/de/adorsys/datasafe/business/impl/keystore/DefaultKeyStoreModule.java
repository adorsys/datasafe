package de.adorsys.datasafe.business.impl.keystore;

import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.keystore.PublicKeySerde;
import de.adorsys.datasafe.encrypiton.impl.keystore.DefaultPasswordBasedKeyConfigRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.keystore.PublicKeySerdeImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreConfig;
import de.adorsys.datasafe.encrypiton.impl.keystore.types.PasswordBasedKeyConfig;

/**
 * This module provides keystore management operations.
 */
@Module
public abstract class DefaultKeyStoreModule {

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

    /**
     * Configures how to persist storage access credentials.
     */
    @Binds
    public abstract PasswordBasedKeyConfig passwordBasedKeyConfig(DefaultPasswordBasedKeyConfigRuntimeDelegatable impl);

    /**
     * Keystore settings - type, encryption, etc.
     */
    @BindsOptionalOf
    public abstract KeyStoreConfig keyStoreConfig();

}
