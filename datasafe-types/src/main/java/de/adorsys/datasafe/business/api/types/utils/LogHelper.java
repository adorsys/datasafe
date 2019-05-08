package de.adorsys.datasafe.business.api.types.utils;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class LogHelper {
    private static String secureLogs = System.getProperty("SECURE_LOGS");

    @SneakyThrows
    public static String secure(Object value) {
        String s = value.toString();
        if ("0".equals(secureLogs) || "false".equalsIgnoreCase(secureLogs) || "off".equalsIgnoreCase(secureLogs)) {
            return s;
        }

        if ("stars".equalsIgnoreCase(secureLogs)) {
            if (s.length() <= 4) {
                return "****";
            }
            return s.substring(0, 2) + "****" + s.substring(s.length()-2);
        }

        // by default return hash
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] originalBytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] hash = messageDigest.digest(originalBytes);
        return Base64.getEncoder().encodeToString(hash);
    }
}
