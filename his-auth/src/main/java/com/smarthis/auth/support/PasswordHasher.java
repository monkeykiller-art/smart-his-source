package com.smarthis.auth.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class PasswordHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {}

    public static String hash(String rawPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltHex = HexFormat.of().formatHex(salt);
        String hashHex = sha256(saltHex + rawPassword);
        return "$sha256$" + saltHex + "$" + hashHex;
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || !encodedPassword.startsWith("$sha256$")) {
            return false;
        }
        String[] parts = encodedPassword.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        String salt = parts[2];
        String expected = parts[3];
        String actual = sha256(salt + rawPassword);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
