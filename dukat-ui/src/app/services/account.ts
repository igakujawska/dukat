import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionService } from './session';

export interface Account {
  id: number;
  name: string;
  accountType: string;
  balance: number;
  archived?: boolean;
  version?: number;
}

export interface CreateAccountRequest {
  name: string;
  accountType: string;
  balance: number;
}

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  // inject() pobiera gotowe zależności; komponent nie tworzy sam klienta HTTP.
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);

  refreshAccounts(includeArchived = false): Observable<Account[]> {
    // Względne /api przechodzi przez proxy Angulara do Spring Boot na :8082.
    return this.http.get<Account[]>('/api/accounts', {
      params: includeArchived ? { includeArchived: 'true' } : {},
      headers: this.session.headers(),
    });
  }

  createAccount(request: CreateAccountRequest): Observable<Account> {
    return this.http.post<Account>('/api/accounts', request, { headers: this.session.headers() });
  }
  update(id: number, request: { name: string; accountType: string }): Observable<Account> {
    return this.http.put<Account>('/api/accounts/' + id, request, {
      headers: this.session.headers(),
    });
  }
  archive(id: number): Observable<void> {
    return this.http.delete<void>('/api/accounts/' + id, { headers: this.session.headers() });
  }
  restore(id: number): Observable<Account> {
    return this.http.post<Account>(
      '/api/accounts/' + id + '/restore',
      {},
      { headers: this.session.headers() },
    );
  }
}
