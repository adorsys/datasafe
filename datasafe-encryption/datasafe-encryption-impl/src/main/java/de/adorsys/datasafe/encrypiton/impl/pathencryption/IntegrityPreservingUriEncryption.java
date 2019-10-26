package de.adorsys.datasafe.encrypiton.impl.pathencryption;

import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.keystore.AuthPathEncryptionSecretKey;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.dto.PathSegmentWithSecretKeyWith;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.types.api.global.PathEncryptionId.AES_SIV;

/**
 * Path encryption service that maintains URI segments integrity.
 * It means that path/to/file is encrypted to cipher(path)/cipher(to)/cipher(file) and each invocation of example:
 * cipher(path) will yield same string, but cipher(path)/cipher(path) will not yield same segments -
 * it will be more like abc/cde and not like abc/abc.
 * Additionally each segment is authenticated against its parent path hash, so attacker can't
 * move a/file to b/file without being detected.
 */
@Slf4j
@RuntimeDelegate
public class IntegrityPreservingUriEncryption implements SymmetricPathEncryptionService {

    private static final int DOT_SLASH_PREFIX_LENGTH = 2;
    private static final String DOT_SLASH_PREFIX = "./";
    private static final String PATH_SEPARATOR = "/";

    private final Function<PathSegmentWithSecretKeyWith, String> encryptAndEncode;
    private final Function<PathSegmentWithSecretKeyWith, String> decryptAndDecode;

    @Inject
    public IntegrityPreservingUriEncryption(PathEncryptorDecryptor pathEncryptorDecryptor) {
        encryptAndEncode = keyAndSegment -> encryptorAndEncoder(keyAndSegment, pathEncryptorDecryptor);
        decryptAndDecode = keyAndSegment -> decryptorAndDecoder(keyAndSegment, pathEncryptorDecryptor);
    }

    /**
     * Encrypts each URI segment separately and composes them back in same order.
     */
    @Override
    @SneakyThrows
    public Uri encrypt(AuthPathEncryptionSecretKey pathEncryptionSecretKey, Uri bucketPath) {
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
    public Uri decrypt(AuthPathEncryptionSecretKey pathEncryptionSecretKey, Uri bucketPath) {
        validateArgs(pathEncryptionSecretKey, bucketPath);
        validateUriIsRelative(bucketPath);

        bucketPath = AES_SIV.asUriRoot().relativize(bucketPath);

        return processURIparts(
                pathEncryptionSecretKey,
                bucketPath,
                decryptAndDecode
        );
    }

    /**
     * Parent path authentication digest. a/b/c - each path segment on encryption will be authenticated:
     * for a - will be authenticated against ``
     * for b - will be authenticated against `/encrypted(a)`
     * for c - will be authenticated against `/encrypted(a)/encrypted(b)`
     */
    @SneakyThrows
    protected MessageDigest getDigest() {
        return MessageDigest.getInstance("SHA-256");
    }

    protected String decryptorAndDecoder(PathSegmentWithSecretKeyWith keyAndSegment,
                                         PathEncryptorDecryptor pathEncryptorDecryptor) {
            byte[] segment = keyAndSegment.getPath().getBytes(StandardCharsets.UTF_8);
            keyAndSegment.getDigest().update(segment);

            return new String(
                    pathEncryptorDecryptor.decrypt(
                            keyAndSegment.getPathEncryptionSecretKey(),
                            decode(keyAndSegment.getPath()),
                            keyAndSegment.getParentHash()
                    ),
                    StandardCharsets.UTF_8
            );
    }

    protected String encryptorAndEncoder(PathSegmentWithSecretKeyWith keyAndSegment,
                                         PathEncryptorDecryptor pathEncryptorDecryptor) {
        String result = encode(
                pathEncryptorDecryptor.encrypt(
                        keyAndSegment.getPathEncryptionSecretKey(),
                        keyAndSegment.getPath().getBytes(StandardCharsets.UTF_8),
                        keyAndSegment.getParentHash()
                )
        );

        keyAndSegment.getDigest().update(result.getBytes(StandardCharsets.UTF_8));
        return result;
    }

    @SneakyThrows
    private byte[] decode(String encryptedPath) {
        if (null == encryptedPath || encryptedPath.isEmpty()) {
            return null;
        }

        return Base64.getUrlDecoder().decode(encryptedPath);
    }

    @SneakyThrows
    private String encode(byte[] encryptedPath) {
        if (null == encryptedPath) {
            return null;
        }

        return Base64.getUrlEncoder().encodeToString(encryptedPath);
    }

    private Uri processURIparts(
            AuthPathEncryptionSecretKey secretKeyEntry,
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

    private String processSegments(AuthPathEncryptionSecretKey secretKeyEntry,
                                          Function<PathSegmentWithSecretKeyWith, String> process,
                                          String[] segments) {
        MessageDigest digest = getDigest();
        digest.update(PATH_SEPARATOR.getBytes(StandardCharsets.UTF_8));

        return Arrays.stream(segments)
                .map(it -> processAndAuthenticateSegment(it, secretKeyEntry, process, digest))
                .collect(Collectors.joining(PATH_SEPARATOR));
    }

    @SneakyThrows
    private String processAndAuthenticateSegment(
            String segment,
            AuthPathEncryptionSecretKey secretKeyEntry,
            Function<PathSegmentWithSecretKeyWith, String> process,
            MessageDigest digest) {
        MessageDigest currentDigest = (MessageDigest) digest.clone();
        return process.apply(
                new PathSegmentWithSecretKeyWith(
                        digest,
                        currentDigest.digest(),
                        secretKeyEntry,
                        segment
                )
        );
    }

    private static void validateArgs(AuthPathEncryptionSecretKey secretKeyEntry, Uri bucketPath) {
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
