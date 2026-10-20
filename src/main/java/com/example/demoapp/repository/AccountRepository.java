package com.example.demoapp.repository;

import com.example.demoapp.model.Account;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
  List<Account> findAllByOwnerIdOrderByIdAsc(Long ownerId);
  Optional<Account> findByIdAndOwnerId(Long id, Long ownerId);

  boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Account a where a.id=:id and a.owner.id=:ownerId")
  Optional<Account> lockOwned(
    @Param("id") Long id,
    @Param("ownerId") Long ownerId
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
    "select a from Account a where a.owner.id=:ownerId and a.id in :ids order by a.id"
  )
  List<Account> lockAccounts(
    @Param("ownerId") Long ownerId,
    @Param("ids") List<Long> ids
  );
}
