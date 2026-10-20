package com.example.demoapp.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public final class AuthDtos {

  private AuthDtos() {}

  @Schema(description = "Logowanie istniejącego profilu.")
  public record LoginRequest(
    @NotBlank
    @Schema(
      description = "Login: 3–40 liter ASCII, cyfr lub znaków . _ - przy rejestracji."
    )
    String username,
    @NotBlank
    @Schema(
      description = "Hasło: przy rejestracji 10–72 znaki i najwyżej 72 bajty UTF-8.",
      format = "password"
    )
    String password
  ) {}

  @Schema(description = "Nowy profil; hasło jest hashowane BCrypt.")
  public record RegisterRequest(
    @NotBlank
    @Pattern(regexp = "[a-zA-Z0-9._-]{3,40}")
    @Schema(
      description = "Login: 3–40 liter ASCII, cyfr lub znaków . _ - przy rejestracji."
    )
    String username,
    @NotBlank
    @Email
    @Size(max = 120)
    @Schema(description = "Adres e-mail użytkownika.")
    String email,
    @NotBlank
    @Size(min = 10, max = 72)
    @Schema(
      description = "Hasło: przy rejestracji 10–72 znaki i najwyżej 72 bajty UTF-8.",
      format = "password"
    )
    String password
  ) {}

  @Schema(description = "Bezpieczny profil, bez hasła.")
  public record UserResponse(
    @Schema(description = "Identyfikator nadany przez bazę.") Long id,
    @Schema(
      description = "Login: 3–40 liter ASCII, cyfr lub znaków . _ - przy rejestracji."
    )
    String username,
    @Schema(description = "Adres e-mail użytkownika.") String email,
    @Schema(description = "Rola przydzielana przez serwer.") String role
  ) {}

  @Schema(description = "Token sesji i publiczny profil.")
  public record AuthResponse(
    @Schema(description = "Podpisany token JWT. Nie zapisuj go w repozytorium.")
    String accessToken,
    @Schema(description = "Bearer") String tokenType,
    @Schema(description = "Ważność tokenu w sekundach.") long expiresIn,
    UserResponse user
  ) {}
}
