import { Injectable, computed, inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';

/** Wrapper de conveniencia sobre AuthService para acceder al tenant activo. */
@Injectable({ providedIn: 'root' })
export class TenantService {
  private readonly auth = inject(AuthService);

  readonly activeTenant = computed(() => this.auth.activeTenant());
  readonly tenantId = computed(() => this.auth.activeTenant()?.tenantId ?? null);
  readonly tenantName = computed(() => this.auth.activeTenant()?.tenantName ?? null);
}
