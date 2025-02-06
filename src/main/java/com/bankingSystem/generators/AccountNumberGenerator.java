package com.bankingSystem.generators;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import com.bankingSystem.services.AccountService;

public class AccountNumberGenerator {
    public static String accountNumberGenerator(String name, String userId) {
        try {
            LocalDateTime time = LocalDateTime.now();
            String timeStr = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String composite = name + timeStr + userId;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(composite.getBytes(StandardCharsets.UTF_8));
            StringBuilder hashedName = new StringBuilder();
            for (byte b : hashBytes) {
                hashedName.append(String.format("%02x", b));
            }

            UUID uuid = UUID.randomUUID();
            UUID compositeId = UUID.nameUUIDFromBytes((hashedName + uuid.toString()).getBytes());
            String shortenedUuid = compositeId.toString().replace("-", "").substring(0, 8);

            StringBuilder newUuid = new StringBuilder();
            for (char letter : shortenedUuid.toCharArray()) {
                newUuid.append(letterToNumber(letter));
            }

            return checkSumAlgorithm(newUuid);

        } catch (Exception e) {
            AccountService.log.error("e: ", e);
        }
        return null;
    }
    static String checkSumAlgorithm(StringBuilder newUuid){
        String finalUuid = newUuid.toString();
        String stringPlaceholder = finalUuid + "00";
        int modulo = Integer.parseInt(stringPlaceholder) % 42;
        int  checkDigits = (43 - modulo);
        final String finalDigits = String.format("%02d", checkDigits);
        return finalUuid + finalDigits;
    }

    public static int letterToNumber(char letter) {
        letter = Character.toUpperCase(letter);
        return letter - 'A' + 17;
    }
}
