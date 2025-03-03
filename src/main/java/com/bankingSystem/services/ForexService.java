package com.bankingSystem.services;

import com.bankingSystem.models.Account;
import com.bankingSystem.models.Users;
import com.bankingSystem.models.Transaction;
import com.bankingSystem.repositories.AccountRepository;
import com.bankingSystem.repositories.TransactionRepository;
import com.bankingSystem.repositories.UsersRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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

    public static class ForexRequest {
        private String email;
        private String fromCurrency;
        private String toCurrency;
        private double amount;

        // Getter and Setter for email
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        // Getter and Setter for fromCurrency
        public String getFromCurrency() {
            return fromCurrency;
        }

        public void setFromCurrency(String fromCurrency) {
            this.fromCurrency = fromCurrency;
        }

        // Getter and Setter for toCurrency
        public String getToCurrency() {
            return toCurrency;
        }

        public void setToCurrency(String toCurrency) {
            this.toCurrency = toCurrency;
        }

        // Getter and Setter for amount
        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }


    public ForexService(UsersService usersService, UsersRepository usersRepository, AccountService accountService, AccountRepository accountRepository, TransactionRepository transactionRepository){
        this.accountService = accountService;
        this.usersRepository = usersRepository;
        this.usersService = usersService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public double getRates(String fromCurrency, String toCurrency) {
        try {
            String urlString = String.format("https://v6.exchangerate-api.com/v6/19d7498bf9720e16f6308633/pair/%s/%s/1.00", fromCurrency, toCurrency);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                response.append((char) c);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());

            if (jsonResponse.getString("result").equals("success")) {
                return jsonResponse.getDouble("conversion_rate");
            } else {
                throw new IOException("Failed to retrieve conversion rates");
            }

        } catch (Exception e) {
            System.err.println("Error fetching exchange rate: " + e.getMessage());
            throw new RuntimeException("Currency exchange rate retrieval failed", e);
        }
    }

    public String exchange(ForexRequest forexResponse) throws NoSuchAlgorithmException {
        double amount = forexResponse.getAmount();
        String fromCurrency = forexResponse.getFromCurrency();
        String toCurrency = forexResponse.getToCurrency();


        double exchangedAmount = amount * getRates(fromCurrency, toCurrency);

        Optional<Users> user = usersRepository.findByEmail(forexResponse.getEmail());
        List<Account> accounts = accountRepository.findByUserId(user.get().getUserId());
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
            newAccount.setUserId(user.get().getUserId());
            newAccount.setBalance(BigDecimal.valueOf(exchangedAmount));
            accountService.createAccount(newAccount);
            toAccount = newAccount;
        }

        Transaction transaction = new Transaction(fromAccount, toAccount, BigDecimal.valueOf(amount),
                transactionIdGenerator(fromAccount.getAccountNumber(), toAccount.getAccountNumber(), "RFX", amount), sufficientFunds ? "SUCCESSFUL" : "FAILED", fromCurrency, "CURRENCY EXCHANGE", fromCurrency, toCurrency);
        transactionRepository.save(transaction);

        return sufficientFunds ? "successful" : "insufficient balance";
    }
}

