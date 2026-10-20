package com.example.demoapp.repository;

import com.example.demoapp.model.Transaction;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository
  extends JpaRepository<Transaction, Long>
{
  @EntityGraph(attributePaths = { "account", "categoryEntity" })
  List<Transaction> findAllByAccountOwnerIdOrderByTransactionDateDescIdDesc(
    Long ownerId
  );

  @EntityGraph(attributePaths = { "account", "categoryEntity" })
  List<Transaction> findByAccountOwnerIdAndTransactionDateGreaterThanEqualAndTransactionDateLessThan(
    Long ownerId,
    LocalDate start,
    LocalDate end
  );

  boolean existsByRecurringPaymentIdAndScheduledDate(
    Long recurringPaymentId,
    LocalDate date
  );
}
