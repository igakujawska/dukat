package com.example.demoapp.service;

import com.example.demoapp.model.RecurringPayment;
import com.example.demoapp.repository.RecurringPaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecurringPaymentService {

    private final RecurringPaymentRepository repository;

    public RecurringPaymentService(RecurringPaymentRepository repository) {
        this.repository = repository;
    }

    public List<RecurringPayment> getAllPayments() {
        return repository.findAll();
    }

    public RecurringPayment createPayment(RecurringPayment payment) {
        return repository.save(payment);
    }
}
