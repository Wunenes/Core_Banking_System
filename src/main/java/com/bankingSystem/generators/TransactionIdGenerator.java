package com.bankingSystem.generators;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static com.bankingSystem.generators.AccountNumberGenerator.checkSumAlgorithm;

public class TransactionIdGenerator {
    private static final SecureRandom random = new SecureRandom();
    public static String transactionIdGenerator(String senderAccountNumber, String recipientAccountNumber, String type,
                                                double amount) throws NoSuchAlgorithmException {

        String details = senderAccountNumber + recipientAccountNumber + amount;
        String hashedDetails = hashDetails(details);
        String uniqueComponent = hashedDetails.substring(0, 4);
        int randomComponent = random.nextInt(90) + 10;
        String rawIntToken = uniqueComponent + randomComponent;
        String finalIntToken = rawIntToken + checkSumAlgorithm(new StringBuilder(rawIntToken));
        return type + finalIntToken + randomChars();
    }

    public static String hashDetails(String rawDetails) throws NoSuchAlgorithmException {
        LocalDateTime time = LocalDateTime.now();
        String timeStr = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String compositeDetails = rawDetails + timeStr;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(compositeDetails.getBytes(StandardCharsets.UTF_8));
        StringBuilder hashedName = new StringBuilder();
        for (byte b: hashedBytes) {
            hashedName.append(String.format("%02x", (b & 0xFF) % 100));
        }
        return hashedName.toString();
    }

    public static String randomChars() {
        Random random = new Random();

        char letter1 = (char) ('A' + random.nextInt(26));
        char letter2 = (char) ('A' + random.nextInt(26));
        char letter3 = (char) ('A' + random.nextInt(26));
        String randomLetters = "" + letter1 + letter2 + letter3;
        char[] charArray = randomLetters.toCharArray();

        StringBuilder charStrings = new StringBuilder();
        for(char letter: charArray) {
            charStrings.append(letterToNumber(letter));
        }

        String finalDigits = checkSumAlgorithm(charStrings);

        return "" + letter1 + letter2 + letter3 + finalDigits;
    }
    public static int letterToNumber(char letter) {
        letter = Character.toUpperCase(letter);
        return letter - 'A' + 17;
    }
}
