import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Protege rutas bajo /app/**.
 * READY → acceso permitido.
 * IDENTITY_CONFIRMED → redirige a /select-tenant.
 * UNAUTHENTICATED → redirige a /.
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  switch (auth.authState()) {
    case 'ready':
      return true;
    case 'identity_confirmed':
      return router.createUrlTree(['/select-tenant']);
    default:
      return router.createUrlTree(['/']);
  }
};
