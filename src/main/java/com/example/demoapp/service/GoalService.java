package com.example.demoapp.service;

import com.example.demoapp.dto.finance.FinanceDtos.*;
import com.example.demoapp.model.Goal;
import com.example.demoapp.repository.GoalRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class GoalService {

  private final GoalRepository goals;
  private final CurrentUser current;

  public GoalService(GoalRepository goals, CurrentUser current) {
    this.goals = goals;
    this.current = current;
  }

  @Transactional(readOnly = true)
  public List<GoalResponse> list() {
    return goals
      .findAllByOwnerIdOrderByIdAsc(current.id())
      .stream()
      .map(GoalService::response)
      .toList();
  }

  public GoalResponse create(GoalRequest r) {
    var g = new Goal();
    g.setOwner(current.required());
    apply(g, r);
    return response(goals.save(g));
  }

  public GoalResponse update(Long id, GoalRequest r) {
    var g = owned(id);
    apply(g, r);
    return response(g);
  }

  public GoalResponse contribute(Long id, ContributionRequest r) {
    var g = goals
      .lockOwned(id, current.id())
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono celu")
      );
    g.setCurrentAmount(g.getCurrentAmount().add(r.amount()));
    return response(g);
  }

  public void delete(Long id) {
    goals.delete(owned(id));
  }

  private Goal owned(Long id) {
    return goals
      .findByIdAndOwnerId(id, current.id())
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono celu")
      );
  }

  private void apply(Goal g, GoalRequest r) {
    g.setName(r.name().trim());
    g.setTargetAmount(r.targetAmount());
    g.setCurrentAmount(r.currentAmount());
    g.setTargetDate(r.targetDate());
  }

  private static GoalResponse response(Goal g) {
    return new GoalResponse(
      g.getId(),
      g.getName(),
      g.getTargetAmount(),
      g.getCurrentAmount(),
      g.getTargetDate()
    );
  }
}
