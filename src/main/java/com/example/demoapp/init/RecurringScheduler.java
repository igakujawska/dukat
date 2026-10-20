package com.example.demoapp.init;

import com.example.demoapp.repository.RecurringPaymentRepository;
import com.example.demoapp.service.*;
import java.time.LocalDate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true")
public class RecurringScheduler {

  private final RecurringPaymentRepository payments;
  private final RecurringProcessor processor;
  private final RecurringPaymentService service;

  public RecurringScheduler(
    RecurringPaymentRepository payments,
    RecurringProcessor processor,
    RecurringPaymentService service
  ) {
    this.payments = payments;
    this.processor = processor;
    this.service = service;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 60000)
  public void bookDue() {
    for (var p : payments.findAllByActiveTrueAndNextDueDateLessThanEqual(
      LocalDate.now()
    )) {
      try {
        processor.execute(p.getId());
      } catch (RuntimeException e) {
        service.recordFailure(p.getId(), e.getMessage());
      }
    }
  }
}
