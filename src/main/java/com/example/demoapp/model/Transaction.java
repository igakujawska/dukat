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

    @Enumerated(EnumType.STRING) // W bazie zapisujemy nazwę INCOME/EXPENSE, a nie numer stałej enuma.
    @Column(nullable = false)
    private TransactionType type; // INCOME lub EXPENSE

    // Relacja: Wiele transakcji do jednego konta (Foreign Key account_id)
    @ManyToOne(fetch = FetchType.LAZY) // Wiele operacji wskazuje jedno konto; relacja może być doczytana później.
    @JoinColumn(name = "account_id", nullable = false) // Kolumna klucza obcego w tabeli transactions.
    private Account account;

    // 1. Pusty konstruktor wymagany przez JPA
    public Transaction() {}

    // 2. Konstruktor z parametrami dla naszej wygody
    public Transaction(String description, BigDecimal amount, LocalDate transactionDate,
                       String category, TransactionType type, Account account) {
        this.description = description;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.category = category;
        this.type = type;
        this.account = account;
    }

    // Gettery i Settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
}
