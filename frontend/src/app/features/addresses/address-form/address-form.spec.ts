import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { API_URL } from '../../../core/api-url';
import { AuthSession } from '../../../core/auth/auth-session';
import { User } from '../../../core/auth/auth.models';
import { NotificationService } from '../../../core/services/notification.service';
import { AddressForm } from './address-form';

const user: User = {
  id: 7,
  name: 'Ana',
  email: 'ana@example.com',
  role: 'USER',
  createdAt: '2026-01-15T13:00:00Z',
};

describe('AddressForm', () => {
  let fixture: ComponentFixture<AddressForm>;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [AddressForm],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideNoopAnimations(),
        { provide: ActivatedRoute, useValue: activatedRoute() },
        { provide: NotificationService, useValue: { success: () => undefined, error: () => undefined } },
      ],
    }).compileComponents();

    TestBed.inject(AuthSession).establish('token', user);
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(AddressForm);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
    TestBed.inject(AuthSession).clear();
  });

  it('consulta o CEP no blur e mostra o logradouro', () => {
    setInput(fixture, 'zipCode', '01310-100');
    blur(fixture, 'zipCode');

    const request = http.expectOne(`${API_URL}/api/zipcodes/01310100`);
    expect(request.request.method).toBe('GET');
    request.flush({
      zipCode: '01310100',
      street: 'Praça da Sé',
      neighborhood: 'Sé',
      city: 'São Paulo',
      state: 'SP',
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Praça da Sé');
    expect(text).toContain('Sé');
    expect(text).toContain('São Paulo');
    expect(text).toContain('SP');
  });

  it('não consulta CEP curto', () => {
    setInput(fixture, 'zipCode', '0131');
    blur(fixture, 'zipCode');
    http.expectNone((req) => req.url.includes('/api/zipcodes/'));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('CEP deve ter 8 dígitos');
  });
});

function setInput(fixture: ComponentFixture<AddressForm>, name: string, value: string): void {
  const input = field(fixture, name);
  input.value = value;
  input.dispatchEvent(new Event('input'));
  fixture.detectChanges();
}

function blur(fixture: ComponentFixture<AddressForm>, name: string): void {
  field(fixture, name).dispatchEvent(new Event('blur'));
}

function field(fixture: ComponentFixture<AddressForm>, name: string): HTMLInputElement {
  const input: HTMLInputElement | null = fixture.nativeElement.querySelector(
    `input[formControlName="${name}"]`,
  );
  if (!input) throw new Error(`Campo ${name} não encontrado`);
  return input;
}

function activatedRoute() {
  const params = convertToParamMap({});
  return {
    snapshot: { paramMap: params, data: { title: 'Novo endereço' } },
    paramMap: of(params),
  };
}
