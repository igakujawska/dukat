package com.example.demoapp.service;

import com.example.demoapp.repository.*;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecurringProcessor {

  private final RecurringPaymentRepository payments;
  private final TransactionRepository transactions;
  private final FinancialLedger ledger;

  public RecurringProcessor(
    RecurringPaymentRepository payments,
    TransactionRepository transactions,
    FinancialLedger ledger
  ) {
    this.payments = payments;
    this.transactions = transactions;
    this.ledger = ledger;
  }

  // Osobny bean: wywołanie z harmonogramu przechodzi przez proxy @Transactional.
  @Transactional
  public int execute(Long id) {
    var payment = payments.lockById(id).orElseThrow();
    int count = 0;
    int posted = 0;
    var today = LocalDate.now();
    while (
      payment.isActive() &&
      !payment.getNextDueDate().isAfter(today) &&
      count < 12
    ) {
      var due = payment.getNextDueDate();
      if (
        payment.getAccount() == null || payment.getCategoryEntity() == null
      ) throw new IllegalArgumentException(
        "Uzupełnij konto i kategorię płatności"
      );
      if (!transactions.existsByRecurringPaymentIdAndScheduledDate(id, due)) {
        ledger.post(
          payment.getOwner().getId(),
          payment.getAccount().getId(),
          payment.getCategoryEntity().getId(),
          payment.getAmount(),
          payment.getType(),
          due,
          payment.getTitle(),
          payment,
          due
        );
        posted++;
      }
      payment.setNextDueDate(
        switch (payment.getFrequency()) {
          case "WEEKLY" -> due.plusWeeks(1);
          case "MONTHLY" -> anchored(due.plusMonths(1), payment.getAnchorDay());
          case "YEARLY" -> anchored(due.plusYears(1), payment.getAnchorDay());
          default -> throw new IllegalArgumentException(
            "Nieznana częstotliwość"
          );
        }
      );
      payment.setLastError(null);
      count++;
    }
    return posted;
  }

  private LocalDate anchored(LocalDate next, int day) {
    return next.withDayOfMonth(Math.min(day, next.lengthOfMonth()));
  }
}
