package com.bankingSystem.services;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.bankingSystem.models.Account;
import com.bankingSystem.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.bankingSystem.repositories.AccountRepository;
import com.bankingSystem.repositories.TransactionRepository;

import static com.bankingSystem.generators.AccountNumberGenerator.checkSumAlgorithm;
import static com.bankingSystem.generators.TransactionIdGenerator.letterToNumber;
import static com.bankingSystem.generators.TransactionIdGenerator.transactionIdGenerator;


@Service
public class TransactionService {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TransactionRepository transactionRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction deposit(String receiverAccount, double amount) throws NoSuchAlgorithmException {
        Account account = accountRepository.findByAccountNumber(receiverAccount)
                .orElseThrow(() -> new RuntimeException("Account not found"));


        account.setBalance(BigDecimal.valueOf(account.getBalance().doubleValue() + amount));
        accountRepository.save(account);

        Random random = new Random();

        char letter1 = (char) ('A' + random.nextInt(26));
        char letter2 = (char) ('A' + random.nextInt(26));
        char letter3 = (char) ('A' + random.nextInt(26));
        String randomLetters = "" + letter1 + letter2 + letter3;
        char[] charArray = randomLetters.toCharArray();

        StringBuilder charStrings = new StringBuilder();
        for(char letter: charArray) {
            charStrings.append(letterToNumber(letter));
        }

        String finalDigits = checkSumAlgorithm(charStrings);
        String randomChars = "" + letter1 + letter2 + letter3 + finalDigits;

        Transaction transaction = new Transaction("DEPOSIT", account, BigDecimal.valueOf(amount),
                transactionIdGenerator("DEPOSIT", receiverAccount, "RKE", amount));
        return transactionRepository.save(transaction);
    }
    @Transactional
    public Transaction internalTransfer(String senderAccount, String receiverAccount, double amount) throws NoSuchAlgorithmException {
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

        Transaction transaction = new Transaction(sender, receiver, BigDecimal.valueOf(amount),
                transactionIdGenerator(senderAccount, receiverAccount, "RNI", amount));
        return transactionRepository.save(transaction);
    }
}