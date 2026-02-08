import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth-service';

export const authGuard: CanActivateFn = (route, state) => {
  const service = inject(AuthService);
  const userAuthenticated = service.isUserAuthenticated();
  if (!userAuthenticated) {
    service.logout();
    return false;
  }

  return userAuthenticated;
};
