package com.example.demoapp.service;

import com.example.demoapp.dto.auth.AuthDtos.*;
import com.example.demoapp.model.*;
import com.example.demoapp.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final JwtEncoder encoder;
  private final CategoryService categories;
  private final long ttl;

  public AuthService(
    UserRepository users,
    PasswordEncoder passwords,
    JwtEncoder encoder,
    CategoryService categories,
    @Value("${app.jwt.ttl-seconds:3600}") long ttl
  ) {
    this.users = users;
    this.passwords = passwords;
    this.encoder = encoder;
    this.categories = categories;
    this.ttl = ttl;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String username = request.username().trim(),
      email = request.email().trim().toLowerCase();
    if (
      username.startsWith("__") ||
      users.findByUsernameIgnoreCase(username).isPresent() ||
      users.findByEmailIgnoreCase(email).isPresent()
    ) throw new ResponseStatusException(
      HttpStatus.CONFLICT,
      "Login lub e-mail jest już zajęty"
    );
    if (
      request.password().getBytes(StandardCharsets.UTF_8).length > 72
    ) throw new IllegalArgumentException(
      "Hasło może mieć maksymalnie 72 bajty UTF-8"
    );
    var user = users.save(
      new User(username, passwords.encode(request.password()), email, Role.USER)
    );
    categories.seed(user);
    return token(user);
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    var user = users
      .findByUsernameIgnoreCase(request.username().trim())
      .filter(User::isEnabled)
      .orElseThrow(() ->
        new BadCredentialsException("Nieprawidłowy login lub hasło")
      );
    if (
      request.password().getBytes(StandardCharsets.UTF_8).length > 72 ||
      !passwords.matches(request.password(), user.getPassword())
    ) throw new BadCredentialsException("Nieprawidłowy login lub hasło");
    return token(user);
  }

  private AuthResponse token(User user) {
    var now = Instant.now();
    var claims = JwtClaimsSet.builder()
      .issuer("dukat")
      .subject(user.getUsername())
      .issuedAt(now)
      .expiresAt(now.plusSeconds(ttl))
      .claim("uid", user.getId())
      .claim("role", user.getRole().name())
      .build();
    String jwt = encoder
      .encode(
        JwtEncoderParameters.from(
          JwsHeader.with(MacAlgorithm.HS256).build(),
          claims
        )
      )
      .getTokenValue();
    return new AuthResponse(jwt, "Bearer", ttl, profile(user));
  }

  public static UserResponse profile(User u) {
    return new UserResponse(
      u.getId(),
      u.getUsername(),
      u.getEmail(),
      u.getRole().name()
    );
  }
}
