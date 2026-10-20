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
    // Oznaczamy żądanie aplikacji webowej. Także domyślny Spring Security
    // rozpoznaje XMLHttpRequest i nie wyświetla natywnego monitu HTTP Basic.
    return new HttpHeaders({
      Authorization: this.authorization,
      'X-Requested-With': 'XMLHttpRequest',
    });
  }

  clear(): void {
    this.authorization = '';
  }
}
