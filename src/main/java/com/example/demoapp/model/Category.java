package com.example.demoapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "categories")
public class Category extends OwnedResource {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private boolean active = true;

  public Category() {}

  public Category(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    this.name = value;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean value) {
    this.active = value;
  }
}
