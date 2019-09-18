package de.adorsys.datasafe.simple.adapter.impl.legacy.pathencryption;

import de.adorsys.datasafe.simple.adapter.api.legacy.pathencryption.LegacyPathEncryptionConfig;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Default path encryption/decryption that uses encryption specified by {@link LegacyPathDigestConfig} and
 * encodes resulting bytes using Base64-urlsafe encoding.
 */
public class LegacyPathEncryptor implements LegacyPathEncryptionConfig {

    private final LegacyPathDigestConfig digestConfig;

    @Inject
    public LegacyPathEncryptor(LegacyPathDigestConfig config) {
        this.digestConfig = config;
    }

    @Override
    public Cipher encryptionCipher(SecretKey secretKey) {
        return createCipher(secretKey, digestConfig, Cipher.ENCRYPT_MODE);
    }

    @Override
    public Cipher decryptionCipher(SecretKey secretKey) {
        return createCipher(secretKey, digestConfig, Cipher.DECRYPT_MODE);
    }

    @Override
    public String byteSerializer(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    @Override
    public byte[] byteDeserializer(String input) {
        return Base64.getUrlDecoder().decode(input);
    }

    @SneakyThrows
    private static Cipher createCipher(SecretKey secretKey, LegacyPathDigestConfig config, int cipherMode) {
        byte[] key = secretKey.getEncoded();
        MessageDigest sha = MessageDigest.getInstance(config.getMessageDigest());
        key = sha.digest(key);

        key = Arrays.copyOf(key, config.getShaKeyPartSize());

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, config.getAlgorithm());

        Cipher cipher = Cipher.getInstance(config.getAlgorithm());
        cipher.init(cipherMode, secretKeySpec);
        return cipher;
    }
}
