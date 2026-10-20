package com.example.demoapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budgets")
public class Budget extends OwnedResource {

  @Column(nullable = false)
  private String category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private Category categoryEntity;

  @Column(nullable = false)
  private BigDecimal monthlyLimit;

  @Column(nullable = false, length = 7)
  private String month;

  public Budget() {}

  public Budget(String category, BigDecimal monthlyLimit) {
    this.category = category;
    this.monthlyLimit = monthlyLimit;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String value) {
    this.category = value;
  }

  public Category getCategoryEntity() {
    return categoryEntity;
  }

  public void setCategoryEntity(Category value) {
    this.categoryEntity = value;
  }

  public BigDecimal getMonthlyLimit() {
    return monthlyLimit;
  }

  public void setMonthlyLimit(BigDecimal value) {
    this.monthlyLimit = value;
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String value) {
    this.month = value;
  }
}
