package com.bankingsystem.controllers;

import com.bankingsystem.models.Account;
import com.bankingsystem.services.AccountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")

public class AccountController {
    final AccountService accountService;
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    @PostMapping("/createaccount")
    public String createAccount (@RequestBody Account accountDetails) {
        return accountService.createAccount(accountDetails);
    }
    @DeleteMapping("/deleteaccount/{accountNumber}")
    public String deleteAccount(@PathVariable String accountNumber) {
        return accountService.deleteAccount(accountNumber);
    }
}