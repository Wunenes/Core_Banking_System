package com.bankingSystem.models;

import com.bankingSystem.encryption.AttributeEncryptor;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String holderName;

    @Column(nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String idNumber;

    @Column(nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String email;

    @Column(nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    @Convert(converter = AttributeEncryptor.class)
    private String accountNumber;

    @Column(nullable = false)
    protected BigDecimal balance;

    public Account() {}

    public Account(String accountNumber, String holderName, String idNumber, String email, String phoneNumber,
                   BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.idNumber = idNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.balance = balance;
    }

    public Account(String accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public BigDecimal getBalance() {
        return balance;
    }
    public String getHolderName() {
        return holderName;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getIdNumber() {
        return idNumber;
    }
    public String getEmail() {
        return email;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public void setHolderName(String holderName){
        this.holderName = holderName;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setIdNumber(String ID){
        this.idNumber = ID;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}