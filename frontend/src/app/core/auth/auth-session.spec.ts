import { TestBed } from '@angular/core/testing';

import { User } from './auth.models';
import { AuthSession } from './auth-session';

const user: User = {
  id: 1,
  name: 'Ana',
  email: 'ana@example.com',
  role: 'USER',
  createdAt: '2026-01-01T00:00:00Z',
};

describe('AuthSession', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
  });

  it('grava o token e o usuário e limpa os dois', () => {
    TestBed.configureTestingModule({});
    const session = TestBed.inject(AuthSession);

    session.establish('token', user);

    const stored = localStorage.getItem('auth.session');
    expect(session.accessToken()).toBe('token');
    expect(session.user()).toEqual(user);
    expect(stored).toBeTypeOf('string');
    if (typeof stored !== 'string') return;
    expect(JSON.parse(stored)).toEqual({ accessToken: 'token', user });

    session.clear();

    expect(session.accessToken()).toBeNull();
    expect(session.user()).toBeNull();
    expect(session.isAuthenticated()).toBe(false);
    expect(localStorage.getItem('auth.session')).toBeNull();
  });

  it('descarta JSON inválido', () => {
    localStorage.setItem('auth.session', '{');
    TestBed.configureTestingModule({});
    const session = TestBed.inject(AuthSession);

    expect(session.accessToken()).toBeNull();
    expect(session.user()).toBeNull();
    expect(localStorage.getItem('auth.session')).toBeNull();
  });
});
