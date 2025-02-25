package com.bankingSystem.services;

import com.bankingSystem.controllers.AccountController;
import com.bankingSystem.models.Account;
import com.bankingSystem.models.Users;
import com.bankingSystem.repositories.AccountRepository;
import com.bankingSystem.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsersService extends Users {
    private final UsersRepository usersRepository;
    private final AccountService accountService;
    final AccountRepository accountRepository;

    @Autowired
    public UsersService(UsersRepository usersRepository, AccountService accountService, AccountRepository accountRepository) {
        this.usersRepository = usersRepository;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
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

    public String createUser(Users user){
        try{
            usersRepository.save(user);
            Account account = new Account();
            account.setAccountType("Checking");
            account.setCurrencyType("USD");
            account.setBalance(BigDecimal.valueOf(0.00));
            account.setStatus("Inactive");
            account.setUserId(user.getUserId());
            return accountService.createAccount(account);
        } catch (Error e){
            return e.getLocalizedMessage();
        }
    }

    public ResponseEntity<List<AccountService.AccountResponseDTO>> getUserAccountsDetails(String email){
        Optional<Users> user = usersRepository.findByEmail(email);
        UUID userId = user.get().getUserId();
        List<Account> accounts = accountRepository.findByUserId(userId);
        if (accounts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }

        List<AccountService.AccountResponseDTO> responseList = accounts.stream()
                .map(this::getAccountResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    public List<AccountService.AccountResponseDTO> getUserAccounts(String email){
        Optional<Users> user = usersRepository.findByEmail(email);
        UUID userId = user.get().getUserId();
        List<Account> accounts = accountRepository.findByUserId(userId);

        return accounts.stream()
                .map(this::getAccountResponseDTO)
                .collect(Collectors.toList());
    }

    private AccountService.AccountResponseDTO getAccountResponseDTO(Account userAccount) {
        AccountService.AccountResponseDTO response = new AccountService.AccountResponseDTO(userAccount.getAccountType(),
                userAccount.getStatus(), userAccount.getBalance(), userAccount.getCurrencyType());
        response.setType(userAccount.getAccountType());
        response.setStatus(userAccount.getStatus());
        response.setBalance(userAccount.getBalance());
        response.setCurrencyType(userAccount.getCurrencyType());
        return response;
    }
}
