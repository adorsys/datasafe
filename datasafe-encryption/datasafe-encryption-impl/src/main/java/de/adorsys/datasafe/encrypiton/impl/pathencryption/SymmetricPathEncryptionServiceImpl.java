package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.net.URI;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.types.api.global.PathEncryptionId.AES_SIV;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Path encryption service that maintains URI segments integrity.
 * It means that path/to/file is encrypted to cipher(path)/cipher(to)/cipher(file) and each invocation of example:
 * cipher(path) will yield same string.
 */
@Slf4j
@RuntimeDelegate
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

        Uri uri = processURIparts(
                bucketPath,
                str -> encode(str, cipher)
        );

        return AES_SIV.asUriRoot().resolve(uri);
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
        bucketPath = AES_SIV.asUriRoot().relativize(bucketPath);
        return processURIparts(
                bucketPath,
                str -> decode(str, cipher)
        );
    }

    @SneakyThrows
    private String decode(String str, Cipher cipher) {
        if (str.isEmpty()) {
            return str;
        }

        return new String(cipher.doFinal(encryptionConfig.byteDeserializer(str)), UTF_8);
    }

    @SneakyThrows
    private String encode(String str, Cipher cipher) {
        if (str.isEmpty()) {
            return str;
        }

        return encryptionConfig.byteSerializer(cipher.doFinal(str.getBytes(UTF_8)));
    }

    private static Uri processURIparts(
            Uri bucketPath,
            Function<String, String> process) {
        StringBuilder result = new StringBuilder();

        String path = bucketPath.getRawPath();
        if (bucketPath.getRawPath().startsWith("./")) {
            result.append("./");
            path = bucketPath.getRawPath().substring(2);
        }

        if (path.isEmpty()) {
            return new Uri(result.toString());
        }

        // Resulting value of `path` is URL-safe
        return new Uri(
                URI.create(
                        Arrays.stream(path.split(PATH_SEPARATOR, -1))
                                .map(process)
                                .collect(Collectors.joining(PATH_SEPARATOR))
                )
        );
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
