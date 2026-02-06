import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly route = inject(Router);
  private readonly httpClient = inject(HttpClient);

  isUserAuthenticated() {
    return false;
  }

  logout() {
    this.route.navigate(['/login']);
  }

  login(username: string, password: string) {
    return this.httpClient.post('http://localhost:8080/chat/api/auth/login', {
      username,
      password,
    });
  }
}
