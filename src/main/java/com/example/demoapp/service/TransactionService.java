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

@Service // Przypadek użycia: zapis operacji i przeliczenie salda, niezależnie od wyglądu UI.
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

    // @Transactional obejmuje zapis historii i zmianę salda jedną transakcją bazy.
    // Błąd RuntimeException wycofa oba zapisy. To nie zastępuje ochrony przed współbieżnymi zmianami.
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Kwota transakcji musi być dodatnia");
        }
        // Konwersja tekstu kontraktu API na zamknięty zestaw wartości enuma domeny.
        TransactionType type = TransactionType.valueOf(request.getType());
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono konta o ID: " + request.getAccountId()));

        // Przeliczenie salda konta w zależności od typu transakcji
        if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        }

        accountRepository.save(account);

        Transaction transaction = new Transaction(
                request.getDescription(),
                request.getAmount(),
                request.getDate(),
                request.getCategory(),
                type,
                account
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    private TransactionResponse mapToResponse(Transaction t) {
        // DTO odpowiedzi ujawnia potrzebne pola, bez serializacji całej relacji Account.
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
