import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Protege rutas por rol. Usar como: canActivate: [authGuard, roleGuard('DOCTOR')].
 * Si el rol del tenant activo no coincide → redirige a /app/unauthorized.
 */
export const roleGuard = (requiredRole: string): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.hasRole(requiredRole) ? true : router.createUrlTree(['/app/unauthorized']);
};
