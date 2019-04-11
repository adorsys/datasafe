package de.adorsys.datasafe.business.impl.keystore.generator;

import de.adorsys.docusafe2.business.api.keystore.types.KeyStoreCreationConfig;

/**
 * Created by peter on 09.01.18.
 */
public class KeyStoreCreationConfigImpl {
    private final KeyStoreCreationConfig config;

    public KeyStoreCreationConfigImpl(KeyStoreCreationConfig config) {
        this.config = config;
    }

    public KeyPairGeneratorImpl getEncKeyPairGenerator(String keyPrefix) {
        return new KeyPairGeneratorImpl("RSA", 2048, "SHA256withRSA", "enc-" + keyPrefix);
    }

    public KeyPairGeneratorImpl getSignKeyPairGenerator(String keyPrefix) {
        return new KeyPairGeneratorImpl("RSA", 2048, "SHA256withRSA", "sign-" + keyPrefix);
    }

    public SecretKeyGeneratorImpl getSecretKeyGenerator(String keyPrefix) {
        return new SecretKeyGeneratorImpl("AES", 256);
    }

    public Integer getEncKeyNumber() {
        return config.getEncKeyNumber();
    }
    public Integer getSignKeyNumber() {
        return config.getSignKeyNumber();
    }
    public Integer getSecretKeyNumber() {
        return config.getSecretKeyNumber();
    }
}
