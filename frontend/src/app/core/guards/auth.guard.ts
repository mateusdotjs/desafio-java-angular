import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthSession } from '../auth/auth-session';

export const authGuard: CanActivateFn = () => {
  if (inject(AuthSession).isAuthenticated()) return true;
  return inject(Router).createUrlTree(['/login']);
};

export const guestGuard: CanActivateFn = () => {
  if (!inject(AuthSession).isAuthenticated()) return true;
  return inject(Router).createUrlTree(['/me']);
};
