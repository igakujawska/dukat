package com.example.demoapp.service;

import com.example.demoapp.model.Goal;
import com.example.demoapp.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    public Goal createGoal(Goal goal) {
        return goalRepository.save(goal);
    }
}