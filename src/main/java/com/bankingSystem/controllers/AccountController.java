package com.bankingSystem.controllers;

import com.bankingSystem.models.Account;
import com.bankingSystem.services.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")

public class AccountController {
    final AccountService accountService;
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    @PostMapping("/create")
    public String createAccount (@RequestBody Account accountDetails) {
        return accountService.createAccount(accountDetails).getAccountNumber();
    }
    @DeleteMapping("/delete")
    public String deleteAccount(@RequestParam String accountNumber) {
        return accountService.deleteAccount(accountNumber);
    }
    @GetMapping("/get")
    public ResponseEntity<AccountService.AccountResponseDTO> getAccount(@RequestParam String accountNumber) {
        return accountService.getAccount(accountNumber);
    }
}