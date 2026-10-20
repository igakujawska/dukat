package com.example.demoapp.service;

import com.example.demoapp.model.*;
import com.example.demoapp.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Jedno miejsce zmienia saldo wraz z historią. Używane także przez harmonogram.
@Service
public class FinancialLedger {

  private final AccountService accounts;
  private final CategoryService categories;
  private final TransactionRepository transactions;

  public FinancialLedger(
    AccountService accounts,
    CategoryService categories,
    TransactionRepository transactions
  ) {
    this.accounts = accounts;
    this.categories = categories;
    this.transactions = transactions;
  }

  @Transactional
  public Transaction post(
    Long owner,
    Long accountId,
    Long categoryId,
    BigDecimal amount,
    TransactionType type,
    LocalDate date,
    String description,
    RecurringPayment recurring,
    LocalDate scheduledDate
  ) {
    if (
      amount == null || amount.signum() <= 0 || amount.scale() > 2
    ) throw new IllegalArgumentException(
      "Podaj dodatnią kwotę z maks. 2 miejscami po przecinku"
    );
    if (
      type != TransactionType.INCOME && type != TransactionType.EXPENSE
    ) throw new IllegalArgumentException("Nieprawidłowy typ operacji");
    var account = accounts.lock(accountId, owner);
    accounts.requireActive(account);
    var category = categories.active(categoryId, owner);
    account.setBalance(
      type == TransactionType.INCOME
        ? account.getBalance().add(amount)
        : account.getBalance().subtract(amount)
    );
    var transaction = new Transaction(
      description.trim(),
      amount,
      date,
      category.getName(),
      type,
      account
    );
    transaction.setCategoryEntity(category);
    transaction.setRecurringPayment(recurring);
    transaction.setScheduledDate(scheduledDate);
    return transactions.save(transaction);
  }
}
