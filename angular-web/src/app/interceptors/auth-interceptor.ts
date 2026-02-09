import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth-service';

@Injectable({
  providedIn: 'root',
})
export class AuthInterceptor implements HttpInterceptor {
  private readonly authService = inject(AuthService);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (req.url.includes('/login')) {
      return next.handle(req);
    }

    const token = this.authService.getToken();
    if (token) {
      req = req.clone({
        headers: req.headers.append('Authorization', 'Bearer ' + token),
      });
    }

    return next.handle(req);
  }
}
