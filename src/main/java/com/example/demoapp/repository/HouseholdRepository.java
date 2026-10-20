package com.example.demoapp.repository;

import com.example.demoapp.model.Household;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {
  List<Household> findAllByOwnerIdOrderByIdAsc(Long ownerId);
  Optional<Household> findByIdAndOwnerId(Long id, Long ownerId);
}
