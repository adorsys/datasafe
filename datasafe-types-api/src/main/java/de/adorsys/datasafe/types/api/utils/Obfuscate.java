package de.adorsys.datasafe.types.api.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class hides sensitive information that may appear in logs. While hiding information it preserves the capability
 * to find the same words in logs - that is useful for debugging.
 * How it obscures sensitive information depends on SECURE_LOGS system property:
 * <ul>
 * <li>SECURE_LOGS=off,0,false - nothing is obscured</li>
 * <li>SECURE_LOGS=stars - only first 2 symbols and last 2 symbols, example: password - pa****rd, cat - ****</li>
 * <li>all other values of SECURE_LOGS - input content will be hashed with SHA-256</li>
 * </ul>
 * For highly sensitive information there is additional system property SECURE_SENSITIVE:
 * <ul>
 * <li>SECURE_SENSITIVE=off,0,false AND SECURE_LOGS=off,0,false - passwords are not obscured in logs</li>
 * <li>SECURE_SENSITIVE=hash - first 4 chars of SHA-256 hashed value is logged</li>
 * <li>all other values yield string with stars</li>
 * <ul>
 */
@UtilityClass
public class Obfuscate {

    static String secureLogs = System.getProperty("SECURE_LOGS");
    static String secureSensitive = System.getProperty("SECURE_SENSITIVE");
    private static Base64.Encoder encoder = Base64.getUrlEncoder(); // thread-safe class

    /**
     * By default, protects moderately sensitive data, but preserves delimiters in it.
     * @param value String with delimiters to obfuscate
     * @param delim Delimiters to split on and preserve
     * I.e. a/b/c with {@code delim} equal to "/" will create sha(a)/sha(b)/sha(c)
     */
    public static String secure(String value, String delim) {
        if (null == value) {
            return null;
        }

        return Stream.of(value.split("/", -1))
                .map(it -> it.isEmpty() ? it : secure(it))
                .collect(Collectors.joining(delim));
    }

    /**
     * By default, protects moderately sensitive data, but allows to log it using SECURE_LOGS property.
     * @param value Its toString() result will get encrypted.
     * @return Secured string value that is safe to log.
     */
    public static String secure(String value) {
        if (value == null) {
            return null;
        }

        if (isDisabled(secureLogs)) {
            return value;
        }

        if ("stars".equalsIgnoreCase(secureLogs)) {
            if (value.length() <= 4) {
                return "****";
            }
            return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
        }

        // by default return hash
        return computeSha(value);
    }

    /**
     * Method has slightly other name as secureSensitive,
     * because is call sometimes with null and thus can not
     * be clearly assigned.
     *
     * By default, protects highly sensitive data, but allows to log it using SECURE_SENSITIVE property.
     * @param value Its toString() result will get encrypted.
     * @return Secured string value that is safe to log.
     *
     */
    public static String secureSensitiveChar(char[] value) {
        if (value == null) {
            return null;
        }

        if (isDisabled(secureLogs) && isDisabled(secureSensitive)) {
            return new String(value);
        }

        if ("hash".equalsIgnoreCase(secureSensitive)) {
            return "hash:" + computeShaChar(value).substring(0, 4);
        }

        return "****";
    }

    /**
     * By default, protects highly sensitive data, but allows to log it using SECURE_SENSITIVE property.
     * @param value Its toString() result will get encrypted.
     * @return Secured string value that is safe to log.
     */
    public static String secureSensitive(String value) {
        if (value == null) {
            return null;
        }

        if (isDisabled(secureLogs) && isDisabled(secureSensitive)) {
            return value;
        }

        if ("hash".equalsIgnoreCase(secureSensitive)) {
            return "hash:" + computeSha(value).substring(0, 4);
        }

        return "****";
    }

    private static String computeSha(String s) {
        byte[] originalBytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] hash = getDigest().digest(originalBytes);
        return encoder.encodeToString(hash);
    }

    private static String computeShaChar(char[] s) {
        byte[] originalBytes = new byte[s.length];
        for (int i = 0; i < s.length; i++) {
            originalBytes[i] = (byte) s[i];
        }
        byte[] hash = getDigest().digest(originalBytes);
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
