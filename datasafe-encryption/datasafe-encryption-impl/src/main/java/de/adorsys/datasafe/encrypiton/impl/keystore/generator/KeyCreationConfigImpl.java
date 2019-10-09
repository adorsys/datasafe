package de.adorsys.datasafe.encrypiton.impl.keystore.generator;

import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyCreationConfig;

/**
 * Created by peter on 09.01.18.
 */
public class KeyCreationConfigImpl {
    private final KeyCreationConfig config;

    public KeyCreationConfigImpl(KeyCreationConfig config) {
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

    public int getEncKeyNumber() {
        return config.getEncKeyNumber();
    }
    public int getSignKeyNumber() {
        return config.getSignKeyNumber();
    }
}
