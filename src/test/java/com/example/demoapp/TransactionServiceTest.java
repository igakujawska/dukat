package com.example.demoapp;

import com.example.demoapp.dto.transaction.CreateTransactionRequest;
import com.example.demoapp.model.Account;
import com.example.demoapp.repository.AccountRepository;
import com.example.demoapp.repository.TransactionRepository;
import com.example.demoapp.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldIncreaseBalanceOnIncome() {
        Account account = new Account("Główne", "PLN", new BigDecimal("1000.00"));
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("500.00"));
        request.setType("INCOME");
        request.setDate(LocalDate.now());

        transactionService.createTransaction(request);

        assertEquals(new BigDecimal("1500.00"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void shouldDecreaseBalanceOnExpense() {
        Account account = new Account("Główne", "PLN", new BigDecimal("1000.00"));
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("200.00"));
        request.setType("EXPENSE");
        request.setDate(LocalDate.now());

        transactionService.createTransaction(request);

        assertEquals(new BigDecimal("800.00"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAccountId(99L);
        request.setAmount(new BigDecimal("100.00"));
        request.setType("EXPENSE");
        request.setDate(LocalDate.now());

        assertThrows(IllegalArgumentException.class, () -> transactionService.createTransaction(request));
    }
}