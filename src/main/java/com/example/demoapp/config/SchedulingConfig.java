package com.example.demoapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Włącza automatyczne zadania w tle
    // (np. cykliczne naliczanie płatności co miesiąc)
}