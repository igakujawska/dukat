package com.example.demoapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private LocalDate transactionDate;

  @Column(nullable = false)
  private String category;

  @Enumerated(EnumType.STRING) // W bazie zapisujemy nazwę typu, np. EXPENSE lub TRANSFER_OUT, a nie numer enuma.
  @Column(nullable = false)
  private TransactionType type; // Przychód, wydatek lub strona przelewu.

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private Category categoryEntity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "transfer_id")
  private Transfer transfer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recurring_payment_id")
  private RecurringPayment recurringPayment;

  private LocalDate scheduledDate;

  public Category getCategoryEntity() {
    return categoryEntity;
  }

  public void setCategoryEntity(Category v) {
    categoryEntity = v;
    category = v.getName();
  }

  public Transfer getTransfer() {
    return transfer;
  }

  public void setTransfer(Transfer v) {
    transfer = v;
  }

  public RecurringPayment getRecurringPayment() {
    return recurringPayment;
  }

  public void setRecurringPayment(RecurringPayment v) {
    recurringPayment = v;
  }

  public LocalDate getScheduledDate() {
    return scheduledDate;
  }

  public void setScheduledDate(LocalDate v) {
    scheduledDate = v;
  }

  // Relacja: Wiele transakcji do jednego konta (Foreign Key account_id)
  @ManyToOne(fetch = FetchType.LAZY) // Wiele operacji wskazuje jedno konto; relacja może być doczytana później.
  @JoinColumn(name = "account_id", nullable = false) // Kolumna klucza obcego w tabeli transactions.
  private Account account;

  // 1. Pusty konstruktor wymagany przez JPA
  public Transaction() {}

  // 2. Konstruktor z parametrami dla naszej wygody
  public Transaction(
    String description,
    BigDecimal amount,
    LocalDate transactionDate,
    String category,
    TransactionType type,
    Account account
  ) {
    this.description = description;
    this.amount = amount;
    this.transactionDate = transactionDate;
    this.category = category;
    this.type = type;
    this.account = account;
  }

  // Gettery i Settery
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public LocalDate getTransactionDate() {
    return transactionDate;
  }

  public void setTransactionDate(LocalDate transactionDate) {
    this.transactionDate = transactionDate;
  }

  public String getCategory() {
    return categoryEntity == null ? category : categoryEntity.getName();
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public TransactionType getType() {
    return type;
  }

  public void setType(TransactionType type) {
    this.type = type;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }
}
