package com.example.demoapp.service;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.model.Household;
import com.example.demoapp.repository.HouseholdRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HouseholdService {

  private final HouseholdRepository repository;
  private final CurrentUser current;

  public HouseholdService(HouseholdRepository repository, CurrentUser current) {
    this.repository = repository;
    this.current = current;
  }

  public List<HouseholdResponse> list() {
    return repository
      .findAllByOwnerIdOrderByIdAsc(current.id())
      .stream()
      .map(h -> new HouseholdResponse(h.getId(), h.getName()))
      .toList();
  }

  public HouseholdResponse create(NameRequest request) {
    var h = new Household(request.name().trim());
    h.setOwner(current.required());
    repository.save(h);
    return new HouseholdResponse(h.getId(), h.getName());
  }
}
