package com.example.demoapp.service;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.model.*;
import com.example.demoapp.repository.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class RecurringPaymentService {

  private final RecurringPaymentRepository payments;
  private final AccountRepository accounts;
  private final CategoryService categories;
  private final CurrentUser current;
  private final RecurringProcessor processor;

  public RecurringPaymentService(
    RecurringPaymentRepository payments,
    AccountRepository accounts,
    CategoryService categories,
    CurrentUser current,
    RecurringProcessor processor
  ) {
    this.payments = payments;
    this.accounts = accounts;
    this.categories = categories;
    this.current = current;
    this.processor = processor;
  }

  @Transactional(readOnly = true)
  public List<RecurringResponse> list() {
    return payments
      .findAllByOwnerIdOrderByIdAsc(current.id())
      .stream()
      .map(RecurringPaymentService::response)
      .toList();
  }

  public RecurringResponse create(RecurringRequest r) {
    var p = new RecurringPayment();
    p.setOwner(current.required());
    apply(p, r);
    return response(payments.save(p));
  }

  public RecurringResponse update(Long id, RecurringRequest r) {
    var p = owned(id);
    apply(p, r);
    return response(p);
  }

  public void pause(Long id) {
    owned(id).setActive(false);
  }

  public int execute(Long id) {
    owned(id);
    return processor.execute(id);
  }

  private RecurringPayment owned(Long id) {
    return payments
      .lockOwned(id, current.id())
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Nie znaleziono płatności"
        )
      );
  }

  private void apply(RecurringPayment p, RecurringRequest r) {
    var account = accounts
      .findByIdAndOwnerId(r.accountId(), current.id())
      .filter(a -> !a.isArchived())
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Nie znaleziono aktywnego konta"
        )
      );
    p.setTitle(r.title().trim());
    p.setAmount(r.amount());
    p.setFrequency(r.frequency());
    if (
      p.getNextDueDate() == null || !p.getNextDueDate().equals(r.nextDueDate())
    ) p.setAnchorDay(r.nextDueDate().getDayOfMonth());
    p.setNextDueDate(r.nextDueDate());
    p.setAccount(account);
    p.setCategoryEntity(categories.active(r.categoryId(), current.id()));
    p.setType(TransactionType.valueOf(r.type()));
    p.setActive(r.active());
    p.setLastError(null);
  }

  public void recordFailure(Long id, String reason) {
    payments
      .findById(id)
      .ifPresent(p ->
        p.setLastError(
          reason == null
            ? "Błąd księgowania"
            : reason.substring(0, Math.min(250, reason.length()))
        )
      );
  }

  private static RecurringResponse response(RecurringPayment p) {
    return new RecurringResponse(
      p.getId(),
      p.getTitle(),
      p.getAmount(),
      p.getFrequency(),
      p.getNextDueDate(),
      p.getAccount() == null ? null : p.getAccount().getId(),
      p.getAccount() == null ? "Uzupełnij konto" : p.getAccount().getName(),
      p.getCategoryEntity() == null ? null : p.getCategoryEntity().getId(),
      p.getCategoryEntity() == null
        ? "Uzupełnij kategorię"
        : p.getCategoryEntity().getName(),
      p.getType().name(),
      p.isActive(),
      p.getLastError()
    );
  }
}
