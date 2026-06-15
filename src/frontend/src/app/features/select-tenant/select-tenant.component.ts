import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../core/auth/auth.service';
import { TenantMembership } from '../../core/auth/models/tenant-membership.model';

/**
 * Selección de clínica post-login.
 * Si hay un solo tenant, se auto-selecciona en OnInit (nunca debería verse).
 */
@Component({
  selector: 'app-select-tenant',
  standalone: true,
  imports: [MatCardModule, MatListModule, MatIconModule, MatButtonModule],
  template: `
    <div class="wrapper">
      <mat-card class="card">
        <mat-card-header>
          <mat-card-title>Seleccioná tu clínica</mat-card-title>
          <mat-card-subtitle>Tu cuenta tiene acceso a más de una clínica</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <mat-nav-list>
            @for (tenant of auth.availableTenants(); track tenant.tenantId) {
              <a mat-list-item (click)="select(tenant)">
                <mat-icon matListItemIcon>business</mat-icon>
                <span matListItemTitle>{{ tenant.tenantName }}</span>
                <span matListItemLine>{{ roleLabel(tenant.role) }}</span>
              </a>
            }
          </mat-nav-list>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [
    `
      .wrapper {
        display: flex;
        align-items: center;
        justify-content: center;
        min-height: 100vh;
        background: #e3f2fd;
        padding: 24px;
      }
      .card {
        width: 100%;
        max-width: 440px;
      }
    `,
  ],
})
export class SelectTenantComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  private static readonly ROLE_LABELS: Record<string, string> = {
    ADMIN: 'Administrador',
    DOCTOR: 'Médico',
    SECRETARY: 'Secretaria',
  };

  ngOnInit(): void {
    const tenants = this.auth.availableTenants();
    if (tenants.length === 1) this.select(tenants[0]);
  }

  /** Selecciona el tenant indicado y navega a la agenda. */
  select(tenant: TenantMembership): void {
    this.auth.selectTenant(tenant.tenantId).subscribe({
      next: () => this.router.navigate(['/app/agenda']),
    });
  }

  /** Devuelve la etiqueta en español para un rol dado. */
  roleLabel(role: string): string {
    return SelectTenantComponent.ROLE_LABELS[role] ?? role;
  }
}
