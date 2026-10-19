package com.example.demoapp.service;

import com.example.demoapp.dto.transfer.CreateTransferRequest;
import com.example.demoapp.model.Account;
import com.example.demoapp.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final AccountRepository accountRepository;

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void executeTransfer(CreateTransferRequest request) {
        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new IllegalArgumentException("Konto źródłowe i docelowe muszą być różne.");
        }

        Account source = accountRepository.findById(request.sourceAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono konta źródłowego ID: " + request.sourceAccountId()));

        Account target = accountRepository.findById(request.targetAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono konta docelowego ID: " + request.targetAccountId()));

        if (source.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Brak wystarczających środków na koncie źródłowym.");
        }

        // Odejmujemy kwotę z jednego konta i dodajemy na drugie w ramach jednej transakcji ACID
        source.setBalance(source.getBalance().subtract(request.amount()));
        target.setBalance(target.getBalance().add(request.amount()));

        accountRepository.save(source);
        accountRepository.save(target);
    }
}