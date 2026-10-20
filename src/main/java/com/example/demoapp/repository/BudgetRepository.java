package com.example.demoapp.repository;

import com.example.demoapp.model.Budget;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
  List<Budget> findAllByOwnerIdOrderByIdAsc(Long ownerId);
  Optional<Budget> findByIdAndOwnerId(Long id, Long ownerId);
  List<Budget> findAllByOwnerIdAndMonth(Long ownerId, String month);
}
