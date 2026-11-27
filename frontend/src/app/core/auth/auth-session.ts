import { Injectable, Signal, WritableSignal, computed, signal } from '@angular/core';

import { User } from './auth.models';

const STORAGE_KEY = 'auth.session';

interface StoredSession {
  accessToken: string;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class AuthSession {
  private readonly accessTokenState: WritableSignal<string | null>;
  private readonly userState: WritableSignal<User | null>;

  readonly accessToken: Signal<string | null>;
  readonly user: Signal<User | null>;
  readonly isAuthenticated: Signal<boolean>;

  constructor() {
    const restored = readStoredSession();
    this.accessTokenState = signal<string | null>(restored?.accessToken ?? null);
    this.userState = signal<User | null>(restored?.user ?? null);
    this.accessToken = this.accessTokenState.asReadonly();
    this.user = this.userState.asReadonly();
    this.isAuthenticated = computed(
      () => this.accessTokenState() !== null && this.userState() !== null,
    );
  }

  establish(accessToken: string, user: User): void {
    this.accessTokenState.set(accessToken);
    this.userState.set(user);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ accessToken, user } satisfies StoredSession));
    } catch {
      // A sessão continua em memória quando o navegador bloqueia o armazenamento.
    }
  }

  clear(): void {
    this.accessTokenState.set(null);
    this.userState.set(null);
    localStorage.removeItem(STORAGE_KEY);
  }
}

function readStoredSession(): StoredSession | null {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (raw === null) return null;

  try {
    const parsed: unknown = JSON.parse(raw);
    if (isStoredSession(parsed)) return parsed;
  } catch {
    // JSON inválido é descartado abaixo.
  }

  localStorage.removeItem(STORAGE_KEY);
  return null;
}

function isStoredSession(value: unknown): value is StoredSession {
  if (typeof value !== 'object' || value === null) return false;
  const session = value as Partial<StoredSession>;
  return typeof session.accessToken === 'string' && session.accessToken.length > 0 && isUser(session.user);
}

function isUser(value: unknown): value is User {
  if (typeof value !== 'object' || value === null) return false;
  const user = value as Partial<User>;
  return (
    typeof user.id === 'number' &&
    Number.isInteger(user.id) &&
    user.id > 0 &&
    typeof user.name === 'string' &&
    user.name.length > 0 &&
    typeof user.email === 'string' &&
    user.email.length > 0 &&
    (user.role === 'ADMIN' || user.role === 'USER') &&
    typeof user.createdAt === 'string' &&
    user.createdAt.length > 0
  );
}
