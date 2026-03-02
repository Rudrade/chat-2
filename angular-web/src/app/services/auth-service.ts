import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly route = inject(Router);
  private readonly httpClient = inject(HttpClient);
  private readonly keyStorage = 'sessionData';
  private username?: string;

  login(username: string, password: string) {
    return this.httpClient.post<AuthResponse>('http://localhost:8080/chat/api/user/login', {
      username,
      password,
    });
  }

  logout() {
    sessionStorage.removeItem(this.keyStorage);
    this.route.navigate(['/login']);
  }

  getToken() {
    let token = sessionStorage.getItem(this.keyStorage);
    if (!token) {
      this.logout();
      return;
    }

    return token;
  }

  setToken(token: string) {
    sessionStorage.setItem(this.keyStorage, token);
  }

  setCurrentUsername(username: string) {
    this.username = username;
  }

  getCurrenUsername() {
    return this.username;
  }

  isUserAuthenticated() {
    const token = sessionStorage.getItem(this.keyStorage);
    if (!token) {
      return false;
    }

    const decodedToken = jwtDecode(token);
    if (!decodedToken?.exp) {
      return false;
    }

    return decodedToken.exp * 1000 > Date.now();
  }

  getSubject() {
    const token = this.getToken();
    const decoded = jwtDecode(token!);

    return decoded?.sub;
  }
}

export interface AuthResponse {
  token: string;
}
