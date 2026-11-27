import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    fixture.detectChanges();
  });

  it('pede só e-mail e senha', () => {
    expect(fixture.nativeElement.querySelector('input[formControlName="name"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('input[formControlName="email"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('input[formControlName="password"]')).not.toBeNull();
  });

  it('exige senha com 8 caracteres', () => {
    setInput(fixture, 'email', 'ana@example.com');
    setInput(fixture, 'password', '1234567');

    submit(fixture);

    expect(fixture.nativeElement.textContent).toContain('Mínimo de 8 caracteres');
  });
});

function setInput(fixture: ComponentFixture<Login>, name: string, value: string): void {
  const input: HTMLInputElement | null = fixture.nativeElement.querySelector(
    `input[formControlName="${name}"]`,
  );
  if (!input) throw new Error(`Campo ${name} não encontrado`);
  input.value = value;
  input.dispatchEvent(new Event('input'));
  input.dispatchEvent(new Event('blur'));
  fixture.detectChanges();
}

function submit(fixture: ComponentFixture<Login>): void {
  const button: HTMLButtonElement | null = fixture.nativeElement.querySelector('button[type="submit"]');
  if (!button) throw new Error('Botão de envio não encontrado');
  button.click();
  fixture.detectChanges();
}
