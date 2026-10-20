package com.example.demoapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

// Wspólne id i właściciel. @MappedSuperclass mapuje pola w tabelach klas potomnych.
@MappedSuperclass
public abstract class OwnedResource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  @JsonIgnore
  private User owner;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }
}
