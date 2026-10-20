package com.example.demoapp.dto.transaction;

import com.example.demoapp.model.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Wpis historii; przelew ma osobny typ i identyfikator.")
public record TransactionResponse(
  @Schema(description = "Identyfikator nadany przez bazę.") Long id,
  @Schema(description = "Opis operacji wyświetlany w historii.")
  String description,
  @Schema(
    description = "Dodatnia kwota w PLN; najwyżej dwa miejsca po przecinku."
  )
  BigDecimal amount,
  @Schema(description = "Data zaksięgowania operacji.")
  LocalDate transactionDate,
  @Schema(description = "Aktualna nazwa kategorii.") String category,
  @Schema(
    description = "Typ operacji; ręczny zapis dopuszcza tylko INCOME lub EXPENSE."
  )
  TransactionType type,
  @Schema(description = "Nazwa konta.") String accountName,
  @Schema(description = "Identyfikator własnego konta.") Long accountId,
  @Schema(description = "Identyfikator własnej, aktywnej kategorii.")
  Long categoryId,
  @Schema(description = "Identyfikator przelewu; null dla zwykłej transakcji.")
  Long transferId
) {}
