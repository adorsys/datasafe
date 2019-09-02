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
<<<<<<< HEAD:datasafe-encryption/datasafe-encryption-impl/src/main/java/de/adorsys/datasafe/encrypiton/impl/pathencryption/SimplePathEncryptor.java
<<<<<<< HEAD:datasafe-encryption/datasafe-encryption-impl/src/main/java/de/adorsys/datasafe/encrypiton/impl/pathencryption/SimplePathEncryptor.java
public class SimplePathEncryptor implements PathEncryptor {

    private final SimplePathDigestConfig digestConfig;

    @Inject
    public SimplePathEncryptor(SimplePathDigestConfig config) {
=======
public class DatevPathEncryptor implements PathEncryptor {
=======
public class SimplePathEncryptor implements PathEncryptor {
>>>>>>> 03508a1b... DOC-239: code clean up:datasafe-encryption/datasafe-encryption-impl/src/main/java/de/adorsys/datasafe/encrypiton/impl/pathencryption/SimplePathEncryptor.java

    private final SimplePathDigestConfig digestConfig;

    @Inject
<<<<<<< HEAD:datasafe-encryption/datasafe-encryption-impl/src/main/java/de/adorsys/datasafe/encrypiton/impl/pathencryption/SimplePathEncryptor.java
    public DatevPathEncryptor(DatevPathDigestConfig config) {
>>>>>>> a60c9d5a... DOC-239: Using AES in GCM mode. Initial Path encryption impl with SIV:datasafe-encryption/datasafe-encryption-impl/src/main/java/de/adorsys/datasafe/encrypiton/impl/pathencryption/DatevPathEncryptor.java
=======
    public SimplePathEncryptor(SimplePathDigestConfig config) {
>>>>>>> 03508a1b... DOC-239: code clean up:datasafe-encryption/datasafe-encryption-impl/src/main/java/de/adorsys/datasafe/encrypiton/impl/pathencryption/SimplePathEncryptor.java
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
