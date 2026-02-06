import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { take } from 'rxjs';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  form = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  private readonly authService = inject(AuthService);

  submitting = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  onSubmit() {
    // Validate form
    if (this.form.invalid) {
      this.form.markAllAsDirty();
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
        next: () => this.submitting.set(false),
        error: (err) => {
          this.errorMessage.set(err);
          this.submitting.set(false);
        },
      });
  }
}
