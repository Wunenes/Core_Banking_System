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
    @PostMapping("/createAccount")
    public String createAccount (@RequestBody Account accountDetails) {
        return accountService.createAccount(accountDetails);
    }
}