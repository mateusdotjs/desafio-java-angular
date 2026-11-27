import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatError, MatFormField, MatHint, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthSession } from '../../../core/auth/auth-session';
import { UserCreateRequest, UserUpdateRequest } from '../../../core/auth/auth.models';
import { applyServerErrors, controlError, requiredText } from '../../../core/forms/validation';
import { apiErrorMessage, readApiError } from '../../../core/http/api-error';
import { ownerId } from '../../../core/routing/owner-id';
import { routeTitle } from '../../../core/routing/route-title';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../user.service';

@Component({
  selector: 'app-user-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCard,
    MatCardContent,
    MatFormField,
    MatLabel,
    MatHint,
    MatError,
    MatInput,
    MatButton,
    MatProgressSpinner,
  ],
  templateUrl: './user-form.html',
  styleUrl: './user-form.css',
})
export class UserForm {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly session = inject(AuthSession);
  private readonly users = inject(UserService);
  private readonly notifications = inject(NotificationService);
  private readonly userId: number | null;

  protected readonly title = routeTitle();
  protected readonly creating = this.route.snapshot.data['create'] === true;
  protected readonly loading = signal(false);
  protected readonly failed = signal(false);
  protected readonly submitting = signal(false);
  protected readonly form = inject(FormBuilder).nonNullable.group({
    name: ['', [requiredText, Validators.maxLength(255)]],
    email: ['', [requiredText, Validators.email, Validators.maxLength(255)]],
    password: [
      '',
      this.creating
        ? [Validators.required, Validators.minLength(8), Validators.maxLength(72)]
        : [Validators.minLength(8), Validators.maxLength(72)],
    ],
  });

  constructor() {
    if (this.creating) {
      this.userId = null;
      return;
    }

    this.userId = ownerId(this.route.snapshot.paramMap.get('userId'), this.session.user()?.id ?? null);
    if (this.userId === null) {
      this.failed.set(true);
      this.notifications.error('Não foi possível abrir os dados.');
      return;
    }
    this.load(this.userId);
  }

  protected fieldError(name: 'name' | 'email' | 'password'): string | null {
    return controlError(this.form.controls[name]);
  }

  protected backLink(): string[] {
    if (this.creating) return ['/users'];
    const raw = this.route.snapshot.paramMap.get('userId');
    return raw === null ? ['/me'] : ['/users', raw];
  }

  protected submit(): void {
    this.form.controls.name.setValue(this.form.controls.name.value.trim());
    this.form.controls.email.setValue(this.form.controls.email.value.trim());
    this.form.controls.password.setValue(this.form.controls.password.value.trim());
    if (this.form.invalid || (!this.creating && this.userId === null)) {
      this.form.markAllAsTouched();
      return;
    }

    const { name, email, password } = this.form.getRawValue();
    if (this.creating) {
      this.create({ name, email, password });
      return;
    }
    if (this.userId === null) return;
    this.update(this.userId, { name, email, password: password.length > 0 ? password : null });
  }

  private create(request: UserCreateRequest): void {
    this.submitting.set(true);
    this.users
      .create(request)
      .pipe(
        finalize(() => this.submitting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (created) => {
          this.notifications.success('Usuário criado.');
          void this.router.navigate(['/users', created.id]);
        },
        error: (error: unknown) => this.reportSaveError(error),
      });
  }

  private update(userId: number, request: UserUpdateRequest): void {
    this.submitting.set(true);
    this.users
      .update(userId, request)
      .pipe(
        finalize(() => this.submitting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (updated) => {
          if (this.session.user()?.id === updated.id) this.session.updateUser(updated);
          this.notifications.success('Dados atualizados.');
          void this.router.navigate(this.backLink());
        },
        error: (error: unknown) => this.reportSaveError(error),
      });
  }

  private load(userId: number): void {
    this.loading.set(true);
    this.users
      .get(userId)
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (user) => {
          this.form.patchValue({ name: user.name, email: user.email });
        },
        error: (error: unknown) => {
          this.failed.set(true);
          this.notifications.error(apiErrorMessage(error));
        },
      });
  }

  private reportSaveError(error: unknown): void {
    const body = readApiError(error);
    if (body && applyServerErrors(this.form, body.fieldErrors)) {
      this.notifications.error('Revise os campos destacados.');
      return;
    }
    this.notifications.error(apiErrorMessage(error));
  }
}
