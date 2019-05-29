package de.adorsys.datasafe.types.api.utils;

import de.adorsys.datasafe.types.api.resource.ResourceLocation;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * This class hides sensitive information that may appear in logs. While hiding information it preserves the capability
 * to find the same words in logs - that is useful for debugging.
 * How it obscures sensitive information depends on SECURE_LOGS system property:
 * SECURE_LOGS=off,0,false - nothing is obscured
 * SECURE_LOGS=stars - only first 2 symbols and last 2 symbols, i.e. password -> pa****rd, cat -> ****
 * all other values of SECURE_LOGS - input content will be hashed with SHA-256
 * For highly sensitive information there is additional flag SECURE_SENSITIVE:
 * SECURE_SENSITIVE=off,0,false AND SECURE_LOGS=off,0,false - passwords are not obscured in logs
 * SECURE_SENSITIVE=hash - first 4 chars of SHA-256 hashed value is logged
 * all other values yield string with stars
 */
@UtilityClass
public class Log {

    static String secureLogs = System.getProperty("SECURE_LOGS");
    static String secureSensitive = System.getProperty("SECURE_SENSITIVE");
    private static MessageDigest digest = getDigest();
    private static Base64.Encoder encoder = Base64.getEncoder();

    /**
     * By default, protects moderately sensitive data, but allows to log it using SECURE_LOGS property.
     */
    @SneakyThrows
    public static <T> String secure(Iterable<T> values, String delim) {
        if (values == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        boolean isStarted = false;
        for (T value : values) {
            if (isStarted) {
                sb.append(delim);
            }

            if (null == value || "".equals(value.toString())) {
                continue;
            } else {
                sb.append(secure(value));
            }

            isStarted = true;
        }

        return sb.toString();
    }

    /**
     * By default, protects moderately sensitive data, but allows to log it using SECURE_LOGS property.
     */
    public static String secure(Path path) {
        if (path == null) {
            return null;
        }

        return secure(path.toUri());
    }

    /**
     * By default, protects moderately sensitive data, but allows to log it using SECURE_LOGS property.
     */
    public static String secure(Uri uri) {
        if (uri == null) {
            return null;
        }

        return secure(uri.getWrapped());
    }

    /**
     * By default, protects moderately sensitive data, but allows to log it using SECURE_LOGS property.
     */
    public static String secure(URI uri) {
        if (uri == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (null != uri.getScheme()) {
            sb.append(uri.getScheme()).append("://");
        }

        if (null != uri.getHost()) {
            sb.append(uri.getHost());
        }

        if (null != uri.getPath()) {
            sb.append(uri.getPath());
        }

        return secure(sb.toString().split("/", -1), "/");
    }

    /**
     * By default, protects moderately sensitive data, but allows to log it using SECURE_LOGS property.
     */
    public static <T extends ResourceLocation> String secure(T resource) {
        return secure(resource.location());
    }

    /**
     * By default, protects moderately sensitive data, but allows to log it using SECURE_LOGS property.
     */
    public static String secure(Object[] values, String delim) {
        return secure(Arrays.asList(values), delim);
    }

    /**
     * By default, protects moderately sensitive data, but allows to log it using SECURE_LOGS property.
     */
    public static String secure(String value, String delim) {
        if (null == value) {
            return null;
        }

        return secure(value.split(delim), delim);
    }

    /**
     * By default, protects moderately sensitive data, but allows to log it using SECURE_LOGS property.
     */
    public static String secure(Object value) {
        if (value == null) {
            return null;
        }

        String s = value.toString();
        if (isDisabled(secureLogs)) {
            return s;
        }

        if ("stars".equalsIgnoreCase(secureLogs)) {
            if (s.length() <= 4) {
                return "****";
            }
            return s.substring(0, 2) + "****" + s.substring(s.length() - 2);
        }

        // by default return hash
        return computeSha(s);
    }

    /**
     * By default, protects highly sensitive data, but allows to log it using SECURE_SENSITIVE property.
     */
    public static String secureSensitive(Object value) {
        if (value == null) {
            return null;
        }

        String str = value.toString();
        if (isDisabled(secureLogs) && isDisabled(secureSensitive)) {
            return str;
        }

        if ("hash".equalsIgnoreCase(secureSensitive)) {
            return "hash:" + computeSha(str).substring(0, 4);
        }

        return "****";
    }

    private static String computeSha(String s) {
        byte[] originalBytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] hash = digest.digest(originalBytes);
        return encoder.encodeToString(hash);
    }

    private static boolean isDisabled(String value) {
        return "0".equals(value)
                || "false".equalsIgnoreCase(value)
                || "off".equalsIgnoreCase(value);
    }

    @SneakyThrows
    private static MessageDigest getDigest() {
        return MessageDigest.getInstance("SHA-256");
    }
}
