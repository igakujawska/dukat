package com.example.demoapp.repository;

import com.example.demoapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// Interfejs deklaruje operacje. Implementację CRUD tworzy Spring Data JPA.
// Account to typ encji, Long to typ jej klucza głównego.
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Spring Data interpretuje nazwę metody jako zapytanie po polu name.
    Optional<Account> findByName(String name);
}
