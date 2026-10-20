import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: { id: number; username: string; email: string; role: string };
}
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', { username, password });
  }
  register(username: string, email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/register', { username, email, password });
  }
}
