package com.bankingSystem.controllers;

import com.bankingSystem.models.Users;
import com.bankingSystem.services.AccountService;
import com.bankingSystem.services.TransactionService;
import com.bankingSystem.services.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
public class UsersController {
    final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping("/create")
    public String createUser(@RequestBody Users userDetails) {
        return usersService.createUser(userDetails);
    }

    @GetMapping("/get/accounts")
    public ResponseEntity<List<AccountService.AccountResponseDTO>> getUserAccountsDetails(@RequestParam String email) {
        return usersService.getUserAccountsDetails(email);
    }

    @GetMapping("/get/transactions")
    public ArrayList<List<TransactionService.TransactionResponseDTO>> getTransactionDetails(@RequestParam String email) {
        return usersService.getTransactions(email);
    }
}