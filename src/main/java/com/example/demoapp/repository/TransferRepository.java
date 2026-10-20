package com.example.demoapp.repository;

import com.example.demoapp.model.Transfer;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
  List<Transfer> findAllByOwnerIdOrderByIdAsc(Long ownerId);
  Optional<Transfer> findByIdAndOwnerId(Long id, Long ownerId);
}
