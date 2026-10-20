package com.example.demoapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

// @Entity włącza mapowanie JPA. Tworzenie tabel zależy osobno od ustawienia ddl-auto.
@Entity
@Table(name = "accounts") //jawne nadanie nazwy tabeli
public class Account {

    @Id //ustawiam id jako klucz główny
    @GeneratedValue(strategy = GenerationType.IDENTITY) //db ma sama nadawac unikalne id nowym kontom
    private Long id;

    @Column(nullable = false, unique = true) // Zakaz SQL NULL i powtórzeń; pusty tekst sprawdza walidacja DTO.
    private String name;

    @Column(nullable = false)
    private String accountType;

    @Column(nullable = false)
    private BigDecimal balance;
    /* do reprezentowania pieniędzy wyłącznie typów BigDecimal (NIGDY double/float),
     bo typy zmiennoprzecinkowe jak double/float -> błędy zaokrągleń w binarnej arytmetyce komputera
     jak gdy 0.1 + 0.2 = 0.30000000000000004) */

    //Bezargumentowy konstruktor (wymagany przez JPA/hibernate)
    public Account() {} // Provider JPA (Hibernate) używa go podczas odtwarzania encji z bazy.

    //Konstruktor pomocniczy do tworzenia obiektów w kodzie
    //do wygodnego tworzenia gotowych obiektów np. w serwisach lub podczas testów
    public Account(String name, String accountType, BigDecimal balance) {
        this.name = name;
        this.accountType = accountType;
        this.balance = balance;
    }

    //Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
