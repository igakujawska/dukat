package com.example.demoapp.repository;

import com.example.demoapp.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsernameIgnoreCase(String username);
  Optional<User> findByEmailIgnoreCase(String email);
}
