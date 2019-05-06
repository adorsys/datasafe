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

    @Inject
    public DefaultPathEncryption() {
    }

    @Override
    public Cipher encryptionCipher(SecretKey secretKey) {
        return createCipher(secretKey, Cipher.ENCRYPT_MODE);
    }

    @Override
    public Cipher decryptionCipher(SecretKey secretKey) {
        return createCipher(secretKey, Cipher.DECRYPT_MODE);
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
    private static Cipher createCipher(SecretKey secretKey, int cipherMode) {
        byte[] key = secretKey.getEncoded();
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        // nur die ersten 128 bit nutzen
        key = Arrays.copyOf(key, 16);
        // der fertige Schluessel
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(cipherMode, secretKeySpec);
        return cipher;
    }
}
