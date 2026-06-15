import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, of, tap, switchMap, map, finalize } from 'rxjs';
import { AuthApi } from '../api/auth.api';
import { LoginCredentials } from './models/token.model';
import { TenantMembership } from './models/tenant-membership.model';
import { User } from './models/user.model';

/** Estados del flujo de autenticación two-step (ADR-014). */
export type AuthState = 'unauthenticated' | 'identity_confirmed' | 'ready';

/**
 * Servicio singleton de autenticación.
 * Máquina de estados: UNAUTHENTICATED → IDENTITY_CONFIRMED → READY.
 * Ningún token toca localStorage/sessionStorage (NFR-03).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthApi);
  private readonly router = inject(Router);

  readonly authState = signal<AuthState>('unauthenticated');
  readonly currentUser = signal<User | null>(null);
  readonly availableTenants = signal<TenantMembership[]>([]);
  readonly activeTenant = signal<TenantMembership | null>(null);

  /** Token de identidad temporal (5 min, solo para flujo select-tenant). */
  private identityToken: string | null = null;
  /** Token de sesión activo. Accesible por interceptores. */
  accessToken: string | null = null;
  /** Evita múltiples redirecciones a / en respuestas 401 concurrentes. */
  isRedirecting = false;
  /** Evita redirigir a / durante un switch de tenant en progreso. */
  isSwitchingTenant = false;

  readonly isAuthenticated = computed(() => this.authState() === 'ready');

  /** Verifica si el usuario tiene el rol indicado en el tenant activo. */
  hasRole(role: string): boolean {
    return this.activeTenant()?.role === role;
  }

  /**
   * Paso 1 del login: autentica identidad.
   * Si hay un solo tenant, llama selectTenant() automáticamente.
   */
  login(credentials: LoginCredentials): Observable<void> {
    return this.api.login(credentials).pipe(
      tap((response) => {
        this.identityToken = response.identityToken;
        this.availableTenants.set(response.tenants);
        this.authState.set('identity_confirmed');
      }),
      switchMap((response) =>
        response.tenants.length === 1
          ? this.selectTenant(response.tenants[0].tenantId)
          : of(undefined as void),
      ),
    );
  }

  /** Paso 2: selecciona tenant y emite session token. */
  selectTenant(tenantId: string): Observable<void> {
    return this.api.selectTenant(tenantId, this.identityToken!).pipe(
      tap((response) => {
        this.accessToken = response.accessToken;
        this.identityToken = null;
        const tenant = this.availableTenants().find((t) => t.tenantId === tenantId)!;
        this.activeTenant.set(tenant);
        this.authState.set('ready');
      }),
      map(() => undefined as void),
    );
  }

  /** Cambia al tenant indicado sin re-login. */
  switchTenant(tenantId: string): Observable<void> {
    this.isSwitchingTenant = true;
    return this.api.switchTenant(tenantId).pipe(
      tap((response) => {
        this.accessToken = response.accessToken;
        const tenant = this.availableTenants().find((t) => t.tenantId === tenantId)!;
        this.activeTenant.set(tenant);
      }),
      map(() => undefined as void),
      finalize(() => {
        this.isSwitchingTenant = false;
      }),
    );
  }

  /** Cierra sesión y limpia todo el estado en memoria. */
  logout(): void {
    this.api.logout().subscribe({ error: () => {} });
    this.clearState();
    this.router.navigate(['/']);
  }

  /** Limpia el estado sin llamar al backend (usado por ErrorInterceptor en 401). */
  clearState(): void {
    this.accessToken = null;
    this.identityToken = null;
    this.authState.set('unauthenticated');
    this.currentUser.set(null);
    this.availableTenants.set([]);
    this.activeTenant.set(null);
    this.isRedirecting = false;
    this.isSwitchingTenant = false;
  }
}
