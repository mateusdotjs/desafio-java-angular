import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from '../../core/api-url';
import { Page } from '../../core/http/page';
import { Address } from './address.models';

@Injectable({ providedIn: 'root' })
export class AddressService {
  private readonly http = inject(HttpClient);

  list(userId: number): Observable<Page<Address>> {
    return this.http.get<Page<Address>>(`${API_URL}/api/users/${userId}/addresses`);
  }
}
