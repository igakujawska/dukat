import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { SessionService } from './session';
export type PlanningView = 'budgets' | 'goals' | 'recurring-payments' | 'transfers' | 'categories';
export interface Category {
  id: number;
  name: string;
  active: boolean;
}
export interface Budget {
  id: number;
  categoryId: number;
  category: string;
  month: string;
  monthlyLimit: number;
  spent: number;
  remaining: number;
}
export interface Goal {
  id: number;
  name: string;
  targetAmount: number;
  currentAmount: number;
  targetDate: string | null;
}
export interface Recurring {
  id: number;
  title: string;
  amount: number;
  frequency: string;
  nextDueDate: string;
  accountId: number;
  accountName: string;
  categoryId: number;
  category: string;
  type: string;
  active: boolean;
  lastError: string | null;
}
export interface Transfer {
  id: number;
  sourceAccountId: number;
  sourceAccountName: string;
  targetAccountId: number;
  targetAccountName: string;
  amount: number;
  date: string;
  description: string;
}
@Injectable({ providedIn: 'root' })
export class PlanningService {
  private http = inject(HttpClient);
  private session = inject(SessionService);
  list<T>(view: PlanningView, month = '') {
    return this.http.get<T[]>('/api/' + view, {
      headers: this.session.headers(),
      params:
        view === 'budgets' ? { month } : view === 'categories' ? { includeInactive: 'true' } : {},
    });
  }
  save<T>(view: PlanningView, body: unknown, id: number | null = null) {
    const options = { headers: this.session.headers() };
    return id === null
      ? this.http.post<T>('/api/' + view, body, options)
      : this.http.put<T>('/api/' + view + '/' + id, body, options);
  }
  remove(view: PlanningView, id: number) {
    return this.http.delete<void>('/api/' + view + '/' + id, { headers: this.session.headers() });
  }
  contribute(id: number, amount: number) {
    return this.http.post<Goal>(
      '/api/goals/' + id + '/contributions',
      { amount },
      { headers: this.session.headers() },
    );
  }
  execute(id: number) {
    return this.http.post<{ posted: number }>(
      '/api/recurring-payments/' + id + '/execute',
      {},
      { headers: this.session.headers() },
    );
  }
}
