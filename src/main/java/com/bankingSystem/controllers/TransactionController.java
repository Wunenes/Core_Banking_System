package com.bankingSystem.controllers;

import com.bankingSystem.services.TransactionService;
import com.bankingSystem.repositories.TransactionRepository;
import com.bankingSystem.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    TransactionService transactionService;
    TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository, TransactionService transactionService){
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    public static class TransactionResponseDTO{
        private final String sender;
        private final String receiver;
        private final LocalDateTime timestamp;
        private final BigDecimal amount;
        private final String transactionId;

        public TransactionResponseDTO(String sender, String receiver, String transactionId, BigDecimal amount){
            this.sender = sender;
            this.receiver = receiver;
            this.amount = amount;
            this.timestamp = LocalDateTime.now();
            this.transactionId = transactionId;
        }

        public String getSender() { return sender; }
        public String getReceiver() { return receiver; }
        public BigDecimal getAmount() { return amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getTransactionId() {return transactionId; }

        public void setSender(String sender) {
        }
        public void setReceiver(String receiver) {
        }
        public void setAmount(BigDecimal amount) {
        }
        public LocalDateTime setTimestamp() { return timestamp; }
        public void setTransactionId(String transactionId) {
        }
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

    @GetMapping("/get/transactionId")
    public ResponseEntity<TransactionResponseDTO> getByTransactionId(@RequestParam String transactionId) {
        List<Transaction> transactions = transactionRepository.findByTransactionId(transactionId);
        if (!transactions.isEmpty()) {
            Transaction transaction = transactions.getFirst();
            TransactionResponseDTO response = getTransactionResponseDTO(transaction);
            return ResponseEntity.ok(response);

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new TransactionResponseDTO("", "", "", BigDecimal.valueOf(0.0)));
        }
    }

    @GetMapping("/get/sender")
    public ResponseEntity<List<TransactionResponseDTO>> getBySender(@RequestParam String senderAccountNumber) {
        List<Transaction> transactions = transactionRepository.findBySender(senderAccountNumber);

        if (transactions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<TransactionResponseDTO> responseList = transactions.stream()
                .map(this::getTransactionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
    @GetMapping("/get/receiver")
    public ResponseEntity<List<TransactionResponseDTO>> getByReceiver(@RequestParam String receiverAccountNumber) {
        List<Transaction> transactions = transactionRepository.findByReceiver(receiverAccountNumber);

        if (transactions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<TransactionResponseDTO> responseList = transactions.stream()
                .map(this::getTransactionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
    @GetMapping("get/history")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionHistory(@RequestParam String accountNumber) {
        List<Transaction> transactions = transactionRepository.findAllByAccount(accountNumber);
        if (transactions.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        List<TransactionResponseDTO> responseList =  transactions.stream()
                .map(this::getTransactionResponseDTO)
                .toList();
        return ResponseEntity.ok(responseList);
    }


    private TransactionResponseDTO getTransactionResponseDTO(Transaction transaction) {
        TransactionResponseDTO response = new TransactionResponseDTO(transaction.getSender(),
                transaction.getReceiver(), transaction.getTransactionId(), transaction.getAmount());
        response.setSender(transaction.getSender());
        response.setReceiver(transaction.getReceiver());
        response.setAmount(transaction.getAmount());
        response.setTransactionId(transaction.getTransactionId());
        return response;
    }
}
