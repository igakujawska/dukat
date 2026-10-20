package com.example.demoapp.init;

import com.example.demoapp.model.*;
import com.example.demoapp.repository.UserRepository;
import com.example.demoapp.service.CategoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataLoader implements CommandLineRunner {

  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final CategoryService categories;
  private final JdbcTemplate jdbc;

  @Value("${app.bootstrap.enabled:true}")
  private boolean enabled;

  @Value("${app.bootstrap.username:user}")
  private String username;

  @Value("${app.bootstrap.password:}")
  private String password;

  @Value("${app.bootstrap.email:user@dukat.local}")
  private String email;

  public DemoDataLoader(
    UserRepository users,
    PasswordEncoder passwords,
    CategoryService categories,
    JdbcTemplate jdbc
  ) {
    this.users = users;
    this.passwords = passwords;
    this.categories = categories;
    this.jdbc = jdbc;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (!enabled) return;
    if (password.length() < 10) throw new IllegalArgumentException(
      "Ustaw DUKAT_BOOTSTRAP_PASSWORD (minimum 10 znaków)"
    );
    User owner = users
      .findByUsernameIgnoreCase(username)
      .orElseGet(() ->
        users.saveAndFlush(
          new User(username, passwords.encode(password), email, Role.USER)
        )
      );
    // Jednorazowe przeniesienie zachowanych danych; nowo rejestrowane osoby ich nie zobaczą.
    users.findByUsernameIgnoreCase("__legacy_owner__").ifPresent(legacy -> {
      for (String table : java.util.List.of(
        "accounts",
        "categories",
        "budgets",
        "goals",
        "recurring_payments",
        "households"
      ))
        jdbc.update(
          "UPDATE " + table + " SET owner_id=? WHERE owner_id=?",
          owner.getId(),
          legacy.getId()
        );
    });
    // Stary endpoint zapisywał hasła jawnie. Aktualizujemy ich format przed udostępnieniem logowania.
    for (User user : users.findAll()) {
      if (
        user.isEnabled() && !user.getPassword().startsWith("$2")
      ) user.setPassword(passwords.encode(user.getPassword()));
    }
    categories.seed(owner);
  }
}
