package com.example.demoapp.controller;

import com.example.demoapp.model.Household;
import com.example.demoapp.service.HouseholdService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/households")
@CrossOrigin(origins = "*")
public class HouseholdController {

    private final HouseholdService service;

    public HouseholdController(HouseholdService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Household>> getAllHouseholds() {
        return ResponseEntity.ok(service.getAllHouseholds());
    }

    @PostMapping
    public ResponseEntity<Household> createHousehold(@RequestBody Household household) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createHousehold(household));
    }
}