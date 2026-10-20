package com.example.demoapp.repository;

import com.example.demoapp.model.Goal;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
  List<Goal> findAllByOwnerIdOrderByIdAsc(Long ownerId);
  Optional<Goal> findByIdAndOwnerId(Long id, Long ownerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select g from Goal g where g.id=:id and g.owner.id=:ownerId")
  Optional<Goal> lockOwned(
    @Param("id") Long id,
    @Param("ownerId") Long ownerId
  );
}
