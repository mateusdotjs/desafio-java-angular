import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, catchError, finalize, map, switchMap } from 'rxjs';

import { AuthSession } from '../../core/auth/auth-session';
import { apiErrorMessage } from '../../core/http/api-error';
import { routeTitle } from '../../core/routing/route-title';
import { NotificationService } from '../../core/services/notification.service';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { Address } from '../addresses/address.models';
import { AddressService } from '../addresses/address.service';

@Component({
  selector: 'app-account',
  imports: [MatProgressSpinner, EmptyState],
  templateUrl: './account.html',
  styleUrl: './account.css',
})
export class Account {
  private readonly route = inject(ActivatedRoute);
  private readonly session = inject(AuthSession);
  private readonly addresses = inject(AddressService);
  private readonly notifications = inject(NotificationService);

  protected readonly title = routeTitle();
  protected readonly loading = signal(true);
  protected readonly failed = signal(false);
  protected readonly items = signal<Address[]>([]);

  constructor() {
    this.route.paramMap
      .pipe(
        switchMap((params) => this.fetchAddresses(params.get('userId'))),
        takeUntilDestroyed(),
      )
      .subscribe((addresses) => {
        this.items.set(addresses);
        this.failed.set(false);
      });
  }

  private fetchAddresses(rawUserId: string | null) {
    const userId = this.resolveUserId(rawUserId);
    if (userId === null) {
      this.loading.set(false);
      this.failed.set(true);
      this.items.set([]);
      this.notifications.error('Não foi possível carregar os endereços.');
      return EMPTY;
    }

    this.loading.set(true);
    this.failed.set(false);
    return this.addresses.list(userId).pipe(
      map((page) => page.content ?? []),
      finalize(() => this.loading.set(false)),
      catchError((error: unknown) => {
        this.failed.set(true);
        this.items.set([]);
        this.notifications.error(apiErrorMessage(error));
        return EMPTY;
      }),
    );
  }

  private resolveUserId(rawUserId: string | null): number | null {
    if (rawUserId === null) return this.session.user()?.id ?? null;
    const parsed = Number(rawUserId);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
  }
}
