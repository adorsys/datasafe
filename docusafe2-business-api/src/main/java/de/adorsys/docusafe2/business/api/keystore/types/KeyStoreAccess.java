package de.adorsys.docusafe2.business.api.keystore.types;

import java.security.KeyStore;

/**
 * Created by peter on 08.01.18.
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
