package com.bankingSystem.controllers;

import com.bankingSystem.services.ForexService;
import com.bankingSystem.repositories.TransactionRepository;
import com.bankingSystem.models.Transaction;
import com.bankingSystem.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:3000, http://192.168.100.6:3000")
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
    public ResponseEntity<Transaction> deposit(@RequestBody TransactionService.TransactionResponseDTO depositBody) throws NoSuchAlgorithmException {
        return ResponseEntity.ok(transactionService.deposit(depositBody));
    }
    @PostMapping("/internal_transfer")
    public String transfer(@RequestBody TransactionService.TransactionResponseDTO transactionBody) throws NoSuchAlgorithmException {
        return transactionService.internalTransfer(transactionBody);
    }

    @PostMapping("/forex")
    public String forex(
            @RequestBody ForexService.ForexRequest forexBody) throws NoSuchAlgorithmException {
        return forexService.exchange(forexBody);
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
