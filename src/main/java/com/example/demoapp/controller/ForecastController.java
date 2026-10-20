package com.example.demoapp.controller;

import com.example.demoapp.dto.forecast.ForecastResponse;
import com.example.demoapp.service.ForecastService;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/forecasts")
public class ForecastController {
    private final ForecastService forecasts;
    public ForecastController(ForecastService forecasts) { this.forecasts = forecasts; }

    @GetMapping
    public ForecastResponse forecast(@RequestParam(required = false) String month) {
        try {
            return forecasts.forecast(month == null ? YearMonth.now() : YearMonth.parse(month));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Miesiąc musi mieć format RRRR-MM, np. 2026-10.");
        }
    }
}
