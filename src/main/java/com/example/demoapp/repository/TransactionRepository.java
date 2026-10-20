package com.example.demoapp.repository;

import com.example.demoapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findByTransactionDateGreaterThanEqualAndTransactionDateLessThan(LocalDate start, LocalDate end);
}
