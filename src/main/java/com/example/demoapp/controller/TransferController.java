package com.example.demoapp.controller;

import com.example.demoapp.dto.transfer.CreateTransferRequest;
import com.example.demoapp.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@CrossOrigin(origins = "*")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<String> transfer(@RequestBody CreateTransferRequest request) {
        transferService.executeTransfer(request);
        return ResponseEntity.ok("Przelew został wykonany pomyślnie.");
    }
}
