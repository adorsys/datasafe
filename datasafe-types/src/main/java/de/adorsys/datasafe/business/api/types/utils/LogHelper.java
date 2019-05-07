package de.adorsys.datasafe.business.api.types.utils;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class LogHelper {
    private static String encryptLogs = System.getProperty("ENCRYPT_LOGS");

    @SneakyThrows
    public static String encryptIdNeeded(Object value) {
        if ("0".equals(encryptLogs) || "false".equalsIgnoreCase(encryptLogs)) return value.toString();

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] originalBytes = value.toString().getBytes(StandardCharsets.UTF_8);
        byte[] hash = messageDigest.digest(originalBytes);
        return Base64.getEncoder().encodeToString(hash);
    }
}
