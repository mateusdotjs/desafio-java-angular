import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatError, MatFormField, MatLabel, MatSuffix } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject, catchError, finalize, map, of, switchMap } from 'rxjs';

import { AuthSession } from '../../../core/auth/auth-session';
import {
  applyServerErrors,
  controlError,
  formatZipCode,
  requiredText,
  zipCode,
  zipDigits,
} from '../../../core/forms/validation';
import { apiErrorMessage, readApiError } from '../../../core/http/api-error';
import { ownerId, positiveId } from '../../../core/routing/owner-id';
import { routeTitle } from '../../../core/routing/route-title';
import { NotificationService } from '../../../core/services/notification.service';
import { AddressRequest, ZipCodeLookup } from '../address.models';
import { AddressService } from '../address.service';
import { ZipCodeService } from '../zip-code.service';

@Component({
  selector: 'app-address-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCard,
    MatCardContent,
    MatFormField,
    MatLabel,
    MatError,
    MatSuffix,
    MatInput,
    MatButton,
    MatProgressSpinner,
  ],
  templateUrl: './address-form.html',
  styleUrl: './address-form.css',
})
export class AddressForm {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly session = inject(AuthSession);
  private readonly addresses = inject(AddressService);
  private readonly zipCodes = inject(ZipCodeService);
  private readonly notifications = inject(NotificationService);
  private readonly zipLookups = new Subject<string>();
  private readonly userId: number | null;
  private readonly addressId: number | null;

  protected readonly title = routeTitle();
  protected readonly loading = signal(false);
  protected readonly failed = signal(false);
  protected readonly lookingUp = signal(false);
  protected readonly submitting = signal(false);
  protected readonly preview = signal<ZipCodeLookup | null>(null);
  protected readonly form = inject(FormBuilder).nonNullable.group({
    zipCode: ['', [requiredText, zipCode]],
    number: ['', [requiredText, Validators.maxLength(20)]],
    complement: ['', [Validators.maxLength(255)]],
  });

  constructor() {
    const params = this.route.snapshot.paramMap;
    this.userId = ownerId(params.get('userId'), this.session.user()?.id ?? null);
    this.addressId = positiveId(params.get('addressId'));

    this.zipLookups
      .pipe(
        switchMap((zip) => this.lookup(zip)),
        takeUntilDestroyed(),
      )
      .subscribe((result) => this.applyLookup(result));

    this.form.controls.zipCode.valueChanges.pipe(takeUntilDestroyed()).subscribe((value) => {
      const shown = this.preview();
      if (shown !== null && shown.zipCode !== zipDigits(value)) this.preview.set(null);
    });

    if (this.userId === null || (params.has('addressId') && this.addressId === null)) {
      this.failed.set(true);
      this.notifications.error('Não foi possível abrir o endereço.');
      return;
    }

    if (this.addressId !== null) this.loadAddress(this.userId, this.addressId);
  }

  protected fieldError(name: 'zipCode' | 'number' | 'complement'): string | null {
    return controlError(this.form.controls[name]);
  }

  protected previewMatches(): boolean {
    const shown = this.preview();
    return shown !== null && shown.zipCode === zipDigits(this.form.controls.zipCode.value);
  }

  protected backLink(): string[] {
    const raw = this.route.snapshot.paramMap.get('userId');
    return raw === null ? ['/me'] : ['/users', raw];
  }

  protected lookupZip(): void {
    const control = this.form.controls.zipCode;
    const zip = zipDigits(control.value);
    control.markAsTouched();
    if (zip.length === 8) {
      const formatted = formatZipCode(zip);
      if (control.value !== formatted) control.setValue(formatted);
    }
    if (zip.length !== 8) {
      this.preview.set(null);
      return;
    }
    if (this.preview()?.zipCode === zip) return;
    this.zipLookups.next(zip);
  }

  protected submit(): void {
    this.form.controls.number.setValue(this.form.controls.number.value.trim());
    this.form.controls.complement.setValue(this.form.controls.complement.value.trim());
    if (this.form.invalid || !this.previewMatches() || this.userId === null) {
      this.form.markAllAsTouched();
      return;
    }

    const userId = this.userId;
    const complement = this.form.controls.complement.value;
    const request: AddressRequest = {
      zipCode: zipDigits(this.form.controls.zipCode.value),
      number: this.form.controls.number.value,
      complement: complement.length > 0 ? complement : null,
    };
    const addressId = this.addressId;
    const save =
      addressId === null
        ? this.addresses.create(userId, request)
        : this.addresses.update(userId, addressId, request);

    this.submitting.set(true);
    save
      .pipe(
        finalize(() => this.submitting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.notifications.success('Endereço salvo.');
          void this.router.navigate(this.backLink());
        },
        error: (error: unknown) => this.reportSaveError(error),
      });
  }

  private lookup(zip: string) {
    this.lookingUp.set(true);
    return this.zipCodes.lookup(zip).pipe(
      map((preview) => ({ zip, preview, error: null as unknown })),
      catchError((error: unknown) => of({ zip, preview: null, error })),
      finalize(() => this.lookingUp.set(false)),
    );
  }

  private applyLookup(result: { zip: string; preview: ZipCodeLookup | null; error: unknown }): void {
    if (zipDigits(this.form.controls.zipCode.value) !== result.zip) return;
    if (result.preview !== null) {
      this.preview.set(result.preview);
      this.form.controls.zipCode.setErrors(null);
      return;
    }
    this.preview.set(null);
    this.form.controls.zipCode.setErrors({ server: apiErrorMessage(result.error) });
    this.form.controls.zipCode.markAsTouched();
  }

  private loadAddress(userId: number, addressId: number): void {
    this.loading.set(true);
    this.addresses
      .get(userId, addressId)
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (address) => {
          this.form.setValue({
            zipCode: formatZipCode(address.zipCode),
            number: address.number,
            complement: address.complement ?? '',
          });
          this.preview.set({
            zipCode: zipDigits(address.zipCode),
            street: address.street,
            neighborhood: address.neighborhood,
            city: address.city,
            state: address.state,
          });
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
