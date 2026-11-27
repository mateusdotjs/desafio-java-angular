import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButton } from '@angular/material/button';
import { MatCard, MatCardContent } from '@angular/material/card';
import { MatPaginator, MatPaginatorIntl, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EMPTY, Subject, catchError, finalize, forkJoin, map, switchMap } from 'rxjs';

import { AuthSession } from '../../core/auth/auth-session';
import { User } from '../../core/auth/auth.models';
import { formatZipCode } from '../../core/forms/validation';
import { apiErrorMessage } from '../../core/http/api-error';
import { Page } from '../../core/http/page';
import { ownerId } from '../../core/routing/owner-id';
import { routeTitle } from '../../core/routing/route-title';
import { NotificationService } from '../../core/services/notification.service';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { Address } from '../addresses/address.models';
import { AddressService } from '../addresses/address.service';
import { UserService } from '../users/user.service';

const PAGE_SIZE = 10;

interface AccountLoad {
  userId: number;
  page: number;
  size: number;
  includeProfile: boolean;
}

interface AccountSnapshot {
  user: User | null;
  addresses: Page<Address>;
}

@Component({
  selector: 'app-account',
  imports: [MatProgressSpinner, MatCard, MatCardContent, MatButton, MatPaginator, RouterLink, EmptyState],
  providers: [{ provide: MatPaginatorIntl, useFactory: portuguesePaginatorIntl }],
  templateUrl: './account.html',
  styleUrl: './account.css',
})
export class Account {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly session = inject(AuthSession);
  private readonly users = inject(UserService);
  private readonly addresses = inject(AddressService);
  private readonly notifications = inject(NotificationService);
  private readonly loads = new Subject<AccountLoad>();

  protected readonly title = routeTitle();
  protected readonly formatZip = formatZipCode;
  protected readonly pageSizeOptions = [10, 20];
  protected readonly loading = signal(true);
  protected readonly listLoading = signal(false);
  protected readonly failed = signal(false);
  protected readonly profile = signal<User | null>(null);
  protected readonly items = signal<Address[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(PAGE_SIZE);
  protected readonly confirmingId = signal<number | null>(null);
  protected readonly deletingId = signal<number | null>(null);

  constructor() {
    this.loads
      .pipe(
        switchMap((request) => this.fetch(request)),
        takeUntilDestroyed(),
      )
      .subscribe((snapshot) => this.applySnapshot(snapshot));

    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      this.openAccount(params.get('userId'));
    });
  }

  protected roleLabel(role: User['role']): string {
    return role === 'ADMIN' ? 'Administrador' : 'Usuário';
  }

  protected createdAtLabel(createdAt: string): string {
    const date = new Date(createdAt);
    if (Number.isNaN(date.getTime())) return createdAt;
    return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(date);
  }

  protected editProfileLink(): string[] {
    const raw = this.route.snapshot.paramMap.get('userId');
    return raw === null ? ['/me', 'edit'] : ['/users', raw, 'edit'];
  }

  protected newAddressLink(): string[] {
    const raw = this.route.snapshot.paramMap.get('userId');
    return raw === null ? ['/me', 'addresses', 'new'] : ['/users', raw, 'addresses', 'new'];
  }

  protected editAddressLink(addressId: number): string[] {
    const raw = this.route.snapshot.paramMap.get('userId');
    const id = String(addressId);
    return raw === null ? ['/me', 'addresses', id, 'edit'] : ['/users', raw, 'addresses', id, 'edit'];
  }

  protected changePage(event: PageEvent): void {
    if (this.deletingId() !== null) return;
    const userId = this.currentUserId();
    if (userId === null) return;
    if (event.pageIndex === this.pageIndex() && event.pageSize === this.pageSize()) return;
    this.confirmingId.set(null);
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loads.next({ userId, page: event.pageIndex, size: event.pageSize, includeProfile: false });
  }

  protected askDelete(addressId: number): void {
    if (this.deletingId() !== null) return;
    this.confirmingId.set(addressId);
  }

  protected cancelDelete(): void {
    if (this.deletingId() !== null) return;
    this.confirmingId.set(null);
  }

  protected confirmDelete(addressId: number): void {
    const userId = this.currentUserId();
    if (userId === null || this.deletingId() !== null) return;
    this.deletingId.set(addressId);
    this.addresses
      .delete(userId, addressId)
      .pipe(
        finalize(() => this.deletingId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.notifications.success('Endereço apagado.');
          this.confirmingId.set(null);
          const page = this.items().length === 1 && this.pageIndex() > 0 ? this.pageIndex() - 1 : this.pageIndex();
          this.loads.next({ userId, page, size: this.pageSize(), includeProfile: false });
        },
        error: (error: unknown) => this.notifications.error(apiErrorMessage(error)),
      });
  }

  private openAccount(rawUserId: string | null): void {
    const userId = ownerId(rawUserId, this.session.user()?.id ?? null);
    this.confirmingId.set(null);
    this.pageIndex.set(0);
    this.pageSize.set(PAGE_SIZE);
    if (userId === null) {
      this.loading.set(false);
      this.listLoading.set(false);
      this.failed.set(true);
      this.profile.set(null);
      this.items.set([]);
      this.totalElements.set(0);
      this.notifications.error('Não foi possível carregar a conta.');
      return;
    }
    this.loads.next({ userId, page: 0, size: PAGE_SIZE, includeProfile: true });
  }

  private fetch(request: AccountLoad) {
    if (request.includeProfile) {
      this.loading.set(true);
      this.failed.set(false);
      return forkJoin({
        user: this.users.get(request.userId),
        addresses: this.addresses.list(request.userId, request.page, request.size),
      }).pipe(
        finalize(() => this.loading.set(false)),
        catchError((error: unknown) => {
          this.failed.set(true);
          this.profile.set(null);
          this.items.set([]);
          this.totalElements.set(0);
          this.notifications.error(apiErrorMessage(error));
          return EMPTY;
        }),
      );
    }

    this.listLoading.set(true);
    return this.addresses.list(request.userId, request.page, request.size).pipe(
      map((addresses) => ({ user: null, addresses })),
      finalize(() => this.listLoading.set(false)),
      catchError((error: unknown) => {
        this.notifications.error(apiErrorMessage(error));
        return EMPTY;
      }),
    );
  }

  private applySnapshot(snapshot: AccountSnapshot): void {
    if (snapshot.user !== null) this.profile.set(snapshot.user);
    this.items.set(snapshot.addresses.content);
    this.totalElements.set(snapshot.addresses.totalElements);
    this.pageIndex.set(snapshot.addresses.number);
    this.pageSize.set(snapshot.addresses.size);
    this.failed.set(false);
  }

  private currentUserId(): number | null {
    return ownerId(this.route.snapshot.paramMap.get('userId'), this.session.user()?.id ?? null);
  }
}

function portuguesePaginatorIntl(): MatPaginatorIntl {
  const intl = new MatPaginatorIntl();
  intl.itemsPerPageLabel = 'Itens por página';
  intl.nextPageLabel = 'Próxima página';
  intl.previousPageLabel = 'Página anterior';
  intl.firstPageLabel = 'Primeira página';
  intl.lastPageLabel = 'Última página';
  intl.getRangeLabel = (page, pageSize, length) => {
    if (length === 0 || pageSize === 0) return `0 de ${length}`;
    const start = page * pageSize;
    const end = Math.min(start + pageSize, length);
    return `${start + 1}–${end} de ${length}`;
  };
  return intl;
}
