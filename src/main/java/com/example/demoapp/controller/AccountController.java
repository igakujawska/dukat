package com.example.demoapp.controller;

import com.example.demoapp.dto.account.CreateAccountRequest;
import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

  private final AccountService service;

  public AccountController(AccountService service) {
    this.service = service;
  }

  @GetMapping
  @Operation(summary = "Konta zalogowanego użytkownika")
  public List<AccountResponse> list(
    @RequestParam(defaultValue = "false") boolean includeArchived
  ) {
    return service.list(includeArchived);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AccountResponse create(
    @Valid @RequestBody CreateAccountRequest request
  ) {
    return service.createAccount(request);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Zmień nazwę i rodzaj konta; saldo zmieniają operacje")
  public AccountResponse update(
    @PathVariable Long id,
    @Valid @RequestBody AccountEdit request
  ) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Archiwizuj konto z zerowym saldem, zachowując historię")
  public void archive(@PathVariable Long id) {
    service.archive(id);
  }

  @PostMapping("/{id}/restore")
  public AccountResponse restore(@PathVariable Long id) {
    return service.restore(id);
  }
}
