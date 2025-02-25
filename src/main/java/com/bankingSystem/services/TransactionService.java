package com.bankingSystem.services;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.bankingSystem.models.Account;
import com.bankingSystem.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.bankingSystem.repositories.AccountRepository;
import com.bankingSystem.repositories.TransactionRepository;


import static com.bankingSystem.generators.TransactionIdGenerator.transactionIdGenerator;


@Service
public class TransactionService {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction deposit(String receiverAccount, double amount) throws NoSuchAlgorithmException {
        Account account = accountRepository.findByAccountNumber(receiverAccount)
                .orElseThrow(() -> new RuntimeException("Account not found"));


        account.setBalance(BigDecimal.valueOf(account.getBalance().doubleValue() + amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction("DEPOSIT", account, BigDecimal.valueOf(amount),
                transactionIdGenerator("DEPOSIT", receiverAccount, "RKE", amount));

        if (Objects.equals(account.getStatus(), "Inactive")){
            account.setStatus("Active");
        }
        return transactionRepository.save(transaction);
    }
    @Transactional
    public String internalTransfer(String senderAccount, String receiverAccount, double amount) throws NoSuchAlgorithmException {
        Account sender = accountRepository.findByAccountNumber(senderAccount)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Account receiver = accountRepository.findByAccountNumber(receiverAccount)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        double senderBalance = sender.getBalance().doubleValue();
        double receiverBalance = receiver.getBalance().doubleValue();

        if (senderBalance < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(BigDecimal.valueOf(senderBalance - amount));
        receiver.setBalance(BigDecimal.valueOf(receiverBalance + amount));


        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = new Transaction(sender, receiver, BigDecimal.valueOf(amount),
                transactionIdGenerator(senderAccount, receiverAccount, "RNI", amount));
        transactionRepository.save(transaction);
        return transaction.getTransactionId();
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

    public ResponseEntity<TransactionResponseDTO> getByTransactionId(String transactionId) {
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

    public ResponseEntity<List<TransactionResponseDTO>> getBySender(String senderAccountNumber) {
        List<Transaction> transactions = transactionRepository.findBySender(senderAccountNumber);

        if (transactions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<TransactionResponseDTO> responseList = transactions.stream()
                .map(this::getTransactionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    public ResponseEntity<List<TransactionResponseDTO>> getByReceiver(String receiverAccountNumber) {
        List<Transaction> transactions = transactionRepository.findByReceiver(receiverAccountNumber);

        if (transactions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<TransactionResponseDTO> responseList = transactions.stream()
                .map(this::getTransactionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    public ResponseEntity<List<TransactionResponseDTO>> getTransactionHistory(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findAllByAccount(accountNumber);
        if (transactions.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        List<TransactionResponseDTO> responseList =  transactions.stream()
                .map(this::getTransactionResponseDTO)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    private TransactionService.TransactionResponseDTO getTransactionResponseDTO(Transaction transaction) {
        TransactionService.TransactionResponseDTO response = new TransactionResponseDTO(transaction.getSender(),
                transaction.getReceiver(), transaction.getTransactionId(), transaction.getAmount());
        response.setSender(transaction.getSender());
        response.setReceiver(transaction.getReceiver());
        response.setAmount(transaction.getAmount());
        response.setTransactionId(transaction.getTransactionId());
        return response;
    }
}