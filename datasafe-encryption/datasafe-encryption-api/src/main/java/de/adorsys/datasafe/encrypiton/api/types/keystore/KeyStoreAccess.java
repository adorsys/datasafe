package de.adorsys.datasafe.encrypiton.api.types.keystore;

import java.security.KeyStore;

/**
 * Wrapper for keystore with credentials at least to read public keys in it.
 */
public class KeyStoreAccess {

    private final KeyStore keyStore;
    private final KeyStoreAuth keyStoreAuth;

    public KeyStoreAccess(KeyStore keyStore, KeyStoreAuth keyStoreAuth) {
        this.keyStore = keyStore;
        this.keyStoreAuth = keyStoreAuth;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public KeyStoreAuth getKeyStoreAuth() {
        return keyStoreAuth;
    }

    @Override
    public String toString() {
        return "KeyStoreAccess{" +
                "keyStore=" + keyStore +
                ", keyStoreAuth=" + keyStoreAuth +
                '}';
    }
}
