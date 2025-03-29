package com.bankingSystem.models;

import com.bankingSystem.encryption.AttributeEncryptor;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String accountType;

    @Column(nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String currencyType;

    @Column(nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String status;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(nullable = false, unique = true)
    @Convert(converter = AttributeEncryptor.class)
    private String accountNumber;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    protected BigDecimal balance;

    public Account() {}

    public Account(String accountNumber, String accountType, String currencyType, String status, LocalDateTime time,
                   BigDecimal balance, UUID userId) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.currencyType = currencyType;
        this.status = status;
        this.userId = userId;
        this.balance = balance;
        this.time = LocalDateTime.now();
    }

    public Account(String accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    @PrePersist
    protected void onCreate() {
        this.time = LocalDateTime.now();
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public BigDecimal getBalance() {
        return balance;
    }
    public String getAccountType() {
        return accountType;
    }
    public LocalDateTime getTime() {
        return time;
    }
    public String getCurrencyType() {
        return currencyType;
    }
    public String getStatus() {
        return status;
    }
    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public void setAccountType(String accountType){
        this.accountType = accountType;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public void setCurrencyType(String ID){
        this.currencyType = ID;
    }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void debit(BigDecimal amount) {
        BigDecimal newBalance = balance.subtract(amount);
        setBalance(newBalance);
    }

    public void credit(BigDecimal finalAmount) {
        BigDecimal newBalance = balance.add(finalAmount);
        setBalance(newBalance);
    }
}