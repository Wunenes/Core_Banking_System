package com.bankingSystem.services;

import com.bankingSystem.controllers.AccountController;
import com.bankingSystem.models.Account;
import com.bankingSystem.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import static com.bankingSystem.generators.AccountNumberGenerator.accountNumberGenerator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService extends Account {
    @Autowired
    AccountRepository accountRepository;
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    public String createAccount(Account account) {
        try {
            account.setAccountNumber(accountNumberGenerator(account.getAccountType(), account.getCurrencyType()));
            accountRepository.save(account);
            return account.getAccountNumber();
        } catch (Error e) {
            return e.getLocalizedMessage();
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

    public static class AccountResponseDTO {
        private String type;
        private String status;
        private BigDecimal balance;
        private String currencyType;
        public AccountResponseDTO(String type, String status, BigDecimal balance, String currencyType) {
            this.type = type;
            this.status = status;
            this.balance = balance;
            this.currencyType = currencyType;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        public BigDecimal getBalance() {
            return balance;
        }
        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
        public String getCurrencyType() {
            return currencyType;
        }
        public void setCurrencyType(String currencyType){
            this.currencyType = currencyType;
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
                    .body(new AccountResponseDTO("", "", BigDecimal.valueOf(0.0), ""));
        }
    }

    static AccountResponseDTO getAccountResponseDTO(Account userAccount) {
        AccountResponseDTO response = new AccountResponseDTO(userAccount.getAccountType(),
                userAccount.getStatus(), userAccount.getBalance(), userAccount.getCurrencyType());
        response.setType(userAccount.getAccountType());
        response.setStatus(userAccount.getStatus());
        response.setBalance(userAccount.getBalance());
        response.setCurrencyType(userAccount.getCurrencyType());
        return response;
    }
}
