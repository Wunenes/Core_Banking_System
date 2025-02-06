package com.bankingSystem.generators;

import java.math.BigDecimal;
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
                                                BigDecimal amount) throws NoSuchAlgorithmException {
        LocalDateTime time = LocalDateTime.now();
        String timeStr = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String compositeData = senderAccountNumber + recipientAccountNumber + amount.toString() + timeStr;
        MessageDigest digest = MessageDigest.getInstance("SHA_256");
        byte[] hashedBytes = digest.digest(compositeData.getBytes(StandardCharsets.UTF_8));
        StringBuilder hashedName = new StringBuilder();
        for (byte b: hashedBytes) {
            hashedName.append(String.format("%02x", b));
        }

        UUID uuid = UUID.randomUUID();
        UUID compositeId = UUID.nameUUIDFromBytes((hashedName + uuid.toString()).getBytes());
        String shortenedUuid = compositeId.toString().toUpperCase().replace("-", "").substring(0, 4);
        StringBuilder newUuid = new StringBuilder();
        for (char letter : shortenedUuid.toCharArray()) {
            newUuid.append(letterToNumber(letter));
        }
        String intToken = checkSumAlgorithm(newUuid);
        return type + intToken;
    }
}
