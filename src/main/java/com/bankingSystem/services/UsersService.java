package com.bankingSystem.services;

import com.bankingSystem.models.Account;
import com.bankingSystem.models.Transaction;
import com.bankingSystem.models.Users;
import com.bankingSystem.repositories.AccountRepository;
import com.bankingSystem.repositories.TransactionRepository;
import com.bankingSystem.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsersService extends Users {
    private static UsersRepository usersRepository = null;
    private final AccountService accountService;
    static AccountRepository accountRepository = null;
    final TransactionRepository transactionRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository, AccountService accountService, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        UsersService.usersRepository = usersRepository;
        this.accountService = accountService;
        UsersService.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Users> getAllUsers(){
        return usersRepository.findAll();
    }

    public Optional<Users> getByEmail(String email) {
        return usersRepository.findByEmail(email);
    }
    public Optional<Users> getByUserId(UUID userId) {
        return usersRepository.findByUserId(userId);
    }

    public static String getNameByUserAccountNumber(String accountNumber) {
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);
        if (account.isPresent()) {
            Optional<Users> user = usersRepository.findByUserId(account.get().getUserId());
            if (user.isPresent()) {
                return user.get().getUserName(); // Return the user's name
            }
        }
        return accountNumber; // Fallback to account number if user not found
    }

    public String createUser(Users user){
        try{
            Optional<Users> userCheck = usersRepository.findByEmail(user.getEmail());
            if(userCheck.isEmpty()) {
                usersRepository.save(user);
                Account account = new Account();
                account.setAccountType("Checking");
                account.setCurrencyType("USD");
                account.setBalance(BigDecimal.valueOf(0.00));
                account.setStatus("Inactive");
                account.setUserId(user.getUserId());
                return accountService.createAccount(account);
            } else{
                return "User already exists";
            }
        } catch (Error e){
            return e.getLocalizedMessage();
        }
    }

    public ResponseEntity<List<AccountService.AccountResponseDTO>> getUserAccountsDetails(String email){
        Optional<Users> user = usersRepository.findByEmail(email);
        if (user.isPresent()) {
            UUID userId = user.get().getUserId();
            List<Account> accounts = accountRepository.findByUserId(userId);
            if (accounts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            List<AccountService.AccountResponseDTO> responseList = accounts.stream()
                    .map(this::getAccountResponseDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responseList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonList(new AccountService.AccountResponseDTO("", "", BigDecimal.valueOf(0.0), "", "")));
        }
    }

    public ArrayList<List<TransactionService.TransactionResponseDTO>> getTransactions(String email){
        Optional<Users> user = usersRepository.findByEmail(email);
        if (user.isPresent()) {
            UUID userId = user.get().getUserId();
            List<Account> accounts = accountRepository.findByUserId(userId);
            ArrayList<List<TransactionService.TransactionResponseDTO>> userTransactions = new ArrayList<>();

            for (Account account : accounts) {
                List<Transaction> transactions = transactionRepository.findAllByAccount(account.getAccountNumber());
                if (transactions.isEmpty()) {
                    return userTransactions;
                }

                List<TransactionService.TransactionResponseDTO> responseList = transactions.stream()
                        .map(this::getTransactionResponseDTO)
                        .collect(Collectors.toList());

                ListIterator<TransactionService.TransactionResponseDTO> iterator = responseList.listIterator();
                while (iterator.hasNext()) {
                    TransactionService.TransactionResponseDTO response = iterator.next();
                    if (response.getDescription().equals("CURRENCY EXCHANGE")) {
                        if (Objects.equals(response.getReceiverName(), account.getAccountNumber())) {
                            iterator.remove();
                        } else {
                            Transaction forexTransaction = transactionRepository.findByTransactionId(response.getTransactionId());
                            TransactionService.TransactionResponseDTO forexResponse = getForexResponseDTO(forexTransaction);
                            iterator.set(forexResponse);
                        }
                    }
                }


                if (userTransactions.contains(responseList)) {
                    continue;
                }

                userTransactions.add(responseList);
            }

            return userTransactions;
        } else{
            return new ArrayList<>();
        }
    }
    private AccountService.AccountResponseDTO getAccountResponseDTO(Account userAccount) {
        AccountService.AccountResponseDTO response = new AccountService.AccountResponseDTO(userAccount.getAccountType(),
                userAccount.getStatus(), userAccount.getBalance(), userAccount.getCurrencyType(), userAccount.getAccountNumber());
        response.setType(userAccount.getAccountType());
        response.setStatus(userAccount.getStatus());
        response.setBalance(userAccount.getBalance());
        response.setCurrencyType(userAccount.getCurrencyType());
        response.setAccountNumber(userAccount.getAccountNumber());
        return response;
    }

    private TransactionService.TransactionResponseDTO getTransactionResponseDTO(Transaction transaction) {
        TransactionService.TransactionResponseDTO response = new TransactionService.TransactionResponseDTO(transaction.getSender(),
                transaction.getReceiver(), transaction.getTransactionId(), transaction.getAmount(), transaction.getTimestamp().toString(), transaction.getDescription(), transaction.getCurrency());
        response.setSenderName(transaction.getSender());
        response.setReceiverName(transaction.getReceiver());
        response.setAmount(transaction.getAmount());
        response.setTransactionId(transaction.getTransactionId());
        response.setTimestamp(transaction.getTimestamp());
        response.setDescription(transaction.getDescription());
        response.setCurrency(transaction.getCurrency());
        return response;
    }

    private TransactionService.TransactionResponseDTO getForexResponseDTO(Transaction transaction) {
        TransactionService.TransactionResponseDTO response = new TransactionService.TransactionResponseDTO(transaction.getSender(),
                transaction.getReceiver(), transaction.getTransactionId(), transaction.getAmount(), transaction.getTimestamp().toString(), transaction.getDescription(), transaction.getCurrency(), transaction.getToCurrency(), transaction.getFromCurrency());
        response.setSenderName(transaction.getSender());
        response.setReceiverName(transaction.getReceiver());
        response.setAmount(transaction.getAmount());
        response.setTransactionId(transaction.getTransactionId());
        response.setTimestamp(transaction.getTimestamp());
        response.setDescription(transaction.getDescription());
        response.setCurrency(transaction.getCurrency());
        response.setToCurrency(transaction.getToCurrency());
        response.setFromCurrency(transaction.getFromCurrency());
        return response;
    }
}
