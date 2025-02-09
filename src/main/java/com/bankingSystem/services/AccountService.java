package com.bankingSystem.services;

import com.bankingSystem.models.Account;
import com.bankingSystem.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.bankingSystem.generators.AccountNumberGenerator.accountNumberGenerator;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService extends Account {
    @Autowired
    AccountRepository accountRepository;
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    public String createAccount(Account account) {
        try {
            account.setAccountNumber(accountNumberGenerator(account.getHolderName(), account.getIdNumber()));
            accountRepository.save(account);
            return account.getAccountNumber();
        } catch (Error e) {
            return e.getLocalizedMessage();
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
