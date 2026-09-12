package com.benzjeremy.benzstore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class HashUtil {

    public static String calculateSha256(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean verifySha256(File file, String expectedSha256) {
        if (expectedSha256 == null || expectedSha256.isEmpty()) {
            return false;
        }
        String actual = calculateSha256(file);
        return actual.equalsIgnoreCase(expectedSha256);
    }
}
