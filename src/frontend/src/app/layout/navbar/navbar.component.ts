import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from '../../core/auth/auth.service';
import { TenantService } from '../../core/tenant/tenant.service';

/** Barra de navegación superior con nombre del tenant activo y menú de usuario. */
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule],
  template: `
    <mat-toolbar color="primary" class="navbar">
      <span class="brand">ClinicaSaaS</span>
      <span class="tenant-name" *ngIf="tenant.tenantName()">
        — {{ tenant.tenantName() }}
      </span>
      <span class="spacer"></span>

      <button mat-icon-button [matMenuTriggerFor]="userMenu" aria-label="Menú de usuario">
        <mat-icon>account_circle</mat-icon>
      </button>

      <mat-menu #userMenu="matMenu">
        @if (auth.availableTenants().length > 1) {
          <button mat-menu-item [matMenuTriggerFor]="tenantMenu">
            <mat-icon>swap_horiz</mat-icon>
            <span>Cambiar clínica</span>
          </button>
        }
        <button mat-menu-item (click)="auth.logout()">
          <mat-icon>logout</mat-icon>
          <span>Cerrar sesión</span>
        </button>
      </mat-menu>

      <mat-menu #tenantMenu="matMenu">
        @for (t of auth.availableTenants(); track t.tenantId) {
          <button mat-menu-item (click)="switchTenant(t.tenantId)"
                  [disabled]="t.tenantId === tenant.tenantId()">
            {{ t.tenantName }}
          </button>
        }
      </mat-menu>
    </mat-toolbar>
  `,
  styles: [`
    .navbar { position: fixed; top: 0; left: 0; right: 0; z-index: 100; }
    .brand { font-weight: 600; }
    .tenant-name { margin-left: 8px; font-weight: 400; opacity: .85; }
    .spacer { flex: 1; }
  `],
})
export class NavbarComponent {
  readonly auth = inject(AuthService);
  readonly tenant = inject(TenantService);
  private readonly router = inject(Router);

  switchTenant(tenantId: string): void {
    this.auth.switchTenant(tenantId).subscribe({
      next: () => this.router.navigate(['/app/agenda']),
    });
  }
}
