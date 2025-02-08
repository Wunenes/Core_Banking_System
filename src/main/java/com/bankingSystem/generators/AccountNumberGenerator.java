package com.bankingSystem.generators;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;

public class AccountNumberGenerator {
    private static final SecureRandom random = new SecureRandom();
    public static String accountNumberGenerator(String name, String userId) {
        String userHash = hashUserDetails(name + userId);
        String uniquePart = userHash.substring(0, 6);
        int randomComponent = random.nextInt(900) + 100;
        String rawAccount = uniquePart + randomComponent;
        return rawAccount + checkSumAlgorithm(new StringBuilder(rawAccount));
    }
    private static String hashUserDetails(String userDetails) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(String.valueOf(userDetails).getBytes(StandardCharsets.UTF_8));
            StringBuilder numericHash = new StringBuilder();
            for (byte b : hash) {
                numericHash.append(String.format("%02d", (b & 0xFF) % 100));
            }
            return numericHash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating user hash", e);
        }
    }
    public static String checkSumAlgorithm(StringBuilder rawId){
        String rawIdString = rawId.toString();
        String stringPlaceholder = rawIdString + "00";
        long modulo = Long.parseLong(stringPlaceholder) % 42;
        long checkDigits = (43 - modulo);
        return String.format("%02d", checkDigits);
    }
}
