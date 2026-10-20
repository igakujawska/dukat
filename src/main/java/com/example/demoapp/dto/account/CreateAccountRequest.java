package com.example.demoapp.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// DTO opisuje dane wejściowe API. Klient nie ustawia id; nada je baza.
// record zapewnia konstruktor i metody name(), accountType(), balance().
public record CreateAccountRequest(
        @NotBlank(message = "Nazwa konta jest wymagana")
        @Size(max = 100, message = "Nazwa konta może mieć najwyżej 100 znaków")
        String name,
        @NotBlank(message = "Rodzaj konta jest wymagany")
        String accountType,
        @NotNull(message = "Saldo początkowe jest wymagane")
        @PositiveOrZero(message = "Saldo początkowe nie może być ujemne")
        BigDecimal balance
) {}
