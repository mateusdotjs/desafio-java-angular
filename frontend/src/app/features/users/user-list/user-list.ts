import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButton } from '@angular/material/button';
import { MatPaginator, MatPaginatorIntl, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { EMPTY, Subject, catchError, finalize, switchMap } from 'rxjs';

import { User } from '../../../core/auth/auth.models';
import { apiErrorMessage } from '../../../core/http/api-error';
import { Page } from '../../../core/http/page';
import { routeTitle } from '../../../core/routing/route-title';
import { NotificationService } from '../../../core/services/notification.service';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { portuguesePaginatorIntl } from '../../../shared/paginator-intl';
import { UserService } from '../user.service';

const PAGE_SIZE = 10;

type UserSortField = 'name' | 'email' | 'createdAt';
type SortDirection = 'asc' | 'desc';

interface UserListLoad {
  page: number;
  size: number;
  sort: string;
  initial: boolean;
}

@Component({
  selector: 'app-user-list',
  imports: [MatProgressSpinner, MatButton, MatPaginator, RouterLink, EmptyState],
  providers: [{ provide: MatPaginatorIntl, useFactory: portuguesePaginatorIntl }],
  templateUrl: './user-list.html',
  styleUrl: './user-list.css',
})
export class UserList {
  private readonly users = inject(UserService);
  private readonly notifications = inject(NotificationService);
  private readonly loads = new Subject<UserListLoad>();

  protected readonly title = routeTitle();
  protected readonly pageSizeOptions = [10, 20];
  protected readonly sortOptions: readonly { field: UserSortField; label: string }[] = [
    { field: 'name', label: 'Nome' },
    { field: 'email', label: 'E-mail' },
    { field: 'createdAt', label: 'Cadastro' },
  ];
  protected readonly loading = signal(true);
  protected readonly listLoading = signal(false);
  protected readonly failed = signal(false);
  protected readonly items = signal<User[]>([]);
  protected readonly totalElements = signal(0);
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(PAGE_SIZE);
  protected readonly sortField = signal<UserSortField>('createdAt');
  protected readonly sortDirection = signal<SortDirection>('desc');

  constructor() {
    this.loads
      .pipe(
        switchMap((request) => this.fetch(request)),
        takeUntilDestroyed(),
      )
      .subscribe((page) => this.apply(page));

    this.loads.next({ page: 0, size: PAGE_SIZE, sort: this.sortParam(), initial: true });
  }

  protected directionLabel(field: UserSortField): string | null {
    if (this.sortField() !== field) return null;
    return this.sortDirection() === 'asc' ? 'crescente' : 'decrescente';
  }

  protected roleLabel(role: User['role']): string {
    return role === 'ADMIN' ? 'Administrador' : 'Usuário';
  }

  protected createdAtLabel(createdAt: string): string {
    const date = new Date(createdAt);
    if (Number.isNaN(date.getTime())) return createdAt;
    return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(date);
  }

  protected sortBy(field: UserSortField): void {
    if (this.sortField() === field) {
      this.sortDirection.update((current) => (current === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortField.set(field);
      this.sortDirection.set('asc');
    }
    this.pageIndex.set(0);
    this.loads.next({ page: 0, size: this.pageSize(), sort: this.sortParam(), initial: false });
  }

  protected changePage(event: PageEvent): void {
    if (event.pageIndex === this.pageIndex() && event.pageSize === this.pageSize()) return;
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loads.next({
      page: event.pageIndex,
      size: event.pageSize,
      sort: this.sortParam(),
      initial: false,
    });
  }

  private fetch(request: UserListLoad) {
    if (request.initial) {
      this.loading.set(true);
      this.failed.set(false);
    } else {
      this.listLoading.set(true);
    }

    return this.users.list(request.page, request.size, request.sort).pipe(
      finalize(() => {
        if (request.initial) this.loading.set(false);
        else this.listLoading.set(false);
      }),
      catchError((error: unknown) => {
        if (request.initial) {
          this.failed.set(true);
          this.items.set([]);
          this.totalElements.set(0);
        }
        this.notifications.error(apiErrorMessage(error));
        return EMPTY;
      }),
    );
  }

  private apply(page: Page<User>): void {
    this.items.set(page.content);
    this.totalElements.set(page.totalElements);
    this.pageIndex.set(page.number);
    this.pageSize.set(page.size);
    this.failed.set(false);
  }

  private sortParam(): string {
    return `${this.sortField()},${this.sortDirection()}`;
  }
}
