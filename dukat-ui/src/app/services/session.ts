import { Injectable } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
// Token żyje tylko w pamięci. Odświeżenie karty lub upływ godziny wymaga logowania.
@Injectable({ providedIn: 'root' })
export class SessionService {
  private token = '';
  setToken(token: string): void {
    this.token = token;
  }
  headers(): HttpHeaders {
    return this.token
      ? new HttpHeaders({ Authorization: 'Bearer ' + this.token })
      : new HttpHeaders();
  }
  clear(): void {
    this.token = '';
  }
}
