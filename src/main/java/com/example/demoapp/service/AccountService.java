package com.example.demoapp.service;

import com.example.demoapp.dto.account.CreateAccountRequest;
import com.example.demoapp.model.Account;
import com.example.demoapp.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Spring tworzy bean warstwy biznesowej i wstrzykuje potrzebne repozytorium.
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account createAccount(CreateAccountRequest request) {
        return accountRepository.save(new Account(request.name(), request.accountType(), request.balance()));
    }
}
