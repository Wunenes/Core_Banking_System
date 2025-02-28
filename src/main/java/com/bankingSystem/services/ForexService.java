package com.bankingSystem.services;

import com.bankingSystem.models.Account;
import com.bankingSystem.models.Transaction;
import com.bankingSystem.models.Users;
import com.bankingSystem.repositories.AccountRepository;
import com.bankingSystem.repositories.TransactionRepository;
import com.bankingSystem.repositories.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.bankingSystem.generators.TransactionIdGenerator.transactionIdGenerator;

@Service
public class ForexService {
    UsersRepository usersRepository;
    UsersService usersService;
    AccountService accountService;
    AccountRepository accountRepository;
    TransactionRepository transactionRepository;

    public ForexService(UsersService usersService, UsersRepository usersRepository, AccountService accountService, AccountRepository accountRepository, TransactionRepository transactionRepository){
        this.accountService = accountService;
        this.usersRepository = usersRepository;
        this.usersService = usersService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }
    private static final String API_URL = "https://api.exchangeratesapi.io/latest?base=";
    public void exchange(String email, String toCurrency, String fromCurrency, double amount) throws NoSuchAlgorithmException {
        RestTemplate restTemplate = new RestTemplate();
        /*String url = API_URL + fromCurrency;
        Map response = restTemplate.getForObject(url, Map.class);

        Map<String, Double> rates = (Map<String, Double>) response.get("rates");
        double rate = rates.get(toCurrency);*/

        double exchangedAmount = amount * 129;

        Optional<Users> user = usersRepository.findByEmail(email);
        UUID userId = user.get().getUserId();
        List<Account> accounts = accountRepository.findByUserId(userId);
        boolean hasToCurrencyAccount = false;
        boolean sufficientFunds = false;

        Account fromAccount= new Account();
        Account toAccount = new Account();

        for (Account account : accounts) {
            if (account.getCurrencyType().equals(fromCurrency) && account.getBalance().compareTo(BigDecimal.valueOf(amount)) > 0) {
                account.setBalance(account.getBalance().subtract(BigDecimal.valueOf(amount)));
                sufficientFunds = true;
                accountRepository.save(account);
                fromAccount = account;
            }
        }

        for (Account account : accounts) {
            if (account.getCurrencyType().equals(toCurrency) && sufficientFunds) {
                account.setBalance(account.getBalance().add(BigDecimal.valueOf(exchangedAmount)));
                accountRepository.save(account);
                hasToCurrencyAccount = true;
                toAccount = account;
            }
        }

        if (!hasToCurrencyAccount && sufficientFunds) {
            Account newAccount = new Account();
            newAccount.setStatus("Active");
            newAccount.setAccountType("Checking");
            newAccount.setCurrencyType(toCurrency);
            newAccount.setUserId(userId);
            newAccount.setBalance(BigDecimal.valueOf(exchangedAmount));
            accountService.createAccount(newAccount);
            toAccount = newAccount;
        }


        Transaction transaction = new Transaction(fromAccount, toAccount, BigDecimal.valueOf(amount),
                transactionIdGenerator(fromAccount.getAccountNumber(), toAccount.getAccountNumber(), "RFX", amount), sufficientFunds ? "SUCCESSFUL" : "FAILED", fromCurrency, "CURRENCY EXCHANGE", fromCurrency, toCurrency);
        transactionRepository.save(transaction);

    }
}

