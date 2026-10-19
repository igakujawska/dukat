package com.example.demoapp.service;

import com.example.demoapp.repository.AccountRepository;
import com.example.demoapp.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("accountsCount", accountRepository.count());
        stats.put("transactionsCount", transactionRepository.count());
        return stats;
    }
}