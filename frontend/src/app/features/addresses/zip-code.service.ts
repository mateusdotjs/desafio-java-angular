import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from '../../core/api-url';
import { zipDigits } from '../../core/forms/validation';
import { ZipCodeLookup } from './address.models';

@Injectable({ providedIn: 'root' })
export class ZipCodeService {
  private readonly http = inject(HttpClient);

  lookup(zipCode: string): Observable<ZipCodeLookup> {
    return this.http.get<ZipCodeLookup>(`${API_URL}/api/zipcodes/${zipDigits(zipCode)}`);
  }
}
