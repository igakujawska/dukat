package com.example.demoapp.controller;

import com.example.demoapp.dto.account.CreateAccountRequest;
import com.example.demoapp.model.Account;
import com.example.demoapp.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Odpowiedzi metod są danymi HTTP (JSON), a nie nazwami szablonów HTML.
@RequestMapping("/api/accounts") // Wspólny adres dla metod GET i POST tego kontrolera.
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PostMapping
    // @RequestBody zamienia JSON na DTO; @Valid sprawdza ograniczenia pól przed wywołaniem serwisu.
    public ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }
}
