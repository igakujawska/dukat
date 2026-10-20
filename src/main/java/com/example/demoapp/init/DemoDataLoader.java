package com.example.demoapp.init;

import com.example.demoapp.model.Account;
import com.example.demoapp.model.Category;
import com.example.demoapp.repository.AccountRepository;
import com.example.demoapp.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DemoDataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    public DemoDataLoader(CategoryRepository categoryRepository, AccountRepository accountRepository) {
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    new Category("Jedzenie i Restauracje"),
                    new Category("Mieszkanie i Rachunki"),
                    new Category("Transport"),
                    new Category("Rozrywka"),
                    new Category("Wynagrodzenie")
            ));
        }

        if (accountRepository.count() == 0) {
            accountRepository.save(new Account("Konto Główne", "PLN", new BigDecimal("5000.00")));
        }
    }
}