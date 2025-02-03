package com.bankingsystem.controllers;

import com.bankingsystem.models.Account;
import com.bankingsystem.services.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")

public class AccountController {
    final AccountService accountService;
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    @PostMapping("/create")
    public String createAccount (@RequestBody Account accountDetails) {
        return accountService.createAccount(accountDetails);
    }
    @DeleteMapping("/delete/{accountNumber}")
    public String deleteAccount(@PathVariable String accountNumber) {
        return accountService.deleteAccount(accountNumber);
    }

    public static class AccountResponseDTO {
        private String name;
        private String email;
        private BigDecimal balance;
        private String idNumber;
        private String phoneNumber;
        public AccountResponseDTO(String name, String email, BigDecimal balance, String idNumber
                                  , String phoneNumber) {
            this.name = name;
            this.email = email;
            this.balance = balance;
            this.idNumber = idNumber;
            this.phoneNumber = phoneNumber;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public BigDecimal getBalance() {
            return balance;
        }
        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
        public String getPhoneNumber() {
            return phoneNumber;
        }
        public void setPhoneNumber(String phoneNumber){
            this.phoneNumber = phoneNumber;
        }
        public String getIdNumber() {
            return idNumber;
        }
        public void setIdNumber(String ID){
            this.idNumber = ID;
        }
    }
    @GetMapping("/get/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> getAccount(@PathVariable String accountNumber) {
        Optional<Account> account = accountService.getByAccountNumber(accountNumber);
        if (account.isPresent()) {
            Account userAccount = account.get();

            AccountResponseDTO response = getAccountResponseDTO(userAccount);

            return ResponseEntity.ok(response);

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AccountResponseDTO("", "", BigDecimal.valueOf(0.0), "",""));
        }
    }

    private static AccountResponseDTO getAccountResponseDTO(Account userAccount) {
        AccountResponseDTO response = new AccountResponseDTO(userAccount.getHolderName(),
                userAccount.getEmail(), userAccount.getBalance(), userAccount.getIdNumber(),
                userAccount.getPhoneNumber());
        response.setName(userAccount.getHolderName());
        response.setEmail(userAccount.getEmail());
        response.setBalance(userAccount.getBalance());
        response.setIdNumber(userAccount.getIdNumber());
        response.setPhoneNumber(userAccount.getPhoneNumber());
        return response;
    }
}