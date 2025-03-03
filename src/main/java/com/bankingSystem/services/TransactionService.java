package com.bankingSystem.services;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.bankingSystem.models.Account;
import com.bankingSystem.models.Transaction;
import com.bankingSystem.models.Users;
import com.bankingSystem.repositories.UsersRepository;
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
    @Autowired
    UsersRepository usersRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction deposit(TransactionResponseDTO depositResponse) throws NoSuchAlgorithmException {
        String receiverAccount = depositResponse.getReceiver();
        double amount = depositResponse.getAmount().doubleValue();

        Account account = accountRepository.findByAccountNumber(receiverAccount)
                .orElseThrow(() -> new RuntimeException("Account not found"));


        account.setBalance(BigDecimal.valueOf(account.getBalance().doubleValue() + amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction("DEPOSIT", account, BigDecimal.valueOf(amount),
                transactionIdGenerator("DEPOSIT", receiverAccount, "RKE", amount), "SUCCESSFUL", account.getCurrencyType(), "CASH DEPOSIT");

        if (Objects.equals(account.getStatus(), "Inactive")){
            account.setStatus("Active");
        }
        return transactionRepository.save(transaction);
    }
    @Transactional
    public String internalTransfer(TransactionResponseDTO transactionResponse) throws NoSuchAlgorithmException {
        String senderAccount = transactionResponse.getSender();
        String receiverAccount = transactionResponse.getReceiver();
        BigDecimal amount = transactionResponse.getAmount();

        Account sender = accountRepository.findByAccountNumber(senderAccount)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Account receiver = accountRepository.findByAccountNumber(receiverAccount)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        double senderBalance = sender.getBalance().doubleValue();
        double receiverBalance = receiver.getBalance().doubleValue();

        if (senderBalance < amount.doubleValue()) {
            Transaction transaction = new Transaction(sender, receiver, BigDecimal.valueOf(amount.doubleValue()),
                    transactionIdGenerator(senderAccount, receiverAccount, "RFT", amount.doubleValue()), "FAILED", sender.getCurrencyType(), "FAILED TRANSACTION");
            transactionRepository.save(transaction);
            throw new RuntimeException("Insufficient balance");
        }

        sender.setBalance(BigDecimal.valueOf(senderBalance - amount.doubleValue()));
        receiver.setBalance(BigDecimal.valueOf(receiverBalance + amount.doubleValue()));


        accountRepository.save(sender);
        accountRepository.save(receiver);

        if (Objects.equals(receiver.getStatus(), "Inactive")){
            receiver.setStatus("Active");
        }
        UUID receiverUserId = receiver.getUserId();
        Optional<Users> recipientUser = usersRepository.findByUserId(receiverUserId);
        String recipientName = recipientUser.get().getUserName();

        Transaction transaction = new Transaction(sender, receiver, amount,
                transactionIdGenerator(senderAccount, receiverAccount, "RNI", amount.doubleValue()), "SUCCESSFUL", sender.getCurrencyType(), recipientName);
        transactionRepository.save(transaction);
        return transaction.getTransactionId();
    }

    public static class TransactionResponseDTO {
        private String sender;
        private String receiver;
        private String timestamp;
        private BigDecimal amount;
        private String description;
        private String currency;
        private String toCurrency;
        private String fromCurrency;
        private String transactionId;

        // Default constructor for Jackson
        public TransactionResponseDTO() {}

        public TransactionResponseDTO(String sender, String receiver, String transactionId, BigDecimal amount, String timeStamp, String description, String currency) {
            this.sender = sender;
            this.receiver = receiver;
            this.amount = amount;
            this.timestamp = timeStamp;
            this.transactionId = transactionId;
            this.description = description;
            this.currency = currency;
            this.toCurrency = null;
            this.fromCurrency = null;
        }

        public TransactionResponseDTO(String sender, String receiver, String transactionId, BigDecimal amount, String timeStamp, String description, String currency, String toCurrency, String fromCurrency) {
            this.sender = sender;
            this.receiver = receiver;
            this.amount = amount;
            this.timestamp = timeStamp;
            this.transactionId = transactionId;
            this.description = description;
            this.currency = currency;
            this.toCurrency = toCurrency;
            this.fromCurrency = fromCurrency;
        }

        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }

        public String getReceiver() { return receiver; }
        public void setReceiver(String receiver) { this.receiver = receiver; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp.toString(); }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getToCurrency() { return toCurrency; }
        public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

        public String getFromCurrency() { return fromCurrency; }
        public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

        // Overriding equals() method to compare TransactionResponseDTO objects by their fields
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionResponseDTO that = (TransactionResponseDTO) o;
            return Objects.equals(sender, that.sender) &&
                    Objects.equals(receiver, that.receiver) &&
                    Objects.equals(timestamp, that.timestamp) &&
                    Objects.equals(amount, that.amount) &&
                    Objects.equals(transactionId, that.transactionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sender, receiver, timestamp, amount, transactionId);
        }
    }


    public ResponseEntity<TransactionResponseDTO> getByTransactionId(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId);
        if (!transaction.getTransactionId().isEmpty()) {
            TransactionResponseDTO response = getTransactionResponseDTO(transaction);
            return ResponseEntity.ok(response);

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new TransactionResponseDTO("", "", "", BigDecimal.valueOf(0.0), "", "", ""));
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
                transaction.getReceiver(), transaction.getTransactionId(), transaction.getAmount(), transaction.getTimestamp().toString(), transaction.getDescription(), transaction.getCurrency());
        response.setSender(transaction.getSender());
        response.setReceiver(transaction.getReceiver());
        response.setAmount(transaction.getAmount());
        response.setTransactionId(transaction.getTransactionId());
        response.setTimestamp(transaction.getTimestamp());
        response.setDescription(transaction.getDescription());
        response.setCurrency(transaction.getCurrency());
        return response;
    }

    private TransactionService.TransactionResponseDTO getForexResponseDTO(Transaction transaction) {
        TransactionService.TransactionResponseDTO response = new TransactionResponseDTO(transaction.getSender(),
                transaction.getReceiver(), transaction.getTransactionId(), transaction.getAmount(), transaction.getTimestamp().toString(), transaction.getDescription(), transaction.getCurrency(), transaction.getToCurrency(), transaction.getFromCurrency());
        response.setSender(transaction.getSender());
        response.setReceiver(transaction.getReceiver());
        response.setAmount(transaction.getAmount());
        response.setTransactionId(transaction.getTransactionId());
        response.setTimestamp(transaction.getTimestamp());
        response.setDescription(transaction.getDescription());
        response.setCurrency(transaction.getCurrency());
        response.setToCurrency(transaction.getToCurrency());
        response.setFromCurrency(transaction.getFromCurrency());
        return response;
    }
}