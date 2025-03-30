package com.bankingSystem.services;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import com.bankingSystem.exceptions.InsufficientFundsException;
import com.bankingSystem.exceptions.UserNotFoundException;
import com.bankingSystem.models.Account;
import com.bankingSystem.models.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    ForexService forexService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction deposit(TransactionResponseDTO depositResponse) throws NoSuchAlgorithmException {
        String receiverAccount = depositResponse.getReceiverAccNumber();
        double amount = depositResponse.getAmount().doubleValue();

        Account account = accountRepository.findByAccountNumber(receiverAccount)
                .orElseThrow(() -> new RuntimeException("Account not found"));


        account.credit(BigDecimal.valueOf(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction("DEPOSIT", account, BigDecimal.valueOf(amount),
                transactionIdGenerator("DEPOSIT", receiverAccount, "RKE", amount), "SUCCESSFUL", account.getCurrencyType(), "CASH DEPOSIT");

        if (Objects.equals(account.getStatus(), "Inactive") && amount > 200.00){
            account.setStatus("Active");
        }
        return transactionRepository.save(transaction);
    }
    @Transactional
    public String internalTransfer(TransactionResponseDTO transactionResponse) throws NoSuchAlgorithmException, UserNotFoundException, InsufficientFundsException {
        String senderAccount = transactionResponse.getSenderAccNumber();
        String receiverAccount = transactionResponse.getReceiverAccNumber();
        BigDecimal amount = transactionResponse.getAmount();

        Account sender = accountRepository.findByAccountNumber(senderAccount)
                .orElseThrow(() ->  new UserNotFoundException("User not found", senderAccount));

        Account receiver = accountRepository.findByAccountNumber(receiverAccount)
                .orElseThrow(() -> new UserNotFoundException("User not found", receiverAccount));

        BigDecimal senderBalance = sender.getBalance();

        if (senderBalance.compareTo(amount) < 0) {
            Transaction transaction = new Transaction(sender, receiver, amount,
                    transactionIdGenerator(senderAccount, receiverAccount, "RFT", amount.doubleValue()), "FAILED", sender.getCurrencyType(), "INSUFFICIENT BALANCE");
            transactionRepository.save(transaction);
            throw new InsufficientFundsException(
                    "Insufficient funds in " + senderAccount
            );
        }
        if (Objects.equals(sender.getCurrencyType(), receiver.getCurrencyType())) {
            sender.debit(amount);
            receiver.credit(amount);
        } else {
            double exchangeRate = forexService.getRates(sender.getCurrencyType(), receiver.getCurrencyType());
            sender.debit(amount);
            BigDecimal exchangedAmount = amount.multiply(BigDecimal.valueOf(exchangeRate));
            receiver.credit(exchangedAmount);
        }

        accountRepository.save(sender);
        accountRepository.save(receiver);

        if (Objects.equals(receiver.getStatus(), "Inactive")){
            receiver.setStatus("Active");
        }

        Transaction transaction = new Transaction(sender, receiver, amount,
                transactionIdGenerator(senderAccount, receiverAccount, "RNI", amount.doubleValue()), "SUCCESSFUL", sender.getCurrencyType(), "INTERNAL TRANSFER");
        transactionRepository.save(transaction);
        return transaction.getTransactionId();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TransactionResponseDTO {
        private String senderAccNumber;
        private String receiverAccNumber;
        private String timestamp;
        private BigDecimal amount;
        private String description;
        private String currency;
        private String toCurrency;
        private String fromCurrency;
        private String transactionId;

        public TransactionResponseDTO(String sender, String receiver, String transactionId, BigDecimal amount, String timeStamp, String description, String currency) {
            this.senderAccNumber = sender;
            this.receiverAccNumber = receiver;
            this.amount = amount;
            this.timestamp = timeStamp;
            this.transactionId = transactionId;
            this.description = description;
            this.currency = currency;
            this.toCurrency = null;
            this.fromCurrency = null;
        }

        public TransactionResponseDTO(String sender, String receiver, String transactionId, BigDecimal amount, String timeStamp, String description, String currency, String toCurrency, String fromCurrency) {
            this.senderAccNumber = sender;
            this.receiverAccNumber = receiver;
            this.amount = amount;
            this.timestamp = timeStamp;
            this.transactionId = transactionId;
            this.description = description;
            this.currency = currency;
            this.toCurrency = toCurrency;
            this.fromCurrency = fromCurrency;
        }

        // Overriding equals() method to compare TransactionResponseDTO objects by their fields
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionResponseDTO that = (TransactionResponseDTO) o;
            return Objects.equals(senderAccNumber, that.senderAccNumber) &&
                    Objects.equals(receiverAccNumber, that.receiverAccNumber) &&
                    Objects.equals(timestamp, that.timestamp) &&
                    Objects.equals(amount, that.amount) &&
                    Objects.equals(transactionId, that.transactionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(senderAccNumber, receiverAccNumber, timestamp, amount, transactionId);
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

    private TransactionResponseDTO getTransactionResponseDTO(Transaction transaction) {

        return new TransactionResponseDTO(
                "",
                "",
                "",
                transaction.getAmount(),
                "",
                "",
                ""
        );
    }

}