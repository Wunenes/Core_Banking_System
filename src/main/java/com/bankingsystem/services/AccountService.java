package com.bankingsystem.services;

import com.bankingsystem.models.Account;
import com.bankingsystem.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService extends Account {

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
            accountRepository.save(account);
            return "Account successfully created";
        } catch (DataAccessException e) {
            return "Sorry! Account already exists";
        } catch (Error error) {
            return "Apologies! Cannot make an account at the moment";
        }
    }
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }
}
