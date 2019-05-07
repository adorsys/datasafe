package de.adorsys.datasafe.business.impl.encryption.pathencryption;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class DefaultPathEncryption implements PathEncryptionConfig {

    private final DefaultPathDigestConfig digestConfig;

    @Inject
    public DefaultPathEncryption(DefaultPathDigestConfig config) {
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
    private static Cipher createCipher(SecretKey secretKey, DefaultPathDigestConfig config, int cipherMode) {
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
