import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { take } from 'rxjs';
import { translateError } from '../../util/app-util';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { ButtonModule } from 'primeng/button';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, InputTextModule, MessageModule, ButtonModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  form = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  submitting = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  onSubmit() {
    // Validate form
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const username = this.form.get('username')?.value;
    const password = this.form.get('password')?.value;
    if (!username || !password) {
      this.form.markAllAsDirty();
      return;
    }

    // If valid, call backend
    this.submitting.set(true);
    this.errorMessage.set(null);

    this.authService
      .login(username, password)
      .pipe(take(1))
      .subscribe({
        next: (res) => {
          this.submitting.set(false);
          this.authService.setToken(res.token);
          this.authService.setCurrentUsername(username);
          this.router.navigate(['/']);
        },
        error: (err) => {
          const msg = translateError(err);
          this.errorMessage.set(msg);
          this.submitting.set(false);
        },
      });
  }
}
