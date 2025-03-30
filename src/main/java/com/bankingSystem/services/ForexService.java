package com.bankingSystem.services;

import com.bankingSystem.exceptions.InsufficientFundsException;
import com.bankingSystem.exceptions.UserNotFoundException;
import com.bankingSystem.models.Account;
import com.bankingSystem.models.Users;
import com.bankingSystem.models.Transaction;
import com.bankingSystem.repositories.AccountRepository;
import com.bankingSystem.repositories.TransactionRepository;
import com.bankingSystem.repositories.UsersRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.bankingSystem.generators.TransactionIdGenerator.transactionIdGenerator;

@Service
@Transactional
public class ForexService {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    AccountService accountService;

    @Value("${forex.api}")
    private String forexApiKey;

    @Setter
    @Getter
    public static class ForexRequest {
        private String email;
        private String fromCurrency;
        private String toCurrency;
        private double amount;
        private double exchangeRate;

        ForexRequest(String email, String fromCurrency, String toCurrency, double amount, double exchangeRate) {
            this.email = email;
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
            this.amount = amount;
            this.exchangeRate = exchangeRate;
        }

    }

    public double getRates(String fromCurrency, String toCurrency) {
        try {
            String urlString = String.format("https://v6.exchangerate-api.com/v6/%s/pair/%s/%s/1.00",
                    forexApiKey, fromCurrency, toCurrency);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject jsonResponse = new JSONObject(response.body());

            if (jsonResponse.getString("result").equals("success")) {
                return jsonResponse.getDouble("conversion_rate");
            } else {
                throw new IOException("Failed to retrieve conversion rates: " + jsonResponse);
            }
        } catch (Exception e) {
            throw new RuntimeException("Currency exchange rate retrieval failed", e);
        }
    }

    public Account createNewAccount(Users user, String toCurrency) {
        return accountService.createAccount(UsersService.accountBuilder(user, toCurrency, "Active"));
    }

    @Transactional
    public ForexRequest exchange(ForexRequest request) throws InsufficientFundsException, NoSuchAlgorithmException, UserNotFoundException {
        Transaction transaction = null;
        try {
            validateRequest(request);

            Users user = usersRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not found", request.getEmail()));

            BigDecimal amount = BigDecimal.valueOf(request.getAmount())
                    .setScale(2, RoundingMode.HALF_UP);

            double rate = getRates(
                    request.getFromCurrency(),
                    request.getToCurrency()
            );

            request.setExchangeRate(rate);

            BigDecimal exchangedAmount = amount.multiply(BigDecimal.valueOf(rate));
            BigDecimal fee = BigDecimal.valueOf(0.00);
            BigDecimal finalAmount = exchangedAmount.subtract(fee);

            Account fromAccount = accountRepository.findByUserIdAndCurrencyType(
                    user.getUserId(),
                    request.getFromCurrency()
            ).orElseThrow();

            // Lock the account for concurrent access
            List<Account> accounts = accountRepository.findByUserId(fromAccount.getUserId());
            Account lockedFromAccount = accounts.stream()
                    .filter(account -> account.getCurrencyType().equals(request.getFromCurrency())) // Example filter condition
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No account found for specified criteria"));

            if (lockedFromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(
                        "Insufficient funds in " + request.getFromCurrency() + " account"
                );
            }

            Account toAccount = accountRepository.findByUserIdAndCurrencyType(
                    user.getUserId(),
                    request.getToCurrency()
            ).orElseGet(() -> createNewAccount(user, request.toCurrency));

            // Perform the exchange
            lockedFromAccount.debit(amount);
            toAccount.credit(finalAmount);

            // Save accounts
            accountRepository.save(lockedFromAccount);
            accountRepository.save(toAccount);

            // Create and save transaction record
            transaction = new Transaction(lockedFromAccount, toAccount, amount, transactionIdGenerator(lockedFromAccount.getAccountNumber(), toAccount.getAccountNumber(), "RFX", amount.doubleValue()),
                    "SUCCESSFUL",
                    request.getToCurrency(),
                    "CURRENCY EXCHANGE",
                    request.getFromCurrency(),
                    request.getToCurrency()
            );

            transactionRepository.save(transaction);

            return new ForexRequest(
                    request.getEmail(),
                    request.getFromCurrency(),
                    request.getToCurrency(),
                    request.getAmount(),
                    request.getExchangeRate()
            );
        } catch (Exception e) {
            // Log failed transaction if we created one
            if (transaction != null) {
                transaction.setStatus("FAILED");
                transactionRepository.save(transaction);
            }
            throw e;
        }
    }
    private void validateRequest (ForexRequest request){
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Exchange amount cannot be negative");
        }
        // Add more validations as needed
    }
}

