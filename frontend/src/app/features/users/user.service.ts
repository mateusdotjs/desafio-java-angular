import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from '../../core/api-url';
import { User, UserUpdateRequest } from '../../core/auth/auth.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  get(id: number): Observable<User> {
    return this.http.get<User>(`${API_URL}/api/users/${id}`);
  }

  update(id: number, request: UserUpdateRequest): Observable<User> {
    return this.http.put<User>(`${API_URL}/api/users/${id}`, request);
  }
}
