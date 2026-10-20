package com.example.demoapp.service;

import com.example.demoapp.dto.account.CreateAccountRequest;
import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.model.*;
import com.example.demoapp.repository.AccountRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AccountService {

  private final AccountRepository accounts;
  private final CurrentUser current;

  public AccountService(AccountRepository accounts, CurrentUser current) {
    this.accounts = accounts;
    this.current = current;
  }

  @Transactional(readOnly = true)
  public List<AccountResponse> list(boolean includeArchived) {
    return accounts
      .findAllByOwnerIdOrderByIdAsc(current.id())
      .stream()
      .filter(a -> includeArchived || !a.isArchived())
      .map(AccountService::response)
      .toList();
  }

  public AccountResponse createAccount(CreateAccountRequest request) {
    var owner = current.required();
    if (
      accounts.existsByOwnerIdAndNameIgnoreCase(
        owner.getId(),
        request.name().trim()
      )
    ) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Taka nazwa konta już istnieje"
    );
    var account = new Account(
      request.name().trim(),
      request.accountType().trim(),
      request.balance()
    );
    account.setOwner(owner);
    return response(accounts.save(account));
  }

  public AccountResponse update(Long id, AccountEdit request) {
    var account = lock(id, current.id());
    var duplicate = accounts
      .findAllByOwnerIdOrderByIdAsc(current.id())
      .stream()
      .anyMatch(
        a ->
          !a.getId().equals(id) &&
          a.getName().equalsIgnoreCase(request.name().trim())
      );
    if (duplicate) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Taka nazwa konta już istnieje"
    );
    account.setName(request.name().trim());
    account.setAccountType(request.accountType().trim());
    return response(account);
  }

  public void archive(Long id) {
    var account = lock(id, current.id());
    if (account.getBalance().signum() != 0) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Najpierw przenieś środki: zamknąć można konto z saldem 0 zł"
    );
    account.setArchived(true);
  }

  public AccountResponse restore(Long id) {
    var account = lock(id, current.id());
    account.setArchived(false);
    return response(account);
  }

  public Account lock(Long id, Long owner) {
    return accounts
      .lockOwned(id, owner)
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Nie znaleziono konta"
        )
      );
  }

  public void requireActive(Account account) {
    if (account.isArchived()) throw new IllegalArgumentException(
      "Konto jest zarchiwizowane"
    );
  }

  public static AccountResponse response(Account a) {
    return new AccountResponse(
      a.getId(),
      a.getName(),
      a.getAccountType(),
      a.getBalance(),
      a.isArchived(),
      a.getVersion()
    );
  }
}
