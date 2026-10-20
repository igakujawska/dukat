package com.example.demoapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "households")
public class Household extends OwnedResource {

  @Column(nullable = false)
  private String name;

  public Household() {}

  public Household(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }
}
