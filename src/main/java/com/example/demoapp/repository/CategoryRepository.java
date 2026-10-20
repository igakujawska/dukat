package com.example.demoapp.repository;

import com.example.demoapp.model.Category;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findAllByOwnerIdOrderByIdAsc(Long ownerId);
  Optional<Category> findByIdAndOwnerId(Long id, Long ownerId);
  Optional<Category> findByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
}
