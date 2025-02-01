package com.bankingsystem.controllers;

import com.bankingsystem.services.TransactionService;
import com.bankingsystem.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@RequestParam String accountNumber, @RequestParam double amount) {
        return ResponseEntity.ok(transactionService.deposit(accountNumber, amount));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transfer(
            @RequestParam String senderAccount,
            @RequestParam String receiverAccount,
            @RequestParam double amount) {
        return ResponseEntity.ok(transactionService.internalTransfer(senderAccount, receiverAccount, amount));
    }
}