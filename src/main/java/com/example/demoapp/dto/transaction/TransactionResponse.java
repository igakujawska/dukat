package com.example.demoapp.dto.transaction;

import com.example.demoapp.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

//dane wysyłane do widoku czy tam frontendu

public record TransactionResponse(
        Long id,
        String description,
        BigDecimal amount,
        LocalDate transactionDate,
        String category,
        TransactionType type,
        String accountName
) {}
