package com.capgemini.assessment.petclinic.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Cryptographic hashing utility for computing deterministic SHA-256 checksums of integration payloads.
 */
public final class HashUtils {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private HashUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Computes a lowercase hexadecimal SHA-256 digest for the given UTF-8 string content.
     *
     * @param input raw string payload to hash
     * @return 64-character hexadecimal SHA-256 checksum
     */
    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available in JVM", exception);
        }
    }
}
