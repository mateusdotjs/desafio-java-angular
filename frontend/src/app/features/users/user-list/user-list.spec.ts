import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';

import { API_URL } from '../../../core/api-url';
import { AuthSession } from '../../../core/auth/auth-session';
import { User } from '../../../core/auth/auth.models';
import { NotificationService } from '../../../core/services/notification.service';
import { UserList } from './user-list';

const ana: User = {
  id: 7,
  name: 'Ana',
  email: 'ana@example.com',
  role: 'USER',
  createdAt: '2026-01-15T13:00:00Z',
};

describe('UserList', () => {
  let fixture: ComponentFixture<UserList>;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [UserList],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { snapshot: { data: { title: 'Usuários' } } } },
        { provide: NotificationService, useValue: { success: () => undefined, error: () => undefined } },
      ],
    }).compileComponents();

    TestBed.inject(AuthSession).establish('token', { ...ana, id: 1, role: 'ADMIN', name: 'Admin' });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(UserList);
  });

  afterEach(() => {
    http.verify();
    TestBed.inject(AuthSession).clear();
  });

  it('lista a primeira página por data de cadastro', () => {
    const request = http.expectOne((req) => req.url === `${API_URL}/api/users`);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.get('sort')).toBe('createdAt,desc');
    request.flush(pageOf([ana]));
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Ana');
    expect(text).toContain('ana@example.com');
    expect(text).toContain('Usuário');
    expect(text).toContain('decrescente');
    expect(text).toContain('Novo usuário');
    expect(text).toContain('Abrir');
    expect(linkHref(fixture, '/users/7')).not.toBeNull();
    expect(linkHref(fixture, '/users/new')).not.toBeNull();
  });

  it('mostra a lista vazia', () => {
    http.expectOne((req) => req.url === `${API_URL}/api/users`).flush(pageOf([]));
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Nenhum usuário cadastrado');
    expect(text).toContain('Novo usuário');
  });

  it('ordena por nome crescente', () => {
    http.expectOne((req) => req.url === `${API_URL}/api/users`).flush(pageOf([ana]));
    fixture.detectChanges();

    clickButton(fixture, 'Nome');

    const request = http.expectOne((req) => req.url === `${API_URL}/api/users`);
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('sort')).toBe('name,asc');
    request.flush(pageOf([ana]));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('crescente');
  });

  it('mostra erro quando a lista falha', () => {
    http
      .expectOne((req) => req.url === `${API_URL}/api/users`)
      .flush({ message: 'Access denied' }, { status: 403, statusText: 'Forbidden' });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Não foi possível carregar os usuários.');
  });
});

function pageOf(content: User[]) {
  return {
    content,
    empty: content.length === 0,
    totalElements: content.length,
    totalPages: content.length === 0 ? 0 : 1,
    number: 0,
    size: 10,
  };
}

function clickButton(fixture: ComponentFixture<UserList>, label: string): void {
  const buttons = [...fixture.nativeElement.querySelectorAll('button')] as HTMLButtonElement[];
  const button = buttons.find((item) => item.textContent?.includes(label));
  if (!button) throw new Error(`Botão ${label} não encontrado`);
  button.click();
  fixture.detectChanges();
}

function linkHref(fixture: ComponentFixture<UserList>, href: string): HTMLAnchorElement | null {
  const links = [...fixture.nativeElement.querySelectorAll('a')] as HTMLAnchorElement[];
  return links.find((link) => link.getAttribute('href') === href) ?? null;
}
