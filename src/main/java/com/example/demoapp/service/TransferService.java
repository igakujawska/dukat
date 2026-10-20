package com.example.demoapp.service;

import com.example.demoapp.dto.finance.FinanceDtos.TransferResponse;
import com.example.demoapp.dto.transfer.CreateTransferRequest;
import com.example.demoapp.model.*;
import com.example.demoapp.repository.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TransferService {

  private final AccountRepository accounts;
  private final TransferRepository transfers;
  private final TransactionRepository transactions;
  private final CategoryService categories;
  private final CurrentUser current;

  public TransferService(
    AccountRepository accounts,
    TransferRepository transfers,
    TransactionRepository transactions,
    CategoryService categories,
    CurrentUser current
  ) {
    this.accounts = accounts;
    this.transfers = transfers;
    this.transactions = transactions;
    this.categories = categories;
    this.current = current;
  }

  @Transactional(readOnly = true)
  public List<TransferResponse> list() {
    return transfers
      .findAllByOwnerIdOrderByIdAsc(current.id())
      .stream()
      .map(TransferService::response)
      .toList();
  }

  public TransferResponse executeTransfer(CreateTransferRequest request) {
    var owner = current.required();
    if (
      request.sourceAccountId().equals(request.targetAccountId())
    ) throw new IllegalArgumentException("Wybierz dwa różne konta");
    // Stała kolejność blokowania redukuje ryzyko deadlocków przy przelewach A→B i B→A.
    var locked = accounts.lockAccounts(
      owner.getId(),
      List.of(request.sourceAccountId(), request.targetAccountId())
    );
    if (locked.size() != 2) throw new ResponseStatusException(
      HttpStatus.NOT_FOUND,
      "Nie znaleziono własnych kont przelewu"
    );
    var source = locked
      .stream()
      .filter(a -> a.getId().equals(request.sourceAccountId()))
      .findFirst()
      .orElseThrow();
    var target = locked
      .stream()
      .filter(a -> a.getId().equals(request.targetAccountId()))
      .findFirst()
      .orElseThrow();
    if (
      source.isArchived() || target.isArchived()
    ) throw new IllegalArgumentException(
      "Nie można użyć zarchiwizowanego konta"
    );
    if (
      source.getBalance().compareTo(request.amount()) < 0
    ) throw new IllegalStateException("Brak wystarczających środków");
    source.setBalance(source.getBalance().subtract(request.amount()));
    target.setBalance(target.getBalance().add(request.amount()));
    var transfer = new Transfer();
    transfer.setOwner(owner);
    transfer.setSource(source);
    transfer.setTarget(target);
    transfer.setAmount(request.amount());
    transfer.setDate(request.date());
    transfer.setDescription(request.description().trim());
    transfers.save(transfer);
    var category = categories.transferCategory(owner);
    var out = new Transaction(
      request.description().trim(),
      request.amount(),
      request.date(),
      category.getName(),
      TransactionType.TRANSFER_OUT,
      source
    );
    out.setCategoryEntity(category);
    out.setTransfer(transfer);
    var in = new Transaction(
      request.description().trim(),
      request.amount(),
      request.date(),
      category.getName(),
      TransactionType.TRANSFER_IN,
      target
    );
    in.setCategoryEntity(category);
    in.setTransfer(transfer);
    transactions.saveAll(List.of(out, in));
    return response(transfer);
  }

  public static TransferResponse response(Transfer t) {
    return new TransferResponse(
      t.getId(),
      t.getSource().getId(),
      t.getSource().getName(),
      t.getTarget().getId(),
      t.getTarget().getName(),
      t.getAmount(),
      t.getDate(),
      t.getDescription()
    );
  }
}
