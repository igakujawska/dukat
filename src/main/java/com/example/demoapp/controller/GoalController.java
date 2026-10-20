package com.example.demoapp.controller;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

  private final GoalService service;

  public GoalController(GoalService service) {
    this.service = service;
  }

  @GetMapping
  public List<GoalResponse> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GoalResponse create(@Valid @RequestBody GoalRequest r) {
    return service.create(r);
  }

  @PutMapping("/{id}")
  public GoalResponse update(
    @PathVariable Long id,
    @Valid @RequestBody GoalRequest r
  ) {
    return service.update(id, r);
  }

  @PostMapping("/{id}/contributions")
  @Operation(
    summary = "Zwiększ ewidencjonowany postęp celu; bez obciążania rachunku"
  )
  public GoalResponse contribute(
    @PathVariable Long id,
    @Valid @RequestBody ContributionRequest r
  ) {
    return service.contribute(id, r);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
