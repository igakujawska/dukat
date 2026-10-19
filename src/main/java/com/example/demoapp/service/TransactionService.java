package com.example.demoapp.service;

import com.example.demoapp.dto.transaction.CreateTransactionRequest;
import com.example.demoapp.dto.transaction.TransactionResponse;
import com.example.demoapp.model.Account;
import com.example.demoapp.model.Transaction;
import com.example.demoapp.model.TransactionType;
import com.example.demoapp.repository.AccountRepository;
import com.example.demoapp.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional //dodanie transakcji+aktualizacja salda konta w jednej spójnej operacji (ACID).
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono konta o ID: " + request.accountId()));

        // Przeliczenie salda konta w zależności od typu transakcji
        if (request.type() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(request.amount()));
        } else {
            account.setBalance(account.getBalance().subtract(request.amount()));
        }

        accountRepository.save(account);

        Transaction transaction = new Transaction(
                request.description(),
                request.amount(),
                request.transactionDate(),
                request.category(),
                request.type(),
                account
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getDescription(),
                t.getAmount(),
                t.getTransactionDate(),
                t.getCategory(),
                t.getType(),
                t.getAccount().getName()
        );
    }
}
