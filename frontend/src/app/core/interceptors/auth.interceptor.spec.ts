import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { API_URL } from '../api-url';
import { User } from '../auth/auth.models';
import { AuthSession } from '../auth/auth-session';
import { NotificationService } from '../services/notification.service';
import { authInterceptor } from './auth.interceptor';

const user: User = {
  id: 1,
  name: 'Ana',
  email: 'ana@example.com',
  role: 'USER',
  createdAt: '2026-01-01T00:00:00Z',
};

describe('authInterceptor', () => {
  let http: HttpClient;
  let session: AuthSession;
  let requests: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: NotificationService, useValue: { success: () => undefined, error: () => undefined } },
      ],
    });
    http = TestBed.inject(HttpClient);
    session = TestBed.inject(AuthSession);
    requests = TestBed.inject(HttpTestingController);
  });

  afterEach(() => requests.verify());

  it('envia o Bearer', () => {
    session.establish('abc', user);

    http.get('/api/users').subscribe();

    const request = requests.expectOne('/api/users');
    expect(request.request.headers.get('Authorization')).toBe('Bearer abc');
    request.flush([]);
  });

  it('limpa a sessão e volta para /login quando uma rota protegida responde 401', () => {
    session.establish('abc', user);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    http.get('/api/users').subscribe({ error: () => undefined });
    requests
      .expectOne('/api/users')
      .flush(
        { message: 'Authentication required', fieldErrors: [] },
        { status: 401, statusText: 'Unauthorized' },
      );

    expect(session.accessToken()).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });

  it('mantém a sessão quando o login responde 401', () => {
    session.establish('abc', user);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    http.post(`${API_URL}/api/auth/login`, {}).subscribe({ error: () => undefined });
    requests
      .expectOne(`${API_URL}/api/auth/login`)
      .flush(
        { message: 'Invalid email or password', fieldErrors: [] },
        { status: 401, statusText: 'Unauthorized' },
      );

    expect(session.accessToken()).toBe('abc');
    expect(navigate).not.toHaveBeenCalled();
  });
});
