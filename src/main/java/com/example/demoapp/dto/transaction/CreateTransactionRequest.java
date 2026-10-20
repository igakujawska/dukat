package com.example.demoapp.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

// DTO wejściowe: getter getAmount() należy do zwykłej klasy.
// Nie myl go z amount(), które kompilator wygenerowałby dla recordu.
public class CreateTransactionRequest {

    @NotNull(message = "Identyfikator konta jest wymagany")
    private Long accountId;

    // @NotNull odrzuca brak wartości; @Positive osobno odrzuca zero i liczby ujemne.
    @NotNull(message = "Kwota jest wymagana")
    @Positive(message = "Kwota transakcji musi być dodatnia")
    private BigDecimal amount;

    @NotNull(message = "Typ transakcji jest wymagany (INCOME lub EXPENSE)")
    @Pattern(regexp = "INCOME|EXPENSE", message = "Typ transakcji musi być INCOME lub EXPENSE")
    private String type;

    @NotNull(message = "Data jest wymagana")
    private LocalDate date;

    // @NotBlank odrzuca też pusty tekst i same spacje, czego @NotNull nie sprawdza.
    @NotBlank(message = "Kategoria jest wymagana")
    private String category;

    @NotBlank(message = "Opis jest wymagany")
    private String description;

    public CreateTransactionRequest() {}

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
