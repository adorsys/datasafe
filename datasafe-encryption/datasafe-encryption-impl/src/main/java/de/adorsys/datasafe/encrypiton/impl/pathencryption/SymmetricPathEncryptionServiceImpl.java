package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.net.URI;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class SymmetricPathEncryptionServiceImpl implements SymmetricPathEncryptionService {

    private static final String PATH_SEPARATOR = "/";

    private final PathEncryptionConfig encryptionConfig;

    @Inject
    public SymmetricPathEncryptionServiceImpl(PathEncryptionConfig encryptionConfig) {
        this.encryptionConfig = encryptionConfig;
    }

    @Override
    @SneakyThrows
    public URI encrypt(SecretKey secretKey, URI bucketPath) {
        validateArgs(secretKey, bucketPath);
        validateUriIsRelative(bucketPath);

        Cipher cipher = encryptionConfig.encryptionCipher(secretKey);

        return processURIparts(
                bucketPath,
                str -> encode(str, cipher)
        );
    }

    @Override
    @SneakyThrows
    public URI decrypt(SecretKey secretKey, URI bucketPath) {
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

    @SneakyThrows
    private static URI processURIparts(URI bucketPath, Function<String, String> process) {
        StringBuilder result = new StringBuilder();

        String path = bucketPath.getPath();
        if (bucketPath.getPath().startsWith("./")) {
            result.append("./");
            path = bucketPath.getPath().substring(2);
        }

        if (path.isEmpty()) {
            return new URI(result.toString());
        }
        boolean hasStarted = false;
        for (String part : path.split(PATH_SEPARATOR)) {

            if (hasStarted) {
                result.append(PATH_SEPARATOR);
            }

            result.append(process.apply(part));

            hasStarted = true;
        }

        return new URI(result.toString());
    }

    private static void validateArgs(SecretKey secretKey, URI bucketPath) {
        if (null == secretKey) {
            throw new IllegalArgumentException("Secret key should not be null");
        }

        if (null == bucketPath) {
            throw new IllegalArgumentException("Bucket path should not be null");
        }
    }

    private static void validateUriIsRelative(URI uri) {
        if (uri.isAbsolute()) {
            throw new IllegalArgumentException("URI should be relative");
        }
    }
}
