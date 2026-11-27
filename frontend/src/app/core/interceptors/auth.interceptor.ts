import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { API_URL } from '../api-url';
import { AuthSession } from '../auth/auth-session';
import { NotificationService } from '../services/notification.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const session = inject(AuthSession);
  const router = inject(Router);
  const notifications = inject(NotificationService);
  const token = session.accessToken();
  const outgoing = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(outgoing).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !isCredentialRequest(request.url)) {
        session.clear();
        notifications.error('Sessão expirada. Entre novamente.');
        void router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};

function isCredentialRequest(url: string): boolean {
  const path = new URL(url, API_URL).pathname;
  return path === '/api/auth/login' || path === '/api/auth/register';
}
