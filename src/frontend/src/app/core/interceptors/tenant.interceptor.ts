import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { environment } from '../../../environments/environment';

/** Adjunta X-Tenant-ID cuando el estado es READY y la petición va a apiBaseUrl. */
export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const tenantId = auth.activeTenant()?.tenantId;

  if (!req.url.startsWith(environment.apiBaseUrl) || !tenantId || auth.authState() !== 'ready') {
    return next(req);
  }

  return next(req.clone({ setHeaders: { 'X-Tenant-ID': tenantId } }));
};
