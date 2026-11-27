import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from '../../core/api-url';
import { User, UserCreateRequest, UserUpdateRequest } from '../../core/auth/auth.models';
import { Page } from '../../core/http/page';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  list(page: number, size: number, sort: string): Observable<Page<User>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Page<User>>(`${API_URL}/api/users`, { params });
  }

  get(id: number): Observable<User> {
    return this.http.get<User>(`${API_URL}/api/users/${id}`);
  }

  create(request: UserCreateRequest): Observable<User> {
    return this.http.post<User>(`${API_URL}/api/users`, request);
  }

  update(id: number, request: UserUpdateRequest): Observable<User> {
    return this.http.put<User>(`${API_URL}/api/users/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/api/users/${id}`);
  }
}
