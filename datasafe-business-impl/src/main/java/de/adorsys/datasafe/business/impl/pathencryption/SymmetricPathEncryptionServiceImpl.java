package de.adorsys.datasafe.business.impl.pathencryption;

import de.adorsys.common.exceptions.BaseExceptionHandler;
import de.adorsys.datasafe.business.api.encryption.pathencryption.encryption.SymmetricPathEncryptionService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class SymmetricPathEncryptionServiceImpl implements SymmetricPathEncryptionService {

    private static final String PATH_SEPARATOR = "/";

    @Inject
    public SymmetricPathEncryptionServiceImpl() {
    }

    @Override
    @SneakyThrows
    public URI encrypt(SecretKeySpec secretKey, URI bucketPath) {
        validateUriIsRelative(bucketPath);

        Cipher cipher = createCipher(secretKey, Cipher.ENCRYPT_MODE);

        String path = bucketPath.getPath().startsWith("./") ? bucketPath.getPath().substring(2) : bucketPath.getPath();

        StringBuilder result = new StringBuilder();
        for (String part : path.split(PATH_SEPARATOR)) {
            result.append(
                    Base64.getUrlEncoder().encodeToString(cipher.doFinal(part.getBytes(UTF_8)))
            ).append(PATH_SEPARATOR);
        }

        return new URI(result.toString());
    }

    @Override
    @SneakyThrows
    public URI decrypt(SecretKeySpec secretKey, URI bucketPath) {
        validateUriIsRelative(bucketPath);

        Cipher cipher = createCipher(secretKey, Cipher.DECRYPT_MODE);

        StringBuilder result = new StringBuilder();
        for (String part : bucketPath.getPath().split(PATH_SEPARATOR)) {
            result.append(
                    new String(cipher.doFinal(Base64.getUrlDecoder().decode(part)), UTF_8)
            ).append(PATH_SEPARATOR);
        }

        return new URI(result.toString());
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

    private void validateUriIsRelative(URI uri) {
        if (uri.isAbsolute()) {
            throw new IllegalArgumentException("URI should be relative");
        }
    }
}
