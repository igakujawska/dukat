package com.example.demoapp.service;

import com.example.demoapp.dto.transaction.*;
import com.example.demoapp.model.*;
import com.example.demoapp.repository.TransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

  private final TransactionRepository transactions;
  private final FinancialLedger ledger;
  private final CurrentUser current;

  public TransactionService(
    TransactionRepository transactions,
    FinancialLedger ledger,
    CurrentUser current
  ) {
    this.transactions = transactions;
    this.ledger = ledger;
    this.current = current;
  }

  @Transactional(readOnly = true)
  public List<TransactionResponse> getAllTransactions() {
    return transactions
      .findAllByAccountOwnerIdOrderByTransactionDateDescIdDesc(current.id())
      .stream()
      .map(TransactionService::response)
      .toList();
  }

  @Transactional
  public TransactionResponse createTransaction(
    CreateTransactionRequest request
  ) {
    return response(
      ledger.post(
        current.id(),
        request.getAccountId(),
        request.getCategoryId(),
        request.getAmount(),
        TransactionType.valueOf(request.getType()),
        request.getDate(),
        request.getDescription(),
        null,
        null
      )
    );
  }

  public static TransactionResponse response(Transaction t) {
    return new TransactionResponse(
      t.getId(),
      t.getDescription(),
      t.getAmount(),
      t.getTransactionDate(),
      t.getCategory(),
      t.getType(),
      t.getAccount().getName(),
      t.getAccount().getId(),
      t.getCategoryEntity().getId(),
      t.getTransfer() == null ? null : t.getTransfer().getId()
    );
  }
}
