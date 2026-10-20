package com.example.demoapp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.demoapp.model.*;
import com.example.demoapp.repository.*;
import com.example.demoapp.service.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

class RecurringProcessorTest {

  private RecurringPaymentRepository payments;
  private TransactionRepository transactions;
  private FinancialLedger ledger;
  private RecurringProcessor processor;
  private RecurringPayment payment;

  @BeforeEach
  void setup() {
    payments = mock(RecurringPaymentRepository.class);
    transactions = mock(TransactionRepository.class);
    ledger = mock(FinancialLedger.class);
    processor = new RecurringProcessor(payments, transactions, ledger);
    payment = new RecurringPayment();
    payment.setId(5L);
    var owner = new User();
    owner.setId(7L);
    var account = new Account();
    account.setId(1L);
    var category = new Category();
    category.setId(2L);
    payment.setOwner(owner);
    payment.setAccount(account);
    payment.setCategoryEntity(category);
    payment.setAmount(BigDecimal.TEN);
    payment.setTitle("Cycle");
    payment.setFrequency("MONTHLY");
    payment.setActive(true);
    payment.setAnchorDay(31);
    when(payments.lockById(5L)).thenReturn(Optional.of(payment));
  }

  @Test
  void preservesEndOfMonthAndLimitsCatchUpBatch() {
    int year = LocalDate.now().getYear() - 2;
    payment.setNextDueDate(LocalDate.of(year, 1, 31));
    assertEquals(12, processor.execute(5L));
    var dates = ArgumentCaptor.forClass(LocalDate.class);
    verify(ledger, times(12)).post(
      eq(7L),
      eq(1L),
      eq(2L),
      eq(BigDecimal.TEN),
      eq(TransactionType.EXPENSE),
      dates.capture(),
      eq("Cycle"),
      eq(payment),
      any()
    );
    assertEquals(
      YearMonth.of(year, 2).atEndOfMonth(),
      dates.getAllValues().get(1)
    );
    assertEquals(LocalDate.of(year, 3, 31), dates.getAllValues().get(2));
  }

  @Test
  void skipsPreviouslyPostedOccurrences() {
    payment.setNextDueDate(LocalDate.now());
    when(
      transactions.existsByRecurringPaymentIdAndScheduledDate(
        5L,
        LocalDate.now()
      )
    ).thenReturn(true);
    assertEquals(0, processor.execute(5L));
    verifyNoInteractions(ledger);
    assertTrue(payment.getNextDueDate().isAfter(LocalDate.now()));
  }

  @Test
  void futureAndPausedPaymentsDoNotChangeMoney() {
    payment.setNextDueDate(LocalDate.now().plusDays(1));
    assertEquals(0, processor.execute(5L));
    payment.setActive(false);
    payment.setNextDueDate(LocalDate.now().minusDays(1));
    assertEquals(0, processor.execute(5L));
    verifyNoInteractions(ledger);
  }
}
