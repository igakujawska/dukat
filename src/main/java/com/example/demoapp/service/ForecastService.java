package com.example.demoapp.service;

import com.example.demoapp.dto.forecast.ForecastResponse;
import com.example.demoapp.dto.forecast.ForecastResponse.ModelScore;
import com.example.demoapp.dto.forecast.ForecastResponse.MonthlyExpense;
import com.example.demoapp.model.TransactionType;
import com.example.demoapp.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;

@Service
public class ForecastService {

    private static final String MEAN = "MOVING_AVERAGE_3";
    private static final String TREND = "LINEAR_TREND";
    private static final String NAIVE = "LAST_MONTH";
    private static final String ASSUMPTION = "Zakładamy kompletność zapisów w miesiącach z operacjami. "
            + "Miesiąc bez żadnej operacji przerywa historię; miesiąc tylko z przychodami ma 0 zł wydatków. "
            + "Model nie uwzględnia sezonowości ani planowanych rachunków.";
    private final TransactionRepository transactions;

    public ForecastService(TransactionRepository transactions) { this.transactions = transactions; }

    // readOnly określa odczytowy charakter transakcji. Model nie zmienia sald ani historii.
    @Transactional(readOnly = true)
    public ForecastResponse forecast(YearMonth target) {
        if (target.isAfter(YearMonth.now())) {
            throw new IllegalArgumentException("Wybierz miesiąc bieżący lub wcześniejszy. Prognoza ma horyzont jednego miesiąca.");
        }
        var rows = transactions.findByTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                target.minusMonths(12).atDay(1), target.atDay(1));
        Map<YearMonth, BigDecimal> totals = new HashMap<>();
        for (var row : rows) {
            YearMonth month = YearMonth.from(row.getTransactionDate());
            totals.putIfAbsent(month, BigDecimal.ZERO);
            if (row.getType() == TransactionType.EXPENSE) totals.merge(month, row.getAmount(), BigDecimal::add);
        }
        // Wyłącznie kolejne zamknięte miesiące przed celem: brak danych != 0.
        LinkedList<MonthlyExpense> history = new LinkedList<>();
        for (int offset = 1; offset <= 12; offset++) {
            YearMonth month = target.minusMonths(offset);
            if (!totals.containsKey(month)) break;
            history.addFirst(new MonthlyExpense(month.toString(), totals.get(month).setScale(2, RoundingMode.HALF_UP)));
        }
        if (history.size() < 3) {
            return new ForecastResponse(target.toString(), "INSUFFICIENT_DATA", MEAN, null,
                    history.size(), List.copyOf(history), List.of(), 0, ASSUMPTION);
        }
        List<Double> values = history.stream().map(point -> point.expenses().doubleValue()).toList();
        List<ModelScore> scores = new ArrayList<>();
        int folds = values.size() - 3;
        String selected = MEAN;
        if (folds >= 3) {
            // Walk-forward: każdy model widzi wyłącznie wcześniejsze miesiące.
            // MAE = średni bezwzględny błąd w PLN na tych samych miesiącach.
            for (String method : List.of(MEAN, TREND, NAIVE)) {
                double errors = 0;
                for (int i = 3; i < values.size(); i++) {
                    errors += Math.abs(predict(values.subList(0, i), method) - values.get(i));
                }
                scores.add(new ModelScore(method, money(errors / folds)));
            }
            selected = scores.stream().min(Comparator.comparing(ModelScore::mae)).orElseThrow().method();
        }
        return new ForecastResponse(target.toString(), "READY", selected, money(predict(values, selected)),
                history.size(), List.copyOf(history), List.copyOf(scores), folds >= 3 ? folds : 0, ASSUMPTION);
    }

    private double predict(List<Double> values, String method) {
        int size = values.size();
        if (NAIVE.equals(method)) return values.get(size - 1);
        if (MEAN.equals(method)) return values.subList(size - 3, size).stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        // Regresja liniowa OLS dopasowuje nachylenie i wyraz wolny do maks. 6 miesięcy.
        var window = values.subList(Math.max(0, size - 6), size);
        double xMean = (window.size() - 1) / 2.0;
        double yMean = window.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        double numerator = 0, denominator = 0;
        for (int i = 0; i < window.size(); i++) {
            numerator += (i - xMean) * (window.get(i) - yMean);
            denominator += (i - xMean) * (i - xMean);
        }
        return Math.max(0, yMean + (numerator / denominator) * (window.size() - xMean));
    }

    private BigDecimal money(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
