import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';

import { AuthSession } from '../../core/auth/auth-session';
import { User } from '../../core/auth/auth.models';
import { Shell } from './shell';

const user: User = {
  id: 1,
  name: 'Ana',
  email: 'ana@example.com',
  role: 'USER',
  createdAt: '2026-01-01T00:00:00Z',
};

describe('Shell', () => {
  let fixture: ComponentFixture<Shell>;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Shell],
      providers: [provideRouter([]), provideNoopAnimations()],
    }).compileComponents();

    fixture = TestBed.createComponent(Shell);
  });

  afterEach(() => {
    TestBed.inject(AuthSession).clear();
  });

  it('mostra Usuários só para admin', () => {
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('Usuários');

    TestBed.inject(AuthSession).establish('token', user);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Minha conta');
    expect(fixture.nativeElement.textContent).not.toContain('Usuários');

    TestBed.inject(AuthSession).establish('token', { ...user, role: 'ADMIN' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Usuários');
  });
});
