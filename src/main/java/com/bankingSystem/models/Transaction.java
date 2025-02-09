package com.bankingSystem.models;

import com.bankingSystem.encryption.AttributeEncryptor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private final String sender;

    @Column(name = "receiver_id", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private final String receiver;

    @Column(name = "amount", nullable = false)
    private final BigDecimal amount;

    @Column(name="Transaction id", nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private final String transactionId;

    @Column(name = "time", nullable = false)
    private final LocalDateTime timestamp;

    public Transaction(String sender, Account receiver, BigDecimal amount, String Transaction_ID) {
        this.sender = sender;
        this.receiver = receiver.getAccountNumber();
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.transactionId = Transaction_ID;
    }
    public Transaction(Account sender, Account receiver, BigDecimal amount, String Transaction_ID) {
        this.sender = sender.getAccountNumber();
        this.receiver = receiver.getAccountNumber();
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.transactionId = Transaction_ID;
    }

    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getTransactionId() {return transactionId; }
}
