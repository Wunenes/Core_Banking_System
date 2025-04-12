package com.bankingSystem.controllers;

import com.bankingSystem.exceptions.UserNotFoundException;
import com.bankingSystem.models.Users;
import com.bankingSystem.services.AccountService;
import com.bankingSystem.services.TransactionService;
import com.bankingSystem.services.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
public class UsersController {
    final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam String email) throws UserNotFoundException {
        Users user = usersService.getByEmail(email)
                .orElseThrow(()-> new UserNotFoundException("User not found", "Criteria: Email: ", email));

        return usersService.deleteUser(user.getUserId());
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
    public ResponseEntity<List<List<TransactionService.TransactionResponseDTO>>> getTransactionDetails(@RequestParam String email) {
        return usersService.getTransactions(email);
    }
}