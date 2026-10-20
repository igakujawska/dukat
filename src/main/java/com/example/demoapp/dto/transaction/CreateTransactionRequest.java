package com.example.demoapp.dto.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

// DTO wejściowe: getter getAmount() należy do zwykłej klasy.
// Nie myl go z amount(), które kompilator wygenerowałby dla recordu.
@Schema(
  description = "Przychód lub wydatek na własnym koncie i w aktywnej kategorii."
)
public class CreateTransactionRequest {

  @NotNull(message = "Wybierz kategorię")
  private @Schema(
    description = "Identyfikator własnej, aktywnej kategorii."
  ) Long categoryId;

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long value) {
    categoryId = value;
  }

  @NotNull(message = "Identyfikator konta jest wymagany")
  private @Schema(description = "Identyfikator własnego konta.") Long accountId;

  // @NotNull odrzuca brak wartości; @Positive osobno odrzuca zero i liczby ujemne.
  @NotNull(message = "Kwota jest wymagana")
  @Positive(message = "Kwota transakcji musi być dodatnia")
  @Digits(
    integer = 15,
    fraction = 2,
    message = "Kwota ma maksymalnie 2 miejsca po przecinku"
  )
  private @Schema(
    description = "Dodatnia kwota w PLN; najwyżej dwa miejsca po przecinku."
  ) BigDecimal amount;

  @NotNull(message = "Typ transakcji jest wymagany (INCOME lub EXPENSE)")
  @Pattern(
    regexp = "INCOME|EXPENSE",
    message = "Typ transakcji musi być INCOME lub EXPENSE"
  )
  private @Schema(
    description = "Typ operacji; ręczny zapis dopuszcza tylko INCOME lub EXPENSE."
  ) String type;

  @NotNull(message = "Data jest wymagana")
  private @Schema(
    description = "Data operacji w formacie YYYY-MM-DD."
  ) LocalDate date;

  @NotBlank(message = "Opis jest wymagany")
  @Size(max = 255)
  private @Schema(
    description = "Opis operacji wyświetlany w historii."
  ) String description;

  public CreateTransactionRequest() {}

  public Long getAccountId() {
    return accountId;
  }

  public void setAccountId(
    @Schema(description = "Identyfikator własnego konta.") Long accountId
  ) {
    this.accountId = accountId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(
    @Schema(
      description = "Dodatnia kwota w PLN; najwyżej dwa miejsca po przecinku."
    ) BigDecimal amount
  ) {
    this.amount = amount;
  }

  public String getType() {
    return type;
  }

  public void setType(
    @Schema(
      description = "Typ operacji; ręczny zapis dopuszcza tylko INCOME lub EXPENSE."
    ) String type
  ) {
    this.type = type;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(
    @Schema(description = "Data operacji w formacie YYYY-MM-DD.") LocalDate date
  ) {
    this.date = date;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(
    @Schema(
      description = "Opis operacji wyświetlany w historii."
    ) String description
  ) {
    this.description = description;
  }
}
