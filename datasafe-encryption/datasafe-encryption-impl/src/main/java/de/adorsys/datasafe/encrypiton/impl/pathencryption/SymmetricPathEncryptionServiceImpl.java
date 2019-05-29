package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Path encryption service that maintains URI segments integrity.
 * It means that path/to/file is encrypted to cipher(path)/cipher(to)/cipher(file) and each invocation of i.e.
 * cipher(path) will yield same string.
 */
@Slf4j
public class SymmetricPathEncryptionServiceImpl implements SymmetricPathEncryptionService {

    private static final String PATH_SEPARATOR = "/";

    private final PathEncryptionConfig encryptionConfig;

    @Inject
    public SymmetricPathEncryptionServiceImpl(PathEncryptionConfig encryptionConfig) {
        this.encryptionConfig = encryptionConfig;
    }

    /**
     * Encrypts each URI segment separately and composes them back in same order.
     */
    @Override
    @SneakyThrows
    public Uri encrypt(SecretKey secretKey, Uri bucketPath) {
        validateArgs(secretKey, bucketPath);
        validateUriIsRelative(bucketPath);

        Cipher cipher = encryptionConfig.encryptionCipher(secretKey);

        return processURIparts(
                bucketPath,
                str -> encode(str, cipher)
        );
    }

    /**
     * Decrypts each URI segment separately and composes them back in same order.
     */
    @Override
    @SneakyThrows
    public Uri decrypt(SecretKey secretKey, Uri bucketPath) {
        validateArgs(secretKey, bucketPath);
        validateUriIsRelative(bucketPath);

        Cipher cipher = encryptionConfig.decryptionCipher(secretKey);

        return processURIparts(
                bucketPath,
                str -> decode(str, cipher)
        );
    }

    @SneakyThrows
    private String decode(String str, Cipher cipher) {
        return new String(cipher.doFinal(encryptionConfig.byteDeserializer(str)), UTF_8);
    }

    @SneakyThrows
    private String encode(String str, Cipher cipher) {
        return encryptionConfig.byteSerializer(cipher.doFinal(str.getBytes(UTF_8)));
    }

    private static Uri processURIparts(Uri bucketPath, Function<String, String> process) {
        StringBuilder result = new StringBuilder();

        String path = bucketPath.getPath();
        if (bucketPath.getPath().startsWith("./")) {
            result.append("./");
            path = bucketPath.getPath().substring(2);
        }

        if (path.isEmpty()) {
            return new Uri(result.toString());
        }
        boolean hasStarted = false;
        for (String part : path.split(PATH_SEPARATOR)) {

            if (hasStarted) {
                result.append(PATH_SEPARATOR);
            }

            result.append(process.apply(part));

            hasStarted = true;
        }

        return new Uri(result.toString());
    }

    private static void validateArgs(SecretKey secretKey, Uri bucketPath) {
        if (null == secretKey) {
            throw new IllegalArgumentException("Secret key should not be null");
        }

        if (null == bucketPath) {
            throw new IllegalArgumentException("Bucket path should not be null");
        }
    }

    private static void validateUriIsRelative(Uri uri) {
        if (uri.isAbsolute()) {
            throw new IllegalArgumentException("URI should be relative");
        }
    }
}
