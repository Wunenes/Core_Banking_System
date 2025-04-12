package com.bankingSystem.models;

import com.bankingSystem.encryption.AttributeEncryptor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "accounts",
       indexes = {
               @Index(name = "idx_user_id", columnList = "userId"),
               @Index(name = "idx_account_number", columnList = "accountNumber"),
               @Index(name = "idx_currency_type", columnList = "currencyType"),
       })
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

    public Account() {
        this.time = LocalDateTime.now();
    }

    public synchronized void debit(BigDecimal amount) {
        BigDecimal newBalance = balance.subtract(amount);
        setBalance(newBalance);
    }

    public synchronized void credit(BigDecimal finalAmount) {
        BigDecimal newBalance = balance.add(finalAmount);
        setBalance(newBalance);
    }
}