package com.example.demoapp.init;

import com.example.demoapp.model.Account;
import com.example.demoapp.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import java.math.BigDecimal;

//do danych używanych np. przy samym uruchomieniu
public class DemoDataLoader implements CommandLineRunner {

    private final AccountRepository accountRepository;

    public DemoDataLoader(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // czy baza pusta, aby nie dublować kont przy restartach
        if (accountRepository.count() == 0) {
            Account account = new Account("Konto Główne PLN", "Konto Osobiste", new BigDecimal("0.00"));
            accountRepository.save(account);
            System.out.println("Utworzono przykładowe konto testowe o ID: " + account.getId());
        }
    }
}
