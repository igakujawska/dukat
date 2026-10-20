package com.example.demoapp.controller;

import com.example.demoapp.dto.auth.AuthDtos.UserResponse;
import com.example.demoapp.service.*;
import org.springframework.web.bind.annotation.*;

// Zwracamy DTO, nigdy encję z hasłem (także haszowane hasło nie powinno opuszczać serwera).
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final CurrentUser current;

  public UserController(CurrentUser current) {
    this.current = current;
  }

  @GetMapping("/me")
  public UserResponse me() {
    return AuthService.profile(current.required());
  }
}
