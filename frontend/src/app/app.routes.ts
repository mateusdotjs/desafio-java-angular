import { Routes } from '@angular/router';

import { AddressForm } from './features/addresses/address-form/address-form';
import { Account } from './features/account/account';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { UserForm } from './features/users/user-form/user-form';
import { UserList } from './features/users/user-list/user-list';
import { Shell } from './layout/shell/shell';

export const routes: Routes = [
  {
    path: '',
    component: Shell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'login' },
      { path: 'login', title: 'Entrar', component: Login },
      { path: 'register', title: 'Criar conta', component: Register },
      { path: 'me', title: 'Minha conta', data: { title: 'Minha conta' }, component: Account },
      { path: 'me/edit', title: 'Editar meus dados', data: { title: 'Editar meus dados' }, component: UserForm },
      { path: 'me/addresses/new', title: 'Novo endereço', data: { title: 'Novo endereço' }, component: AddressForm },
      {
        path: 'me/addresses/:addressId/edit',
        title: 'Editar endereço',
        data: { title: 'Editar endereço' },
        component: AddressForm,
      },
      { path: 'users', title: 'Usuários', component: UserList },
      { path: 'users/new', title: 'Novo usuário', data: { title: 'Novo usuário' }, component: UserForm },
      { path: 'users/:userId', title: 'Usuário', data: { title: 'Usuário' }, component: Account },
      {
        path: 'users/:userId/edit',
        title: 'Editar usuário',
        data: { title: 'Editar usuário' },
        component: UserForm,
      },
      {
        path: 'users/:userId/addresses/new',
        title: 'Novo endereço',
        data: { title: 'Novo endereço' },
        component: AddressForm,
      },
      {
        path: 'users/:userId/addresses/:addressId/edit',
        title: 'Editar endereço',
        data: { title: 'Editar endereço' },
        component: AddressForm,
      },
      { path: '**', redirectTo: 'login' },
    ],
  },
];
