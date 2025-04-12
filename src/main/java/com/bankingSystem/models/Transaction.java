package com.bankingSystem.models;

import com.bankingSystem.encryption.AttributeEncryptor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "transactions",
       indexes = {
       @Index(name = "idx_sender_id", columnList = "sender_id"),
       @Index(name = "idx_transaction_id", columnList = "transaction_id"),
       @Index(name="idx_receiver_id", columnList = "receiver_id")
       }
)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String sender;

    @Column(name = "receiver_id", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String receiver;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name="transaction_id", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String transactionId;

    @Column(name="status", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String status;

    @Column(name = "time", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "description", nullable = false)
    private String description ;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "to_currency")
    private String toCurrency;

    @Column(name = "from_currency")
    private String fromCurrency;

    public Transaction(String sender, Account receiver, BigDecimal amount, String Transaction_ID, String status, String currency, String description) {
        this.sender = sender;
        this.receiver = receiver.getAccountNumber();
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.transactionId = Transaction_ID;
        this.status = status;
        this.description = description;
        this.currency = currency;
    }
    public Transaction(Account sender, Account receiver, BigDecimal amount, String Transaction_ID, String status, String currency, String description) {
        this.sender = sender.getAccountNumber();
        this.receiver = receiver.getAccountNumber();
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.transactionId = Transaction_ID;
        this.status = status;
        this.description = description;
        this.currency = currency;
    }

    public Transaction(Account sender, Account receiver, BigDecimal amount, String Transaction_ID, String status, String currency, String description, String fromCurrency, String toCurrency) {
        this.sender = sender.getAccountNumber();
        this.receiver = receiver.getAccountNumber();
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.transactionId = Transaction_ID;
        this.status = status;
        this.description = description;
        this.currency = currency;
        this.toCurrency = toCurrency;
        this.fromCurrency = fromCurrency;
    }
}
