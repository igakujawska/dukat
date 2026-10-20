package com.example.demoapp.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// DTO opisuje dane wejściowe API. Klient nie ustawia id; nada je baza.
// record zapewnia konstruktor i metody name(), accountType(), balance().
@Schema(description = "Nowe konto i stan początkowy przed pierwszą operacją.")
public record CreateAccountRequest(
  @NotBlank(message = "Nazwa konta jest wymagana")
  @Size(max = 100, message = "Nazwa konta może mieć najwyżej 100 znaków")
  @Schema(description = "Nazwa widoczna w aplikacji.")
  String name,
  @NotBlank(message = "Rodzaj konta jest wymagany")
  @Size(max = 50)
  @Schema(description = "Rodzaj rachunku, np. Konto osobiste.")
  String accountType,
  @NotNull(message = "Saldo początkowe jest wymagane")
  @PositiveOrZero(message = "Saldo początkowe nie może być ujemne")
  @jakarta.validation.constraints.Digits(integer = 15, fraction = 2)
  @Schema(description = "Saldo w PLN, dokładność do dwóch miejsc po przecinku.")
  BigDecimal balance
) {}
