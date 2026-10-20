package com.example.demoapp.controller;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.service.RecurringPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recurring-payments")
public class RecurringPaymentController {

  private final RecurringPaymentService service;

  public RecurringPaymentController(RecurringPaymentService service) {
    this.service = service;
  }

  @GetMapping
  public List<RecurringResponse> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public RecurringResponse create(@Valid @RequestBody RecurringRequest r) {
    return service.create(r);
  }

  @PutMapping("/{id}")
  public RecurringResponse update(
    @PathVariable Long id,
    @Valid @RequestBody RecurringRequest r
  ) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void pause(@PathVariable Long id) {
    service.pause(id);
  }

  @PostMapping("/{id}/execute")
  @Operation(
    summary = "Zaksięguj zaległe terminy, najwyżej 12 na wywołanie; termin nie zostanie zapisany dwukrotnie"
  )
  public Map<String, Integer> execute(@PathVariable Long id) {
    return Map.of("posted", service.execute(id));
  }
}
