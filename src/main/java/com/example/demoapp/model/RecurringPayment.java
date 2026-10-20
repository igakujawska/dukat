package com.example.demoapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_payments")
public class RecurringPayment extends OwnedResource {

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private String frequency;

  @Column(nullable = false)
  private LocalDate nextDueDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category categoryEntity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionType type = TransactionType.EXPENSE;

  @Column(nullable = false)
  private boolean active = true;

  private String lastError;

  @Column(nullable = false)
  private int anchorDay = 1;

  public int getAnchorDay() {
    return anchorDay;
  }

  public void setAnchorDay(int value) {
    anchorDay = value;
  }

  public RecurringPayment() {}

  public RecurringPayment(
    String title,
    BigDecimal amount,
    String frequency,
    LocalDate nextDueDate
  ) {
    this.title = title;
    this.amount = amount;
    this.frequency = frequency;
    this.nextDueDate = nextDueDate;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    this.title = value;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    this.amount = value;
  }

  public String getFrequency() {
    return frequency;
  }

  public void setFrequency(String value) {
    this.frequency = value;
  }

  public LocalDate getNextDueDate() {
    return nextDueDate;
  }

  public void setNextDueDate(LocalDate value) {
    this.nextDueDate = value;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account value) {
    this.account = value;
  }

  public Category getCategoryEntity() {
    return categoryEntity;
  }

  public void setCategoryEntity(Category value) {
    this.categoryEntity = value;
  }

  public TransactionType getType() {
    return type;
  }

  public void setType(TransactionType value) {
    this.type = value;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean value) {
    this.active = value;
  }

  public String getLastError() {
    return lastError;
  }

  public void setLastError(String value) {
    this.lastError = value;
  }
}
