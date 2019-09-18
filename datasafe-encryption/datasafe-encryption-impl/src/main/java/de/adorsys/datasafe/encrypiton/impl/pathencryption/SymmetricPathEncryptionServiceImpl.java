package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import com.google.common.primitives.Ints;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PathEncryptionSecretKey;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.dto.PathSegmentWithSecretKeyWith;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.adorsys.datasafe.types.api.global.PathEncryptionId.AES_SIV;

/**
 * Path encryption service that maintains URI segments integrity.
 * It means that path/to/file is encrypted to cipher(path)/cipher(to)/cipher(file) and each invocation of example:
 * cipher(path) will yield same string, but cipher(path)/cipher(path) will not yield same segments -
 * it will be more like abc/cde and not like abc/abc.
 */
@Slf4j
@RuntimeDelegate
public class SymmetricPathEncryptionServiceImpl implements SymmetricPathEncryptionService {

    private static final int DOT_SLASH_PREFIX_LENGTH = 2;
    private static final String DOT_SLASH_PREFIX = "./";
    private static final String PATH_SEPARATOR = "/";

    private final Function<PathSegmentWithSecretKeyWith, String> encryptAndEncode;
    private final Function<PathSegmentWithSecretKeyWith, String> decryptAndDecode;

    @Inject
    public SymmetricPathEncryptionServiceImpl(PathEncryptorDecryptor pathEncryptorDecryptor) {
        encryptAndEncode = keyEntryEncryptedDataPair ->
                encode(
                        pathEncryptorDecryptor.encrypt(
                                keyEntryEncryptedDataPair.getPathEncryptionSecretKey(),
                                keyEntryEncryptedDataPair.getPath().getBytes(StandardCharsets.UTF_8),
                                Ints.toByteArray(keyEntryEncryptedDataPair.getAuthenticationPosition())
                        )
                );

        decryptAndDecode = keyEntryDecryptedDataPair ->
                new String(
                        pathEncryptorDecryptor.decrypt(
                                keyEntryDecryptedDataPair.getPathEncryptionSecretKey(),
                                decode(keyEntryDecryptedDataPair.getPath()),
                                Ints.toByteArray(keyEntryDecryptedDataPair.getAuthenticationPosition())
                        ),
                        StandardCharsets.UTF_8
                );
    }

    /**
     * Encrypts each URI segment separately and composes them back in same order.
     */
    @Override
    @SneakyThrows
    public Uri encrypt(PathEncryptionSecretKey pathEncryptionSecretKey, Uri bucketPath) {
        validateArgs(pathEncryptionSecretKey, bucketPath);
        validateUriIsRelative(bucketPath);

        Uri uri = processURIparts(
                pathEncryptionSecretKey,
                bucketPath,
                encryptAndEncode
        );

        return AES_SIV.asUriRoot().resolve(uri);
    }

    /**
     * Decrypts each URI segment separately and composes them back in same order.
     */
    @Override
    @SneakyThrows
    public Uri decrypt(PathEncryptionSecretKey pathEncryptionSecretKey, Uri bucketPath) {
        validateArgs(pathEncryptionSecretKey, bucketPath);
        validateUriIsRelative(bucketPath);

        bucketPath = AES_SIV.asUriRoot().relativize(bucketPath);

        return processURIparts(
                pathEncryptionSecretKey,
                bucketPath,
                decryptAndDecode
        );
    }

    @SneakyThrows
    private static byte[] decode(String encryptedPath) {
        if (null == encryptedPath || encryptedPath.isEmpty()) {
            return null;
        }

        return Base64.getUrlDecoder().decode(encryptedPath);
    }

    @SneakyThrows
    private static String encode(byte[] encryptedPath) {
        if (null == encryptedPath) {
            return null;
        }

        return Base64.getUrlEncoder().encodeToString(encryptedPath);
    }

    private static Uri processURIparts(
            PathEncryptionSecretKey secretKeyEntry,
            Uri bucketPath,
            Function<PathSegmentWithSecretKeyWith, String> process) {
        StringBuilder result = new StringBuilder();

        String path = bucketPath.getRawPath();
        if (bucketPath.getRawPath().startsWith(DOT_SLASH_PREFIX)) {
            result.append(DOT_SLASH_PREFIX);
            path = bucketPath.getRawPath().substring(DOT_SLASH_PREFIX_LENGTH);
        }

        if (path.isEmpty()) {
            return new Uri(result.toString());
        }

        String[] segments = path.split(PATH_SEPARATOR);

        return new Uri(URI.create(processSegments(secretKeyEntry, process, segments)));
    }

    private static String processSegments(PathEncryptionSecretKey secretKeyEntry,
                                          Function<PathSegmentWithSecretKeyWith, String> process,
                                          String[] segments) {
        return IntStream.range(0, segments.length)
                .boxed()
                .map(position ->
                        process.apply(
                                new PathSegmentWithSecretKeyWith(
                                        secretKeyEntry,
                                        position,
                                        segments[position]
                                ))
                ).collect(Collectors.joining(PATH_SEPARATOR));
    }

    private static void validateArgs(PathEncryptionSecretKey secretKeyEntry, Uri bucketPath) {
        if (null == secretKeyEntry) {
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
