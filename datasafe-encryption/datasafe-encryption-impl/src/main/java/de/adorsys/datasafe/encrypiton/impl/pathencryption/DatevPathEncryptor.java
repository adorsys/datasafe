package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Default path encryption/decryption that uses encryption specified by {@link DefaultPathDigestConfig} and
 * encodes resulting bytes using Base64-urlsafe encoding.
 */
@RuntimeDelegate
public class DatevPathEncryptor implements PathEncryptor {

    private final DatevPathDigestConfig digestConfig;

    @Inject
    public DatevPathEncryptor(DatevPathDigestConfig config) {
        this.digestConfig = config;
    }

    @Override
    @SneakyThrows
    public byte[] encrypt(SecretKeyIDWithKey secretKeyEntry, byte[] rawData) {
        Cipher cipher = createCipher(secretKeyEntry, digestConfig, Cipher.ENCRYPT_MODE);
        byte[] encrypted = cipher.doFinal(rawData);
        
        return encrypted;
    }

    @Override
    @SneakyThrows
    public byte[] decrypt(SecretKeyIDWithKey secretKeyEntry, byte[] encryptedData) {
        Cipher cipher = createCipher(secretKeyEntry, digestConfig, Cipher.DECRYPT_MODE);
        byte[] decrypted = cipher.doFinal(encryptedData);
        
        return decrypted;
    }

    @SneakyThrows
    private static Cipher createCipher(SecretKeyIDWithKey secretKeyEntry, DefaultPathDigestConfig config, int cipherMode) {
        byte[] key = secretKeyEntry.getSecretKey().getEncoded();
        MessageDigest sha = MessageDigest.getInstance(config.getMessageDigest());
        key = sha.digest(key);

        key = Arrays.copyOf(key, config.getShaKeyPartSize());

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, config.getAlgorithm());

        Cipher cipher = Cipher.getInstance(config.getAlgorithm());
        cipher.init(cipherMode, secretKeySpec);
        return cipher;

    }

}