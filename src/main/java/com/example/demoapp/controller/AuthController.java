package com.example.demoapp.controller;

import com.example.demoapp.dto.auth.AuthDtos.*;
import com.example.demoapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.security.SecurityRequirements
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @PostMapping("/register")
  @Operation(summary = "Rejestracja użytkownika i wydanie tokenu")
  public ResponseEntity<AuthResponse> register(
    @Valid @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      auth.register(request)
    );
  }

  @PostMapping("/login")
  @Operation(summary = "Logowanie; token Bearer jest ważny przez godzinę")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return auth.login(request);
  }
}
