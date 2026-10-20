package com.example.demoapp.dto.transfer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateTransferRequest(
        @NotNull(message = "Konto źródłowe jest wymagane")
        Long sourceAccountId,
        @NotNull(message = "Konto docelowe jest wymagane")
        Long targetAccountId,
        @NotNull(message = "Kwota jest wymagana")
        @Positive(message = "Kwota przelewu musi być dodatnia")
        BigDecimal amount,
        String description
) {}
