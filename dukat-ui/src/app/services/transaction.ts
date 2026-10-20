import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionService } from './session';

export type TransactionType = 'INCOME' | 'EXPENSE';

// Interfejs opisuje JSON odpowiedzi API. Nie tworzy tabeli ani obiektu w bazie.
export interface FinanceTransaction {
  id: number;
  description: string;
  amount: number;
  transactionDate: string;
  category: string;
  type: TransactionType;
  accountName: string;
}

export interface CreateTransactionRequest {
  accountId: number;
  amount: number;
  type: TransactionType;
  // Backend przyjmuje pole date, a w odpowiedzi zwraca transactionDate.
  date: string;
  category: string;
  description: string;
}

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);

  getTransactions(): Observable<FinanceTransaction[]> {
    return this.http.get<FinanceTransaction[]>('/api/transactions', {
      headers: this.session.headers(),
    });
  }

  createTransaction(request: CreateTransactionRequest): Observable<FinanceTransaction> {
    return this.http.post<FinanceTransaction>('/api/transactions', request, {
      headers: this.session.headers(),
    });
  }
}
