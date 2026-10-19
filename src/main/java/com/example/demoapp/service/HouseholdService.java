package com.example.demoapp.service;

import com.example.demoapp.model.Household;
import com.example.demoapp.repository.HouseholdRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HouseholdService {

    private final HouseholdRepository repository;

    public HouseholdService(HouseholdRepository repository) {
        this.repository = repository;
    }

    public List<Household> getAllHouseholds() {
        return repository.findAll();
    }

    public Household createHousehold(Household household) {
        return repository.save(household);
    }
}