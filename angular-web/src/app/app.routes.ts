import { Routes } from '@angular/router';
import { App } from './app';
import { authGuard } from './guards/auth-guard';
import { Login } from './components/login/login';

export const routes: Routes = [
  {
    path: '',
    component: App,
    canActivate: [authGuard],
  },
  {
    path: 'login',
    component: Login,
  },
  {
    path: '**',
    redirectTo: 'login',
  },
];
