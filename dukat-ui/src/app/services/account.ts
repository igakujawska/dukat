import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SessionService } from './session';

export interface Account {
  id: number;
  name: string;
  accountType: string;
  balance: number;
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

  getAccounts(username: string, password: string): Observable<Account[]> {
    this.session.setCredentials(username, password);
    return this.refreshAccounts();
  }

  refreshAccounts(): Observable<Account[]> {
    // Względne /api przechodzi przez proxy Angulara do Spring Boot na :8082.
    return this.http.get<Account[]>('/api/accounts', { headers: this.session.headers() });
  }

  createAccount(request: CreateAccountRequest): Observable<Account> {
    return this.http.post<Account>('/api/accounts', request, { headers: this.session.headers() });
  }
}
