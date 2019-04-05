package de.adorsys.docusafe2.business.impl.bucketpathencryption;

import de.adorsys.common.exceptions.BaseException;
import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.common.utils.HexUtil;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.complextypes.BucketPathUtil;
import de.adorsys.docusafe2.business.api.bucketpathencryption.BucketPathEncryptionService;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class BucketPathEncryptionServiceImpl implements BucketPathEncryptionService {

    public static final String ALGORITHM = "AES";

    @Override
    public BucketPath encrypt(SecretKeySpec secretKey, BucketPath bucketPath) {
        Optional<Cipher> cipher = createCipher(secretKey, Cipher.ENCRYPT_MODE);

        cipher.orElseThrow(() -> new BaseException("Can't build cipher, secret key is absent"));

        List<String> subdirs = BucketPathUtil.split(BucketPathUtil.getAsString(bucketPath));
        StringBuilder encryptedPathString = new StringBuilder();
        for(String subdir : subdirs) {
            byte[] encrypt = new byte[0];
            try {
                encrypt = cipher.get().doFinal(subdir.getBytes(UTF_8));
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                log.error("Error during encryption", e.getMessage());
            }

            String encryptedString = HexUtil.convertBytesToHexString(encrypt);
            encryptedPathString.append(BucketPath.BUCKET_SEPARATOR).append(encryptedString);
        }
        return new BucketPath(encryptedPathString.toString().toLowerCase());
    }

    @Override
    public BucketPath decrypt(SecretKeySpec secretKey, BucketPath bucketPath) {
        Optional<Cipher> cipher = createCipher(secretKey, Cipher.DECRYPT_MODE);

        cipher.orElseThrow(() -> new BaseException("Can't build cipher, secret key is absent"));

        List<String> subdirs = BucketPathUtil.split(BucketPathUtil.getAsString(bucketPath));
        StringBuilder decryptedPathString = new StringBuilder();
        for(String subdir : subdirs) {
            byte[] decrypt = HexUtil.convertHexStringToBytes(subdir.toUpperCase());
            byte[] decryptedBytes = new byte[0];
            try {
                decryptedBytes = cipher.get().doFinal(decrypt);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                log.error("Error during decryption", e.getMessage());
            }
            decryptedPathString.append(BucketPath.BUCKET_SEPARATOR).append(new String(decryptedBytes, UTF_8));
        }
        return new BucketPath(decryptedPathString.toString());
    }

    private Optional<Cipher> createCipher(SecretKeySpec secretKey, int cipherMode) {
        if(secretKey == null) return Optional.empty();

        try {
            byte[] key = secretKey.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(cipherMode, secretKeySpec);
            return Optional.of(cipher);
        } catch (Exception e) {
            throw BaseExceptionHandler.handle(e);
        }

    }
}
