package com.example.demoapp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.demoapp.model.*;
import com.example.demoapp.repository.TransactionRepository;
import com.example.demoapp.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.*;

class TransactionServiceTest {

  private AccountService accounts;
  private CategoryService categories;
  private TransactionRepository transactions;
  private FinancialLedger ledger;
  private Account account;
  private Category category;

  @BeforeEach
  void setup() {
    accounts = mock(AccountService.class);
    categories = mock(CategoryService.class);
    transactions = mock(TransactionRepository.class);
    ledger = new FinancialLedger(accounts, categories, transactions);
    account = new Account("Główne", "PLN", new BigDecimal("1000.00"));
    account.setId(1L);
    category = new Category("Test");
    category.setId(2L);
    when(accounts.lock(1L, 7L)).thenReturn(account);
    when(categories.active(2L, 7L)).thenReturn(category);
    when(transactions.save(any())).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  void incomeUpdatesTheLockedOwnedAccount() {
    var row = post("500.00", TransactionType.INCOME);
    assertEquals(new BigDecimal("1500.00"), account.getBalance());
    assertSame(category, row.getCategoryEntity());
    verify(accounts).lock(1L, 7L);
  }

  @Test
  void expenseUpdatesTheLockedOwnedAccount() {
    post("200.00", TransactionType.EXPENSE);
    assertEquals(new BigDecimal("800.00"), account.getBalance());
  }

  @Test
  void precisionAndPositiveAmountAreCheckedBeforeChangingMoney() {
    assertThrows(IllegalArgumentException.class, () ->
      post("0.001", TransactionType.EXPENSE)
    );
    assertThrows(IllegalArgumentException.class, () ->
      post("-10", TransactionType.EXPENSE)
    );
    assertEquals(new BigDecimal("1000.00"), account.getBalance());
    verifyNoInteractions(transactions);
  }

  @Test
  void cannotForgeTransferThroughManualLedger() {
    assertThrows(IllegalArgumentException.class, () ->
      post("20", TransactionType.TRANSFER_IN)
    );
    verifyNoInteractions(transactions);
  }

  private Transaction post(String amount, TransactionType type) {
    return ledger.post(
      7L,
      1L,
      2L,
      new BigDecimal(amount),
      type,
      LocalDate.now(),
      "Test",
      null,
      null
    );
  }
}
