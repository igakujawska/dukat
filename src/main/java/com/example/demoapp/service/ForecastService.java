package com.example.demoapp.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ForecastService {

    // mieejsce pod integrację ze skryptami R czy tam prognozowaniem wydatków
    public Map<String, BigDecimal> predictNextMonthExpenses(Long accountId) {
        Map<String, BigDecimal> forecast = new HashMap<>();
        forecast.put("ESTIMATED_EXPENSES", new BigDecimal("2500.00"));
        return forecast;
    }
}