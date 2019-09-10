package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.inject.Inject;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private final PathEncryptorDecryptor pathEncryptorDecryptor;
    private final Function<ImmutablePair<SecretKeyIDWithKey, byte[]>, byte[]> encryptAndEncode;
    private final Function<ImmutablePair<SecretKeyIDWithKey, byte[]>, byte[]> decryptAndDecode;

    @Inject
    public SymmetricPathEncryptionServiceImpl(PathEncryptorDecryptor pathEncryptorDecryptor) {
        this.pathEncryptorDecryptor = pathEncryptorDecryptor;

        encryptAndEncode = keyEntryEncryptedDataPair -> encode(pathEncryptorDecryptor.encrypt(
                keyEntryEncryptedDataPair.left, keyEntryEncryptedDataPair.right
        ));

        decryptAndDecode = keyEntryDecryptedDataPair -> pathEncryptorDecryptor.decrypt(
                keyEntryDecryptedDataPair.left, decode(keyEntryDecryptedDataPair.right)
        );
    }

    /**
     * Encrypts each URI segment separately and composes them back in same order.
     */
    @Override
    @SneakyThrows
    public Uri encrypt(SecretKeyIDWithKey secretKeyEntry, Uri bucketPath) {
        validateArgs(secretKeyEntry, bucketPath);
        validateUriIsRelative(bucketPath);

        return processURIparts(
                secretKeyEntry,
                bucketPath,
                encryptAndEncode
        );
    }

    /**
     * Decrypts each URI segment separately and composes them back in same order.
     */
    @Override
    @SneakyThrows
    public Uri decrypt(SecretKeyIDWithKey secretKeyEntry, Uri bucketPath) {
        validateArgs(secretKeyEntry, bucketPath);
        validateUriIsRelative(bucketPath);

        return processURIparts(
                secretKeyEntry,
                bucketPath,
                decryptAndDecode
        );
    }

    @SneakyThrows
    private static byte[] decode(byte[] encryptedData) {
        if (null == encryptedData || encryptedData.length == 0) {
            return encryptedData;
        }

        return Base64.getUrlDecoder().decode(encryptedData);
    }

    @SneakyThrows
    private static byte[] encode(byte[] encryptedData) {
        if (null == encryptedData) {
            return null;
        }

        return Base64.getUrlEncoder().encodeToString(encryptedData).getBytes(UTF_8);
    }

    private static Uri processURIparts(
            SecretKeyIDWithKey secretKeyEntry,
            Uri bucketPath,
            Function<ImmutablePair<SecretKeyIDWithKey, byte[]>, byte[]> process) {
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
                        Arrays.stream(path.split(PATH_SEPARATOR))
                                .map(uriPart -> process.apply(new ImmutablePair<>(secretKeyEntry, uriPart.getBytes(UTF_8))))
                                .map(String::new) // byte[] -> string
                                .collect(Collectors.joining(PATH_SEPARATOR)))
        );
    }

    private static void validateArgs(SecretKeyIDWithKey secretKeyEntry, Uri bucketPath) {
        if (null == secretKeyEntry || null == secretKeyEntry.getSecretKey()) {
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