package de.adorsys.datasafe.business.impl.keystore.generator;

import de.adorsys.docusafe2.business.api.keystore.types.SecretKeyGenerator;

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
