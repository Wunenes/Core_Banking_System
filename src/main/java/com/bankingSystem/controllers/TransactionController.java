package com.bankingSystem.controllers;

import com.bankingSystem.services.ForexService;
import com.bankingSystem.repositories.TransactionRepository;
import com.bankingSystem.models.Transaction;
import com.bankingSystem.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    com.bankingSystem.services.TransactionService transactionService;
    TransactionRepository transactionRepository;
    ForexService forexService;

    public TransactionController(TransactionRepository transactionRepository, com.bankingSystem.services.TransactionService transactionService, ForexService forexService){
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.forexService = forexService;
    }
    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@RequestParam String accountNumber, @RequestParam double amount) throws NoSuchAlgorithmException {
        return ResponseEntity.ok(transactionService.deposit(accountNumber, amount));
    }
    @PostMapping("/internal_transfer")
    public String transfer(
            @RequestParam String senderAccount,
            @RequestParam String receiverAccount,
            @RequestParam double amount) throws NoSuchAlgorithmException {
        return transactionService.internalTransfer(senderAccount, receiverAccount, amount);
    }

    @PostMapping("/forex")
    public void forex(
            @RequestParam String email,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam double amount){
        forexService.exchange(email, toCurrency, fromCurrency, amount);
    }

    @GetMapping("/get/transactionId")
    public ResponseEntity<TransactionService.TransactionResponseDTO> getByTransactionId(@RequestParam String transactionId) {
        return transactionService.getByTransactionId(transactionId);
    }

    @GetMapping("/get/sender")
    public ResponseEntity<List<TransactionService.TransactionResponseDTO>> getBySender(@RequestParam String senderAccountNumber) {
        return transactionService.getBySender(senderAccountNumber);
    }
    @GetMapping("/get/receiver")
    public ResponseEntity<List<TransactionService.TransactionResponseDTO>> getByReceiver(@RequestParam String receiverAccountNumber) {
        return transactionService.getByReceiver(receiverAccountNumber);
    }
    @GetMapping("get/history")
    public ResponseEntity<List<TransactionService.TransactionResponseDTO>> getTransactionHistory(@RequestParam String accountNumber) {
        return transactionService.getTransactionHistory(accountNumber);
    }
}
