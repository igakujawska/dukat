package com.example.demoapp.dto.forecast;

import java.math.BigDecimal;
import java.util.List;

// null oznacza brak podstaw do oszacowania, a nie wydatek równy zero.
public record ForecastResponse(
        String targetMonth, String status, String method, BigDecimal predictedExpenses,
        int historyMonths, List<MonthlyExpense> history, List<ModelScore> scores,
        int evaluationMonths, String assumption
) {
    public record MonthlyExpense(String month, BigDecimal expenses) {}
    public record ModelScore(String method, BigDecimal mae) {}
}
