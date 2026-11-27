import { AbstractControl, ValidationErrors } from '@angular/forms';

import { FieldError, translateFieldMessage } from '../http/api-error';

export function requiredText(control: AbstractControl): ValidationErrors | null {
  const value: unknown = control.value;
  return typeof value === 'string' && value.trim().length > 0 ? null : { required: true };
}

export function zipDigits(value: string): string {
  return value.replace(/\D/g, '');
}

export function formatZipCode(value: string): string {
  const digits = zipDigits(value);
  return digits.length === 8 ? `${digits.slice(0, 5)}-${digits.slice(5)}` : value;
}

export function zipCode(control: AbstractControl): ValidationErrors | null {
  const value: unknown = control.value;
  if (typeof value !== 'string' || zipDigits(value).length !== 8) return { zipCode: true };
  return null;
}

export function controlError(control: AbstractControl): string | null {
  if (!control.touched || control.errors === null) return null;
  if (control.hasError('required')) return 'Campo obrigatório';
  if (control.hasError('email')) return 'E-mail inválido';

  const minLength = lengthRequirement(control.getError('minlength'));
  if (minLength !== null) return `Mínimo de ${minLength} caracteres`;

  const maxLength = lengthRequirement(control.getError('maxlength'));
  if (maxLength !== null) return `Máximo de ${maxLength} caracteres`;

  if (control.hasError('zipCode')) return 'CEP deve ter 8 dígitos';

  if (control.hasError('server')) {
    const server: unknown = control.getError('server');
    return typeof server === 'string' ? server : 'Valor inválido';
  }

  return null;
}

export function applyServerErrors(form: AbstractControl, fieldErrors: readonly FieldError[]): boolean {
  let applied = false;
  for (const fieldError of fieldErrors) {
    const control = form.get(fieldError.field);
    if (!control) continue;
    control.setErrors({ ...control.errors, server: translateFieldMessage(fieldError.message) });
    control.markAsTouched();
    applied = true;
  }
  return applied;
}

function lengthRequirement(error: unknown): number | null {
  if (typeof error !== 'object' || error === null || !('requiredLength' in error)) return null;
  const requiredLength = error.requiredLength;
  return typeof requiredLength === 'number' ? requiredLength : null;
}
