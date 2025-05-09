package com.bankingSystem.services;

import com.bankingSystem.models.Account;
import com.bankingSystem.repositories.AccountRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import static com.bankingSystem.generators.AccountNumberGenerator.accountNumberGenerator;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AccountService extends Account {
    @Autowired
    AccountRepository accountRepository;

    public Account createAccount(Account account) {
        account.setAccountNumber(accountNumberGenerator(account.getAccountType(), account.getCurrencyType()));
        accountRepository.save(account);
        return account;
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

    @Setter
    @Getter
    public static class AccountResponseDTO {
        private String type;
        private String status;
        private BigDecimal balance;
        private String currencyType;
        private String accountNumber;
        public AccountResponseDTO(String type, String status, BigDecimal balance, String currencyType, String accountNumber) {
            this.type = type;
            this.status = status;
            this.balance = balance;
            this.currencyType = currencyType;
            this.accountNumber = accountNumber;
        }

    }
    public ResponseEntity<AccountResponseDTO> getAccount(@RequestParam String accountNumber) {
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);
        if (account.isPresent()) {
            Account userAccount = account.get();

            AccountResponseDTO response = getAccountResponseDTO(userAccount);

            return ResponseEntity.ok(response);

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AccountResponseDTO("", "", BigDecimal.valueOf(0.0), "", ""));
        }
    }

    static AccountResponseDTO getAccountResponseDTO(Account userAccount) {
        AccountResponseDTO response = new AccountResponseDTO(userAccount.getAccountType(),
                userAccount.getStatus(), userAccount.getBalance(), userAccount.getCurrencyType(), userAccount.getAccountNumber());
        response.setType(userAccount.getAccountType());
        response.setStatus(userAccount.getStatus());
        response.setBalance(userAccount.getBalance());
        response.setCurrencyType(userAccount.getCurrencyType());
        response.setAccountNumber(userAccount.getAccountNumber());
        return response;
    }
}
