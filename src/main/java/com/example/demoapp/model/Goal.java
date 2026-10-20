package com.example.demoapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "goals")
public class Goal extends OwnedResource {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private BigDecimal targetAmount;

  @Column(nullable = false)
  private BigDecimal currentAmount = BigDecimal.ZERO;

  private LocalDate targetDate;

  public Goal() {}

  public Goal(
    String name,
    BigDecimal targetAmount,
    BigDecimal currentAmount,
    LocalDate targetDate
  ) {
    this.name = name;
    this.targetAmount = targetAmount;
    this.currentAmount = currentAmount;
    this.targetDate = targetDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public BigDecimal getTargetAmount() {
    return targetAmount;
  }

  public void setTargetAmount(BigDecimal value) {
    this.targetAmount = value;
  }

  public BigDecimal getCurrentAmount() {
    return currentAmount;
  }

  public void setCurrentAmount(BigDecimal value) {
    this.currentAmount = value;
  }

  public LocalDate getTargetDate() {
    return targetDate;
  }

  public void setTargetDate(LocalDate value) {
    this.targetDate = value;
  }
}
