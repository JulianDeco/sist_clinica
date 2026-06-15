import { Injectable, computed, inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';

/** Wrapper de conveniencia sobre AuthService para acceder al tenant activo. */
@Injectable({ providedIn: 'root' })
export class TenantService {
  private readonly auth = inject(AuthService);

  /** Membresía completa del tenant activo, o null si no hay sesión activa. */
  readonly activeTenant = this.auth.activeTenant;
  /** ID del tenant activo, o null si no hay sesión activa. */
  readonly tenantId = computed(() => this.activeTenant()?.tenantId ?? null);
  /** Nombre del tenant activo, o null si no hay sesión activa. */
  readonly tenantName = computed(() => this.activeTenant()?.tenantName ?? null);
}
