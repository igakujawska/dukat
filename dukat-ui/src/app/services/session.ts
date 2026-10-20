import { Injectable } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';

// @Injectable udostępnia jedną instancję serwisu przez mechanizm DI Angulara.
// Przechowujemy dane Basic Auth tylko w pamięci: odświeżenie strony kończy sesję.
@Injectable({ providedIn: 'root' })
export class SessionService {
  private authorization = '';

  setCredentials(username: string, password: string): void {
    const bytes = new TextEncoder().encode(`${username}:${password}`);
    this.authorization = `Basic ${btoa(String.fromCharCode(...bytes))}`;
  }

  headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: this.authorization });
  }

  clear(): void {
    this.authorization = '';
  }
}
