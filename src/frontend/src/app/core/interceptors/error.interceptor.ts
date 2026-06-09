import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * Interceptor global de errores HTTP.
 * 401 → limpia estado y redirige a / (salvo durante switch-tenant o select-tenant).
 * 403 → snackbar "Sin permiso".
 * 5xx → snackbar "Error del servidor".
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        const isSelectOrSwitch =
          req.url.includes('/auth/select-tenant') ||
          req.url.includes('/auth/switch-tenant');

        if (!isSelectOrSwitch && !auth.isSwitchingTenant && !auth.isRedirecting) {
          auth.isRedirecting = true;
          auth.clearState();
          router.navigate(['/']);
        }
      } else if (error.status === 403) {
        snackBar.open('Sin permiso', 'Cerrar', { duration: 4000 });
      } else if (error.status >= 500) {
        snackBar.open('Error del servidor', 'Cerrar', { duration: 4000 });
      }

      return throwError(() => error);
    }),
  );
};
