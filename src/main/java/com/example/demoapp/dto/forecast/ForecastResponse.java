package com.example.demoapp.dto.forecast;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

// null oznacza brak podstaw do oszacowania, a nie wydatek równy zero.
@Schema(
  description = "Prognoza oparta wyłącznie na historii własnego użytkownika."
)
public record ForecastResponse(
  @Schema(description = "Miesiąc, dla którego wyznaczana jest prognoza.")
  String targetMonth,
  @Schema(description = "READY albo INSUFFICIENT_DATA.") String status,
  @Schema(
    description = "Wybrany model: średnia 3 miesięcy, trend liniowy lub ostatni miesiąc."
  )
  String method,
  @Schema(
    description = "Prognoza w PLN; null oznacza brak wystarczającej historii."
  )
  BigDecimal predictedExpenses,
  @Schema(description = "Liczba kolejnych pełnych miesięcy historii.")
  int historyMonths,
  @Schema(description = "Miesięczne wydatki sprzed prognozowanego miesiąca.")
  List<MonthlyExpense> history,
  @Schema(
    description = "MAE modeli w walidacji kroczącej; puste przy krótkiej historii."
  )
  List<ModelScore> scores,
  @Schema(description = "Liczba miesięcy walidacji.") int evaluationMonths,
  @Schema(description = "Założenia i ograniczenia prognozy.") String assumption
) {
  @Schema(description = "Agregat wydatków jednego miesiąca.")
  public record MonthlyExpense(
    @Schema(description = "Miesiąc w formacie YYYY-MM.") String month,
    @Schema(description = "Wydatki w PLN, bez przelewów.") BigDecimal expenses
  ) {}

  @Schema(description = "Wynik walidacji modelu.")
  public record ModelScore(
    @Schema(
      description = "Wybrany model: średnia 3 miesięcy, trend liniowy lub ostatni miesiąc."
    )
    String method,
    @Schema(
      description = "Średni błąd bezwzględny w PLN na wcześniejszych miesiącach."
    )
    BigDecimal mae
  ) {}
}
