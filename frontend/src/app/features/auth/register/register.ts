import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatError, MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import { controlError, requiredText } from '../../../core/forms/validation';
import { NotificationService } from '../../../core/services/notification.service';
import { reportAuthError } from '../auth-feedback';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCard,
    MatCardContent,
    MatFormField,
    MatLabel,
    MatError,
    MatInput,
    MatButton,
    MatProgressSpinner,
  ],
  templateUrl: './register.html',
  styleUrl: '../auth-form.css',
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly submitting = signal(false);
  protected readonly form = inject(FormBuilder).nonNullable.group({
    name: ['', [requiredText, Validators.maxLength(255)]],
    email: ['', [requiredText, Validators.email, Validators.maxLength(255)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  protected fieldError(name: 'name' | 'email' | 'password'): string | null {
    return controlError(this.form.controls[name]);
  }

  protected submit(): void {
    this.form.controls.name.setValue(this.form.controls.name.value.trim());
    this.form.controls.email.setValue(this.form.controls.email.value.trim());
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const { name, email, password } = this.form.getRawValue();
    this.auth
      .register({ name, email, password })
      .pipe(
        finalize(() => this.submitting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.notifications.success('Conta criada.');
          void this.router.navigate(['/me']);
        },
        error: (error: unknown) => reportAuthError(this.form, error, this.notifications),
      });
  }
}
