package com.bankingSystem.services;

import java.math.BigDecimal;
import com.bankingSystem.models.Account;
import com.bankingSystem.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.bankingSystem.repositories.AccountRepository;
import com.bankingSystem.repositories.TransactionRepository;


@Service
public class TransactionService {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TransactionRepository transactionRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction deposit(String receiverAccount, double amount) {
        Account account = accountRepository.findByAccountNumber(receiverAccount)
                .orElseThrow(() -> new RuntimeException("Account not found"));


        account.setBalance(BigDecimal.valueOf(account.getBalance().doubleValue() + amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction("ATM DEPOSIT", account, BigDecimal.valueOf(amount), "Holder");
        return transactionRepository.save(transaction);
    }
    @Transactional
    public Transaction internalTransfer(String senderAccount, String receiverAccount, double amount) {
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

        Transaction transaction = new Transaction(sender, receiver, BigDecimal.valueOf(amount), "Holder");
        return transactionRepository.save(transaction);
    }
}