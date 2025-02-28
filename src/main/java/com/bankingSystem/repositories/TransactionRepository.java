package com.bankingSystem.repositories;

import com.bankingSystem.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Transaction findByTransactionId(String transactionId);
    List<Transaction> findBySender(@Param("senderAccountNumber") String senderAccountNumber);
    @Query("SELECT t FROM Transaction t WHERE t.sender= :accountNumber OR t.receiver = :accountNumber ORDER BY t.timestamp DESC")
    List<Transaction> findAllByAccount(@Param("accountNumber") String accountNumber);
    List<Transaction> findByReceiver(@Param("senderAccountNumber") String receiverAccountNumber);

}