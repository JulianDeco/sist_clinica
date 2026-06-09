import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Protege la ruta /select-tenant.
 * IDENTITY_CONFIRMED → acceso permitido.
 * READY → redirige a /app/agenda.
 * UNAUTHENTICATED → redirige a /.
 */
export const selectTenantGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  switch (auth.authState()) {
    case 'identity_confirmed':
      return true;
    case 'ready':
      return router.createUrlTree(['/app/agenda']);
    default:
      return router.createUrlTree(['/']);
  }
};
