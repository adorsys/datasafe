package de.adorsys.datasafe.business.impl.keystore;

import dagger.Binds;
import dagger.Module;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.keystore.PublicKeySerde;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.encrypiton.impl.keystore.PublicKeySerdeImpl;

/**
 * This module provides keystore management operations.
 */
@Module
public abstract class DefaultKeyStoreModule {

    /**
     * Default public key serializer using {@link java.io.ObjectInputStream} and Base64 encoding of bytes
     */
    @Binds
    public abstract PublicKeySerde publicKeySerde(PublicKeySerdeImpl impl);

    /**
     * By default, BouncyCastle keystore - UBER, or one specified by system property SERVER_KEYSTORE_TYPE.
     */
    @Binds
    public abstract KeyStoreService keyStoreService(KeyStoreServiceImpl impl);
}
