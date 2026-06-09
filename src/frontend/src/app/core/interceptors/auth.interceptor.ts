import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { environment } from '../../../environments/environment';

/** Adjunta Authorization: Bearer <token> a peticiones hacia apiBaseUrl. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  if (!req.url.startsWith(environment.apiBaseUrl) || !auth.accessToken) {
    return next(req);
  }

  return next(
    req.clone({ setHeaders: { Authorization: `Bearer ${auth.accessToken}` } }),
  );
};
