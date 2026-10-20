package com.example.demoapp.controller;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

  private final BudgetService service;

  public BudgetController(BudgetService service) {
    this.service = service;
  }

  @GetMapping
  public List<BudgetResponse> list(@RequestParam String month) {
    return service.list(month);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BudgetResponse create(@Valid @RequestBody BudgetRequest r) {
    return service.create(r);
  }

  @PutMapping("/{id}")
  public BudgetResponse update(
    @PathVariable Long id,
    @Valid @RequestBody BudgetRequest r
  ) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
