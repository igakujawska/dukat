package com.example.demoapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transfers")
public class Transfer extends OwnedResource {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_account_id", nullable = false)
  private Account source;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_account_id", nullable = false)
  private Account target;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private String description;

  public Transfer() {}

  public Account getSource() {
    return source;
  }

  public void setSource(Account v) {
    source = v;
  }

  public Account getTarget() {
    return target;
  }

  public void setTarget(Account v) {
    target = v;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal v) {
    amount = v;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate v) {
    date = v;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String v) {
    description = v;
  }
}
