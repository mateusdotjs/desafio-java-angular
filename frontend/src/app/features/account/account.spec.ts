import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { API_URL } from '../../core/api-url';
import { AuthSession } from '../../core/auth/auth-session';
import { User } from '../../core/auth/auth.models';
import { NotificationService } from '../../core/services/notification.service';
import { Address } from '../addresses/address.models';
import { Account } from './account';

const user: User = {
  id: 7,
  name: 'Ana',
  email: 'ana@example.com',
  role: 'USER',
  createdAt: '2026-01-15T13:00:00Z',
};

const address: Address = {
  id: 3,
  street: 'Praça da Sé',
  number: '100',
  complement: 'apto 1',
  neighborhood: 'Sé',
  city: 'São Paulo',
  state: 'SP',
  zipCode: '01310100',
  userId: 7,
};

describe('Account', () => {
  let fixture: ComponentFixture<Account>;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Account],
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
    fixture = TestBed.createComponent(Account);
  });

  afterEach(() => {
    http.verify();
    TestBed.inject(AuthSession).clear();
  });

  it('mostra o perfil e o endereço', () => {
    const userRequest = http.expectOne(`${API_URL}/api/users/7`);
    const addressRequest = http.expectOne((req) => req.url === `${API_URL}/api/users/7/addresses`);
    expect(userRequest.request.method).toBe('GET');
    expect(addressRequest.request.params.get('page')).toBe('0');
    expect(addressRequest.request.params.get('size')).toBe('10');
    expect(addressRequest.request.params.get('sort')).toBe('street,asc');

    userRequest.flush(user);
    addressRequest.flush(pageOf([address]));
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Ana');
    expect(text).toContain('ana@example.com');
    expect(text).toContain('Usuário');
    expect(text).toContain('Praça da Sé, 100');
    expect(text).toContain('apto 1');
    expect(text).toContain('Sé — São Paulo/SP');
    expect(text).toContain('CEP 01310-100');
    expect(text).toContain('Novo endereço');
  });

  it('mostra a lista vazia', () => {
    http.expectOne(`${API_URL}/api/users/7`).flush(user);
    http.expectOne((req) => req.url === `${API_URL}/api/users/7/addresses`).flush(pageOf([]));
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Ana');
    expect(text).toContain('Nenhum endereço cadastrado');
    expect(text).toContain('Novo endereço');
  });
});

function pageOf(content: Address[]) {
  return {
    content,
    empty: content.length === 0,
    totalElements: content.length,
    totalPages: content.length === 0 ? 0 : 1,
    number: 0,
    size: 10,
  };
}

function activatedRoute() {
  const params = convertToParamMap({});
  return {
    snapshot: { paramMap: params, data: { title: 'Minha conta' } },
    paramMap: of(params),
  };
}
