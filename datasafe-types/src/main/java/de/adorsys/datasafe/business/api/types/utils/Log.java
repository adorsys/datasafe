package de.adorsys.datasafe.business.api.types.utils;

import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@UtilityClass
public class Log {

    static String secureLogs = System.getProperty("SECURE_LOGS");
    private static MessageDigest digest = getDigest();
    private static Base64.Encoder encoder = Base64.getEncoder();

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

    public static String secure(Path path) {
        if (path == null) {
            return null;
        }

        return secure(path.toUri());
    }

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

    public static <T extends ResourceLocation> String secure(T resource) {
        return secure(resource.location());
    }

    public static String secure(Object[] values, String delim) {
        return secure(Arrays.asList(values), delim);
    }

    public static String secure(Object value) {
        if (value == null) {
            return null;
        }

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
