package de.adorsys.datasafe.encrypiton.impl.keystore.generator;

import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;

/**
 * Created by peter on 09.01.18.
 */
public class KeyCreationConfigImpl {
    private final KeyCreationConfig config;

    public KeyCreationConfigImpl(KeyCreationConfig config) {
        this.config = config;
    }

    public KeyPairGeneratorImpl getEncKeyPairGenerator(String keyPrefix) {
        return new KeyPairGeneratorImpl(
                config.getEncrypting().getAlgo(),
                config.getEncrypting().getSize(),
                config.getEncrypting().getSigAlgo(),
                "enc-" + keyPrefix
        );
    }

    public KeyPairGeneratorImpl getSignKeyPairGenerator(String keyPrefix) {
        return new KeyPairGeneratorImpl(
                config.getSinging().getAlgo(),
                config.getSinging().getSize(),
                config.getSinging().getSigAlgo(),
                "sign-" + keyPrefix
        );
    }

    public SecretKeyGeneratorImpl getSecretKeyGenerator() {
        return new SecretKeyGeneratorImpl(config.getSecret().getAlgo(), config.getSecret().getSize());
    }

    public int getEncKeyNumber() {
        return config.getEncKeyNumber();
    }
    public int getSignKeyNumber() {
        return config.getSignKeyNumber();
    }
}
