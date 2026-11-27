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
import { UserForm } from './user-form';

const user: User = {
  id: 7,
  name: 'Ana',
  email: 'ana@example.com',
  role: 'USER',
  createdAt: '2026-01-15T13:00:00Z',
};

describe('UserForm', () => {
  let fixture: ComponentFixture<UserForm>;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [UserForm],
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
    fixture = TestBed.createComponent(UserForm);
    http.expectOne(`${API_URL}/api/users/7`).flush(user);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
    TestBed.inject(AuthSession).clear();
  });

  it('envia senha nula quando o campo fica em branco', () => {
    setInput(fixture, 'name', 'Ana Souza');
    submit(fixture);

    const request = http.expectOne(`${API_URL}/api/users/7`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      name: 'Ana Souza',
      email: 'ana@example.com',
      password: null,
    });
    request.flush({ ...user, name: 'Ana Souza' });
    fixture.detectChanges();

    expect(TestBed.inject(AuthSession).user()?.name).toBe('Ana Souza');
    expect(TestBed.inject(AuthSession).accessToken()).toBe('token');
  });

  it('exige 8 caracteres quando a senha é preenchida', () => {
    setInput(fixture, 'password', '1234567');
    submit(fixture);

    http.expectNone(`${API_URL}/api/users/7`);
    expect(fixture.nativeElement.textContent).toContain('Mínimo de 8 caracteres');
  });
});

function setInput(fixture: ComponentFixture<UserForm>, name: string, value: string): void {
  const input: HTMLInputElement | null = fixture.nativeElement.querySelector(
    `input[formControlName="${name}"]`,
  );
  if (!input) throw new Error(`Campo ${name} não encontrado`);
  input.value = value;
  input.dispatchEvent(new Event('input'));
  input.dispatchEvent(new Event('blur'));
  fixture.detectChanges();
}

function submit(fixture: ComponentFixture<UserForm>): void {
  const button: HTMLButtonElement | null = fixture.nativeElement.querySelector('button[type="submit"]');
  if (!button) throw new Error('Botão de envio não encontrado');
  button.click();
  fixture.detectChanges();
}

function activatedRoute() {
  const params = convertToParamMap({});
  return {
    snapshot: {
      paramMap: params,
      data: { title: 'Editar meus dados' },
      routeConfig: { path: 'me/edit' },
    },
    paramMap: of(params),
  };
}
