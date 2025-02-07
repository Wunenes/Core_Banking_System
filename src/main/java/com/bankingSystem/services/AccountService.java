package com.bankingSystem.services;

import com.bankingSystem.generators.AccountNumberGenerator;
import com.bankingSystem.models.Account;
import com.bankingSystem.repositories.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.bankingSystem.generators.AccountNumberGenerator.accountNumberGenerator;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService extends Account {
    public static final Logger log = LoggerFactory.getLogger(AccountService.class);
    @Autowired
    AccountRepository accountRepository;
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
    public Optional<Account> getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    public String createAccount(Account account) {
        try {
            account.setAccountNumber(accountNumberGenerator(account.getHolderName(), account.getIdNumber()));
            accountRepository.save(account);
            return "Account successfully created";
        } catch (Error error) {
            return "Apologies! Cannot make an account at the moment";
        }
    }
    @Transactional
    public String deleteAccount(String accountNumber) {
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);
        if (account.isPresent()) {
            accountRepository.deleteByAccountNumber(accountNumber);
            return "Account deleted successfully.";
        } else {
            return "Account not found.";
        }
    }
}
