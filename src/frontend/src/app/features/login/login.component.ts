import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/auth/auth.service';

/** Formulario de autenticación. Transiciona al estado IDENTITY_CONFIRMED o READY. */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatProgressSpinnerModule,
  ],
  template: `
    <div class="login-wrapper">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>Kuris</mat-card-title>
          <mat-card-subtitle>Ingresá a tu cuenta</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput type="email" formControlName="email" autocomplete="email">
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Contraseña</mat-label>
              <input matInput type="password" formControlName="password" autocomplete="current-password">
            </mat-form-field>

            @if (error()) {
              <p class="error-msg">{{ error() }}</p>
            }

            <button mat-raised-button color="primary" type="submit"
                    [disabled]="form.invalid || loading()" class="full-width submit-btn">
              @if (loading()) {
                <mat-spinner diameter="20" />
              } @else {
                Iniciar sesión
              }
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions>
          <a mat-button routerLink="/">Volver al inicio</a>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-wrapper {
      display: flex; align-items: center; justify-content: center;
      min-height: 100vh; background: #e3f2fd; padding: 24px;
    }
    .login-card { width: 100%; max-width: 400px; padding: 16px; }
    .full-width { width: 100%; }
    .submit-btn { margin-top: 8px; height: 44px; }
    .error-msg { color: #d32f2f; font-size: .875rem; margin-bottom: 8px; }
  `],
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set(null);

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        if (this.auth.authState() === 'ready') {
          this.router.navigate(['/app/agenda']);
        } else {
          this.router.navigate(['/select-tenant']);
        }
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Email o contraseña incorrectos');
      },
    });
  }
}
