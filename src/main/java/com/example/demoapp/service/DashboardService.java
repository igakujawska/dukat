package com.example.demoapp.service;

import com.example.demoapp.repository.*;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

  private final AccountRepository accounts;
  private final TransactionRepository transactions;
  private final CurrentUser current;

  public DashboardService(
    AccountRepository accounts,
    TransactionRepository transactions,
    CurrentUser current
  ) {
    this.accounts = accounts;
    this.transactions = transactions;
    this.current = current;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getSummary() {
    return Map.of(
      "accountsCount",
      accounts.findAllByOwnerIdOrderByIdAsc(current.id()).size(),
      "transactionsCount",
      transactions
        .findAllByAccountOwnerIdOrderByTransactionDateDescIdDesc(current.id())
        .size()
    );
  }
}
