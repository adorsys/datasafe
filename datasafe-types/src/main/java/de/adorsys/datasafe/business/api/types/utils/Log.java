package de.adorsys.datasafe.business.api.types.utils;

import lombok.SneakyThrows;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class Log {
    private static String secureLogs = System.getProperty("SECURE_LOGS");
    private static MessageDigest digest = getDigest();
    private static Base64.Encoder encoder = Base64.getEncoder();

    @SneakyThrows
    public static <T> String secure(Iterable<T> values, String delim) {
        StringBuilder sb = new StringBuilder();
        values.forEach(v -> sb.append(secure(v)).append(delim));
        return sb.toString();
    }

    @SneakyThrows
    public static String secure(Path path) {
        StringBuilder sb = new StringBuilder();
        path.forEach(v -> sb.append(secure(v.toString())).append("/"));
        return sb.toString();
    }

    @SneakyThrows
    public static String secure(URI uri) {
        return secure(uri.getPath().split("/"), "/");
    }

    @SneakyThrows
    public static String secure(Object[] values, String delim) {
        return secure(Arrays.asList(values), delim);
    }

    @SneakyThrows
    public static String secure(Object value) {
        if (value == null) return null;

        String s = value.toString();
        if ("0".equals(secureLogs) || "false".equalsIgnoreCase(secureLogs) || "off".equalsIgnoreCase(secureLogs)) {
            return s;
        }

        if ("stars".equalsIgnoreCase(secureLogs)) {
            if (s.length() <= 4) {
                return "****";
            }
            return s.substring(0, 2) + "****" + s.substring(s.length() - 2);
        }

        // by default return hash
        byte[] originalBytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] hash = digest.digest(originalBytes);
        return encoder.encodeToString(hash);
    }

    @SneakyThrows
    private static MessageDigest getDigest() {
        return MessageDigest.getInstance("SHA-256");
    }
}
