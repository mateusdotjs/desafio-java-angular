import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from '../../core/api-url';
import { Page } from '../../core/http/page';
import { Address, AddressRequest } from './address.models';

@Injectable({ providedIn: 'root' })
export class AddressService {
  private readonly http = inject(HttpClient);

  list(userId: number, page: number, size: number): Observable<Page<Address>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'street,asc');
    return this.http.get<Page<Address>>(`${API_URL}/api/users/${userId}/addresses`, { params });
  }

  get(userId: number, addressId: number): Observable<Address> {
    return this.http.get<Address>(`${API_URL}/api/users/${userId}/addresses/${addressId}`);
  }

  create(userId: number, request: AddressRequest): Observable<Address> {
    return this.http.post<Address>(`${API_URL}/api/users/${userId}/addresses`, request);
  }

  update(userId: number, addressId: number, request: AddressRequest): Observable<Address> {
    return this.http.put<Address>(`${API_URL}/api/users/${userId}/addresses/${addressId}`, request);
  }

  delete(userId: number, addressId: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/api/users/${userId}/addresses/${addressId}`);
  }
}
