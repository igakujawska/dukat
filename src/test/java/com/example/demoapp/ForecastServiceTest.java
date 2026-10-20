package com.example.demoapp;

import com.example.demoapp.model.Transaction;
import com.example.demoapp.model.TransactionType;
import com.example.demoapp.repository.TransactionRepository;
import com.example.demoapp.service.ForecastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForecastServiceTest {
    private final YearMonth target = YearMonth.of(2020, 10);
    private TransactionRepository repository;
    private ForecastService service;

    @BeforeEach
    void setup() { repository = mock(TransactionRepository.class); service = new ForecastService(repository); }

    @Test
    void abstainsWhenHistoryIsTooShort() {
        history(100, 200);
        var result = service.forecast(target);
        assertEquals("INSUFFICIENT_DATA", result.status());
        assertNull(result.predictedExpenses());
        assertEquals(2, result.historyMonths());
    }

    @Test
    void selectsLinearTrendForLinearDataUsingPastOnlyFolds() {
        history(100, 200, 300, 400, 500, 600);
        var result = service.forecast(target);
        assertEquals("LINEAR_TREND", result.method());
        assertEquals(new BigDecimal("700.00"), result.predictedExpenses());
        assertEquals(3, result.evaluationMonths());
        assertEquals(new BigDecimal("0.00"), result.scores().get(1).mae());
        assertEquals(new BigDecimal("200.00"), result.scores().get(0).mae());
        verify(repository).findByTransactionDateGreaterThanEqualAndTransactionDateLessThan(
                target.minusMonths(12).atDay(1), target.atDay(1));
    }

    @Test
    void usesMovingAverageUntilEnoughEvaluationMonthsExist() {
        history(300, 600, 900);
        var result = service.forecast(target);
        assertEquals(new BigDecimal("600.00"), result.predictedExpenses());
        assertEquals("MOVING_AVERAGE_3", result.method());
        assertTrue(result.scores().isEmpty());
    }

    @Test
    void missingMonthIsNotSilentlyImputedAsZero() {
        List<Transaction> rows = new ArrayList<>();
        rows.add(expense(target.minusMonths(1), 100));
        rows.add(expense(target.minusMonths(3), 900));
        rows.add(expense(target.minusMonths(4), 900));
        when(repository.findByTransactionDateGreaterThanEqualAndTransactionDateLessThan(any(), any())).thenReturn(rows);
        var result = service.forecast(target);
        assertEquals(1, result.historyMonths());
        assertNull(result.predictedExpenses());
    }

    @Test
    void incomeOnlyMonthsHaveZeroRecordedExpenses() {
        List<Transaction> rows = new ArrayList<>();
        for (int i = 1; i <= 3; i++) rows.add(new Transaction("Pensja", BigDecimal.TEN,
                target.minusMonths(i).atDay(1), "Praca", TransactionType.INCOME, null));
        when(repository.findByTransactionDateGreaterThanEqualAndTransactionDateLessThan(any(), any())).thenReturn(rows);
        var result = service.forecast(target);
        assertEquals("READY", result.status());
        assertEquals(new BigDecimal("0.00"), result.predictedExpenses());
    }

    @Test
    void rejectsFutureTargetRatherThanUsingIncompleteMonths() {
        assertThrows(IllegalArgumentException.class, () -> service.forecast(YearMonth.now().plusMonths(1)));
        verifyNoInteractions(repository);
    }

    private void history(int... amounts) {
        List<Transaction> rows = new ArrayList<>();
        for (int i = 0; i < amounts.length; i++) rows.add(expense(target.minusMonths(amounts.length - i), amounts[i]));
        when(repository.findByTransactionDateGreaterThanEqualAndTransactionDateLessThan(any(), any())).thenReturn(rows);
    }

    private Transaction expense(YearMonth month, int amount) {
        return new Transaction("Wydatek", BigDecimal.valueOf(amount), month.atDay(12), "Test", TransactionType.EXPENSE, null);
    }
}
