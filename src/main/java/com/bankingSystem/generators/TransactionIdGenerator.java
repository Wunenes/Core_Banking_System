package com.bankingSystem.generators;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.bankingSystem.generators.AccountNumberGenerator.checkSumAlgorithm;
import static com.bankingSystem.generators.AccountNumberGenerator.letterToNumber;

public class TransactionIdGenerator {
    public static String transactionIdGenerator(String senderAccountNumber, String recipientAccountNumber, String type,
                                                double amount, String randomChars) throws NoSuchAlgorithmException {
        LocalDateTime time = LocalDateTime.now();
        String timeStr = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String compositeData = senderAccountNumber + recipientAccountNumber + amount + timeStr;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(compositeData.getBytes(StandardCharsets.UTF_8));
        StringBuilder hashedName = new StringBuilder();
        for (byte b: hashedBytes) {
            hashedName.append(String.format("%02x", b));
        }

        UUID uuid = UUID.randomUUID();
        UUID hashedCompositeId = UUID.nameUUIDFromBytes((hashedName + uuid.toString()).getBytes());
        String shortenedUuid = hashedCompositeId.toString().replace("-", "").substring(0, 2);
        StringBuilder newUuid = new StringBuilder();
        for (char letter : shortenedUuid.toCharArray()) {
            newUuid.append(letterToNumber(letter));
        }
        String intToken = newUuid + checkSumAlgorithm(newUuid);
        return type + intToken + randomChars;
    }
}
