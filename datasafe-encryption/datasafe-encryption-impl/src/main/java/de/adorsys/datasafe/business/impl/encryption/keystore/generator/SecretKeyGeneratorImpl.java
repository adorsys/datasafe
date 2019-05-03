package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import de.adorsys.datasafe.business.api.types.keystore.SecretKeyGenerator;

import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;

public class SecretKeyGeneratorImpl implements SecretKeyGenerator {

    private final String secretKeyAlgo;
    private final Integer keySize;

    public SecretKeyGeneratorImpl(String secretKeyAlgo, Integer keySize) {
        this.secretKeyAlgo = secretKeyAlgo;
        this.keySize = keySize;
    }

    @Override
    public SecretKeyData generate(String alias, CallbackHandler secretKeyPassHandler) {
        SecretKey secretKey = new SecretKeyBuilder()
                .withKeyAlg(secretKeyAlgo)
                .withKeyLength(keySize)
                .build();

        return SecretKeyData.builder().secretKey(secretKey).alias(alias).passwordSource(secretKeyPassHandler).keyAlgo(secretKeyAlgo).build();

    }
}
