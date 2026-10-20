package com.example.demoapp.controller;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.service.HouseholdService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/households")
public class HouseholdController {

  private final HouseholdService service;

  public HouseholdController(HouseholdService service) {
    this.service = service;
  }

  @GetMapping
  public List<HouseholdResponse> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public HouseholdResponse create(@Valid @RequestBody NameRequest r) {
    return service.create(r);
  }
}
