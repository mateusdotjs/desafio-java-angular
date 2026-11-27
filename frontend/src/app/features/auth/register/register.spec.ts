import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Register } from './register';

describe('Register', () => {
  let fixture: ComponentFixture<Register>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    fixture.detectChanges();
  });

  it('exige nome', () => {
    setInput(fixture, 'email', 'ana@example.com');
    setInput(fixture, 'password', '12345678');

    submit(fixture);

    expect(fixture.nativeElement.textContent).toContain('Campo obrigatório');
  });

  it('exige senha com 8 caracteres', () => {
    setInput(fixture, 'name', 'Ana');
    setInput(fixture, 'email', 'ana@example.com');
    setInput(fixture, 'password', '1234567');

    submit(fixture);

    expect(fixture.nativeElement.textContent).toContain('Mínimo de 8 caracteres');
  });
});

function setInput(fixture: ComponentFixture<Register>, name: string, value: string): void {
  const input: HTMLInputElement | null = fixture.nativeElement.querySelector(
    `input[formControlName="${name}"]`,
  );
  if (!input) throw new Error(`Campo ${name} não encontrado`);
  input.value = value;
  input.dispatchEvent(new Event('input'));
  input.dispatchEvent(new Event('blur'));
  fixture.detectChanges();
}

function submit(fixture: ComponentFixture<Register>): void {
  const button: HTMLButtonElement | null = fixture.nativeElement.querySelector('button[type="submit"]');
  if (!button) throw new Error('Botão de envio não encontrado');
  button.click();
  fixture.detectChanges();
}
