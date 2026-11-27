import { HttpErrorResponse } from '@angular/common/http';

export interface FieldError {
  field: string;
  message: string;
}

export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors: FieldError[];
}

const KNOWN_MESSAGES: Record<string, string> = {
  'Invalid email or password': 'E-mail ou senha inválidos.',
  'Email already exists': 'Este e-mail já está cadastrado.',
  'Authentication required': 'Sessão expirada. Entre novamente.',
  'Access denied': 'Você não tem permissão para esta ação.',
  'Validation failed': 'Revise os campos destacados.',
  'Invalid zip code': 'CEP inválido ou não encontrado.',
  'Zip code service is unavailable': 'A consulta de CEP está indisponível.',
  'User not found': 'Usuário não encontrado.',
  'Address not found': 'Endereço não encontrado.',
};

const FIELD_MESSAGES: Record<string, string> = {
  'must not be blank': 'Campo obrigatório',
  'must not be empty': 'Campo obrigatório',
  'must be a well-formed email address': 'E-mail inválido',
  'size must be between 8 and 72': 'A senha deve ter entre 8 e 72 caracteres',
  'size must be between 0 and 20': 'Máximo de 20 caracteres',
  'size must be between 0 and 255': 'Máximo de 255 caracteres',
  'must match "^\\d{8}$"': 'CEP deve ter 8 dígitos',
};

export function readApiError(error: unknown): ApiErrorBody | null {
  if (!(error instanceof HttpErrorResponse)) return null;
  const body: unknown = error.error;
  if (typeof body !== 'object' || body === null) return null;

  const candidate = body as Partial<ApiErrorBody>;
  if (typeof candidate.message !== 'string') return null;

  return {
    timestamp: typeof candidate.timestamp === 'string' ? candidate.timestamp : '',
    status: typeof candidate.status === 'number' ? candidate.status : error.status,
    error: typeof candidate.error === 'string' ? candidate.error : '',
    message: candidate.message,
    path: typeof candidate.path === 'string' ? candidate.path : '',
    fieldErrors: readFieldErrors(candidate.fieldErrors),
  };
}

export function translateFieldMessage(message: string): string {
  return FIELD_MESSAGES[message] ?? message;
}

export function apiErrorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Não foi possível concluir a operação.';
  }
  if (error.status === 0) {
    return 'Não foi possível conectar à API.';
  }

  const body = readApiError(error);
  if (body) {
    const known = KNOWN_MESSAGES[body.message];
    if (known) return known;
    if (body.message.trim().length > 0) return body.message;
  }
  if (error.status === 403) return 'Você não tem permissão para esta ação.';
  if (error.status === 400) return 'Revise os campos destacados.';
  return 'Não foi possível concluir a operação.';
}

function readFieldErrors(value: unknown): FieldError[] {
  if (!Array.isArray(value)) return [];
  return value.filter(isFieldError);
}

function isFieldError(value: unknown): value is FieldError {
  if (typeof value !== 'object' || value === null) return false;
  const fieldError = value as Partial<FieldError>;
  return typeof fieldError.field === 'string' && typeof fieldError.message === 'string';
}
