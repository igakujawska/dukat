package com.example.demoapp.controller;

import com.example.demoapp.model.RecurringPayment;
import com.example.demoapp.service.RecurringPaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-payments")
@CrossOrigin(origins = "*")
public class RecurringPaymentController {

    private final RecurringPaymentService service;

    public RecurringPaymentController(RecurringPaymentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RecurringPayment>> getAllPayments() {
        return ResponseEntity.ok(service.getAllPayments());
    }

    @PostMapping
    public ResponseEntity<RecurringPayment> createPayment(@RequestBody RecurringPayment payment) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayment(payment));
    }
}