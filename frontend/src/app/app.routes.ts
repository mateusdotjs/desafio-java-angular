import { inject } from '@angular/core';
import { Routes } from '@angular/router';

import { AuthSession } from './core/auth/auth-session';
import { adminGuard, authGuard, guestGuard } from './core/guards/auth.guard';
import { AddressForm } from './features/addresses/address-form/address-form';
import { Account } from './features/account/account';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { UserForm } from './features/users/user-form/user-form';
import { UserList } from './features/users/user-list/user-list';
import { Shell } from './layout/shell/shell';

function redirectToSessionHome(): string {
  return inject(AuthSession).isAuthenticated() ? '/me' : '/login';
}

export const routes: Routes = [
  {
    path: '',
    component: Shell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: redirectToSessionHome },
      { path: 'login', title: 'Entrar', canActivate: [guestGuard], component: Login },
      { path: 'register', title: 'Criar conta', canActivate: [guestGuard], component: Register },
      {
        path: '',
        canActivate: [authGuard],
        children: [
          { path: 'me', title: 'Minha conta', data: { title: 'Minha conta' }, component: Account },
          { path: 'me/edit', title: 'Editar meus dados', data: { title: 'Editar meus dados' }, component: UserForm },
          { path: 'me/addresses/new', title: 'Novo endereço', data: { title: 'Novo endereço' }, component: AddressForm },
          {
            path: 'me/addresses/:addressId/edit',
            title: 'Editar endereço',
            data: { title: 'Editar endereço' },
            component: AddressForm,
          },
          {
            path: 'users',
            canActivate: [adminGuard],
            children: [
              { path: '', pathMatch: 'full', title: 'Usuários', data: { title: 'Usuários' }, component: UserList },
              {
                path: 'new',
                title: 'Novo usuário',
                data: { title: 'Novo usuário', create: true },
                component: UserForm,
              },
              { path: ':userId', title: 'Usuário', data: { title: 'Usuário' }, component: Account },
              {
                path: ':userId/edit',
                title: 'Editar usuário',
                data: { title: 'Editar usuário' },
                component: UserForm,
              },
              {
                path: ':userId/addresses/new',
                title: 'Novo endereço',
                data: { title: 'Novo endereço' },
                component: AddressForm,
              },
              {
                path: ':userId/addresses/:addressId/edit',
                title: 'Editar endereço',
                data: { title: 'Editar endereço' },
                component: AddressForm,
              },
            ],
          },
          { path: '**', redirectTo: '/me' },
        ],
      },
    ],
  },
];
