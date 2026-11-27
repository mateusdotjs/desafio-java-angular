import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { API_URL } from '../api-url';
import { AuthResponse, LoginRequest, RegisterRequest } from './auth.models';
import { AuthSession } from './auth-session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(AuthSession);

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${API_URL}/api/auth/login`, request)
      .pipe(tap((response) => this.remember(response)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${API_URL}/api/auth/register`, request)
      .pipe(tap((response) => this.remember(response)));
  }

  logout(): void {
    this.session.clear();
  }

  private remember(response: AuthResponse): void {
    this.session.establish(response.accessToken, response.user);
  }
}
