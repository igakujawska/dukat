package com.example.demoapp.service;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.model.*;
import com.example.demoapp.repository.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class BudgetService {

  private final BudgetRepository budgets;
  private final TransactionRepository transactions;
  private final CategoryService categories;
  private final CurrentUser current;

  public BudgetService(
    BudgetRepository budgets,
    TransactionRepository transactions,
    CategoryService categories,
    CurrentUser current
  ) {
    this.budgets = budgets;
    this.transactions = transactions;
    this.categories = categories;
    this.current = current;
  }

  @Transactional(readOnly = true)
  public List<BudgetResponse> list(String month) {
    var period = YearMonth.parse(month);
    Long owner = current.id();
    Map<Long, BigDecimal> spent = new HashMap<>();
    for (var t : transactions.findByAccountOwnerIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
      owner,
      period.atDay(1),
      period.plusMonths(1).atDay(1)
    )) {
      if (t.getType() == TransactionType.EXPENSE) spent.merge(
        t.getCategoryEntity().getId(),
        t.getAmount(),
        BigDecimal::add
      );
    }
    return budgets
      .findAllByOwnerIdAndMonth(owner, month)
      .stream()
      .map(b ->
        response(
          b,
          spent.getOrDefault(b.getCategoryEntity().getId(), BigDecimal.ZERO)
        )
      )
      .toList();
  }

  public BudgetResponse create(BudgetRequest request) {
    var b = new Budget();
    b.setOwner(current.required());
    apply(b, request);
    budgets.save(b);
    return response(b, spentFor(b));
  }

  public BudgetResponse update(Long id, BudgetRequest request) {
    var b = owned(id);
    apply(b, request);
    return response(b, spentFor(b));
  }

  public void delete(Long id) {
    budgets.delete(owned(id));
  }

  private Budget owned(Long id) {
    return budgets
      .findByIdAndOwnerId(id, current.id())
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Nie znaleziono budżetu"
        )
      );
  }

  private void apply(Budget b, BudgetRequest r) {
    YearMonth.parse(r.month());
    var category = categories.active(r.categoryId(), current.id());
    b.setCategoryEntity(category);
    b.setCategory(category.getName());
    b.setMonth(r.month());
    b.setMonthlyLimit(r.monthlyLimit());
  }

  private BigDecimal spentFor(Budget b) {
    var period = YearMonth.parse(b.getMonth());
    return transactions
      .findByAccountOwnerIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
        current.id(),
        period.atDay(1),
        period.plusMonths(1).atDay(1)
      )
      .stream()
      .filter(
        t ->
          t.getType() == TransactionType.EXPENSE &&
          t.getCategoryEntity().getId().equals(b.getCategoryEntity().getId())
      )
      .map(Transaction::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BudgetResponse response(Budget b, BigDecimal spent) {
    return new BudgetResponse(
      b.getId(),
      b.getCategoryEntity().getId(),
      b.getCategoryEntity().getName(),
      b.getMonth(),
      b.getMonthlyLimit(),
      spent,
      b.getMonthlyLimit().subtract(spent)
    );
  }
}
