import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';

import { User } from '../auth/auth.models';
import { AuthSession } from '../auth/auth-session';
import { adminGuard } from './auth.guard';

const user: User = {
  id: 1,
  name: 'Ana',
  email: 'ana@example.com',
  role: 'USER',
  createdAt: '2026-01-01T00:00:00Z',
};

describe('adminGuard', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  afterEach(() => {
    TestBed.inject(AuthSession).clear();
  });

  it('libera o admin', () => {
    TestBed.inject(AuthSession).establish('token', { ...user, role: 'ADMIN' });

    expect(runGuard()).toBe(true);
  });

  it('envia o usuário comum para a própria conta', () => {
    TestBed.inject(AuthSession).establish('token', user);

    expect(redirectPath()).toBe('/me');
  });

  it('envia quem não entrou para o login', () => {
    expect(redirectPath()).toBe('/login');
  });
});

function runGuard() {
  return TestBed.runInInjectionContext(() =>
    adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
  );
}

function redirectPath(): string {
  const result = runGuard();
  if (!(result instanceof UrlTree)) throw new Error('esperado redirecionamento');
  return TestBed.inject(Router).serializeUrl(result);
}
