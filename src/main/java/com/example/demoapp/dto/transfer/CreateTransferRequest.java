package com.example.demoapp.dto.transfer;

import java.math.BigDecimal;

public record CreateTransferRequest(
        Long sourceAccountId,
        Long targetAccountId,
        BigDecimal amount,
        String description
) {}