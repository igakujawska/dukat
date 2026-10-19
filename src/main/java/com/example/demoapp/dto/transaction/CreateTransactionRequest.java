package com.example.demoapp.dto.transaction;

import com.example.demoapp.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

//dane z formularza dodawania transakcji

public record CreateTransactionRequest(
        String description,
        BigDecimal amount,
        LocalDate transactionDate,
        String category,
        TransactionType type,
        Long accountId
) {}
