import { AbstractControl } from '@angular/forms';

import { NotificationService } from '../../core/services/notification.service';
import { applyServerErrors } from '../../core/forms/validation';
import { apiErrorMessage, readApiError } from '../../core/http/api-error';

export function reportAuthError(
  form: AbstractControl,
  error: unknown,
  notifications: NotificationService,
): void {
  const body = readApiError(error);
  if (body && applyServerErrors(form, body.fieldErrors)) {
    notifications.error('Revise os campos destacados.');
    return;
  }
  notifications.error(apiErrorMessage(error));
}
