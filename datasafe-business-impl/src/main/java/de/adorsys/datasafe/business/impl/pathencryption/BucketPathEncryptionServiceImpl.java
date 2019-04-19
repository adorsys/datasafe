package de.adorsys.datasafe.business.impl.pathencryption;

import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.common.utils.HexUtil;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.complextypes.BucketPathUtil;
import de.adorsys.datasafe.business.api.encryption.bucketpathencryption.BucketPathEncryptionService;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;


public class BucketPathEncryptionServiceImpl implements BucketPathEncryptionService {

    @Inject
    public BucketPathEncryptionServiceImpl() {
    }

    @Override
    public URI encrypt(SecretKeySpec secretKey, URI bucketPath) {
        return bucketPath;
    }

    @Override
    public URI decrypt(SecretKeySpec secretKey, URI bucketPath) {
        return bucketPath;
    }

    public BucketPath encrypt(SecretKeySpec secretKey, BucketPath bucketPath) {
        Cipher cipher = createCipher(secretKey, Cipher.ENCRYPT_MODE);

        List<String> subdirs = BucketPathUtil.split(BucketPathUtil.getAsString(bucketPath));
        StringBuilder encryptedPathString = new StringBuilder();
        for(String subdir : subdirs) {
            byte[] encrypt = new byte[0];
            try {
                encrypt = cipher.doFinal(subdir.getBytes(UTF_8));
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }

            String encryptedString = HexUtil.convertBytesToHexString(encrypt);
            encryptedPathString.append(BucketPath.BUCKET_SEPARATOR).append(encryptedString);
        }
        return new BucketPath(encryptedPathString.toString().toLowerCase());
    }

    public BucketPath decrypt(SecretKeySpec secretKey, BucketPath bucketPath) {
        Cipher cipher = createCipher(secretKey, Cipher.DECRYPT_MODE);

        List<String> subdirs = BucketPathUtil.split(BucketPathUtil.getAsString(bucketPath));
        StringBuilder decryptedPathString = new StringBuilder();
        for(String subdir : subdirs) {
            byte[] decrypt = HexUtil.convertHexStringToBytes(subdir.toUpperCase());
            byte[] decryptedBytes = new byte[0];
            try {
                decryptedBytes = cipher.doFinal(decrypt);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
            decryptedPathString.append(BucketPath.BUCKET_SEPARATOR).append(new String(decryptedBytes, UTF_8));
        }
        return new BucketPath(decryptedPathString.toString());
    }

    private static Cipher createCipher(SecretKeySpec secretKey, int cipherMode) {
        try {
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
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }
}
