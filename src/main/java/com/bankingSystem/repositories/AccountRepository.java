package com.bankingSystem.repositories;

import com.bankingSystem.models.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUserId(UUID userId);
    void deleteByAccountNumber(String accountNumber);

    Optional<Account> findByUserIdAndCurrencyType(UUID userId, String currencyType);

}