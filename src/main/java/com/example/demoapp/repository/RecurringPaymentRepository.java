package com.example.demoapp.repository;

import com.example.demoapp.model.RecurringPayment;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurringPaymentRepository
  extends JpaRepository<RecurringPayment, Long>
{
  List<RecurringPayment> findAllByOwnerIdOrderByIdAsc(Long ownerId);
  Optional<RecurringPayment> findByIdAndOwnerId(Long id, Long ownerId);
  List<RecurringPayment> findAllByActiveTrueAndNextDueDateLessThanEqual(
    java.time.LocalDate date
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select r from RecurringPayment r where r.id=:id")
  Optional<RecurringPayment> lockById(@Param("id") Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
    "select p from RecurringPayment p where p.id=:id and p.owner.id=:ownerId"
  )
  java.util.Optional<RecurringPayment> lockOwned(
    @Param("id") Long id,
    @Param("ownerId") Long ownerId
  );
}
