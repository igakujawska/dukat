package com.example.demoapp.service;

import com.example.demoapp.model.User;
import com.example.demoapp.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUser {

  private final UserRepository users;

  public CurrentUser(UserRepository users) {
    this.users = users;
  }

  public User required() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (
      auth == null || !auth.isAuthenticated()
    ) throw new BadCredentialsException("Zaloguj się ponownie");
    return users
      .findByUsernameIgnoreCase(auth.getName())
      .filter(User::isEnabled)
      .orElseThrow(() -> new BadCredentialsException("Zaloguj się ponownie"));
  }

  public Long id() {
    return required().getId();
  }
}
