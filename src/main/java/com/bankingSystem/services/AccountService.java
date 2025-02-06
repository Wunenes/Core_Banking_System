package com.bankingSystem.services;

import com.bankingSystem.models.Account;
import com.bankingSystem.repositories.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService extends Account {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    @Autowired
    AccountRepository accountRepository;
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
    public Optional<Account> getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    public static String accountNumberGenerator(String name, String userId) {
        try {
            LocalDateTime time = LocalDateTime.now();
            String timeStr = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String var = name + timeStr + userId;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(var.getBytes(StandardCharsets.UTF_8));
            StringBuilder hashedName = new StringBuilder();
            for (byte b : hashBytes) {
                hashedName.append(String.format("%02x", b));
            }

            UUID uuid = UUID.randomUUID();
            UUID customUuid = UUID.nameUUIDFromBytes((hashedName + uuid.toString()).getBytes());
            String shortenedUuid = customUuid.toString().replace("-", "").substring(0, 8);

            StringBuilder newUuid = new StringBuilder();
            for (char letter : shortenedUuid.toCharArray()) {
                newUuid.append(letterToNumber(letter));
            }

            return checkSumAlgorithm(newUuid);

        } catch (Exception e) {
            log.error("e: ", e);
        }
        return null;
    }

    private static String checkSumAlgorithm(StringBuilder newUuid) {
        String finalUuid = newUuid.substring(0, 8);
        String stringPlaceholder = finalUuid + "00";
        int modulo = Integer.parseInt(stringPlaceholder) % 42;
        int checkDigits = (43 - modulo);
        String finalDigits = String.format("%02d", checkDigits);

        return finalUuid + finalDigits;
    }

    public static int letterToNumber(char letter) {
        letter = Character.toUpperCase(letter);
        return letter - 'A' + 17;
    }
    public String createAccount(Account account) {
        try {
            account.setAccountNumber(accountNumberGenerator(account.getHolderName(), account.getIdNumber()));
            accountRepository.save(account);
            return "Account successfully created";
        } catch (DataAccessException e) {
            return "Sorry! Account already exists";
        } catch (Error error) {
            return "Apologies! Cannot make an account at the moment";
        }
    }
    @Transactional
    public String deleteAccount(String accountNumber) {
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);
        if (account.isPresent()) {
            accountRepository.deleteByAccountNumber(accountNumber);
            return "Account deleted successfully.";
        } else {
            return "Account not found.";
        }
    }
}
