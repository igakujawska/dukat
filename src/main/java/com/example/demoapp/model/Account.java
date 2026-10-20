package com.example.demoapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "accounts")
public class Account extends OwnedResource {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String accountType;

  @Column(nullable = false)
  private BigDecimal balance;

  @Column(nullable = false)
  private boolean archived;

  @Version
  @Column(nullable = false)
  private long version;

  public Account() {}

  public Account(String name, String accountType, BigDecimal balance) {
    this.name = name;
    this.accountType = accountType;
    this.balance = balance;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public String getAccountType() {
    return accountType;
  }

  public void setAccountType(String value) {
    this.accountType = value;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal value) {
    this.balance = value;
  }

  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean value) {
    this.archived = value;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long value) {
    this.version = value;
  }
}
