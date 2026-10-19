package com.example.demoapp.repository;

import com.example.demoapp.model.RecurringPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, Long> {
}