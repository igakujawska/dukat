package com.example.demoapp.controller;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.dto.transfer.CreateTransferRequest;
import com.example.demoapp.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

  private final TransferService service;

  public TransferController(TransferService service) {
    this.service = service;
  }

  @GetMapping
  public List<TransferResponse> list() {
    return service.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
    summary = "Przelew między własnymi aktywnymi kontami; dwa salda i historia w jednej transakcji"
  )
  public TransferResponse create(@Valid @RequestBody CreateTransferRequest r) {
    return service.executeTransfer(r);
  }
}
