import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth-service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  console.log('intercept:', req.url);

  if (req.url.includes('/login')) {
    return next(req);
  }

  const authService = inject(AuthService);
  const token = authService.getToken();
  console.log('including token:', token);

  if (token) {
    req = req.clone({
      headers: req.headers.append('Authorization', 'Bearer ' + token),
    });
    console.log('token included', req);
  }

  return next(req);
};
