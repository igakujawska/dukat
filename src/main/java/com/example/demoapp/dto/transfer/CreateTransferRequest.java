package com.example.demoapp.dto.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Przelew między dwoma różnymi własnymi kontami.")
public record CreateTransferRequest(
  @NotNull
  @Schema(description = "Identyfikator własnego konta źródłowego.")
  Long sourceAccountId,
  @NotNull
  @Schema(description = "Identyfikator innego własnego konta docelowego.")
  Long targetAccountId,
  @NotNull
  @Positive
  @Digits(integer = 15, fraction = 2)
  @Schema(
    description = "Dodatnia kwota w PLN; najwyżej dwa miejsca po przecinku."
  )
  BigDecimal amount,
  @NotNull
  @Schema(description = "Data operacji w formacie YYYY-MM-DD.")
  LocalDate date,
  @NotBlank
  @Size(max = 255)
  @Schema(description = "Opis operacji wyświetlany w historii.")
  String description
) {}
