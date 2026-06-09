import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  LoginCredentials,
  LoginResponse,
  SessionResponse,
} from '../auth/models/token.model';

/** Cliente HTTP para los endpoints de autenticación. */
@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/auth`;

  /** Autentica la identidad; devuelve identity token y lista de tenants. */
  login(credentials: LoginCredentials): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/login`, credentials);
  }

  /** Emite session token para el tenant seleccionado. */
  selectTenant(tenantId: string, identityToken: string): Observable<SessionResponse> {
    return this.http.post<SessionResponse>(
      `${this.base}/select-tenant`,
      { tenantId },
      { headers: { Authorization: `Bearer ${identityToken}` } },
    );
  }

  /** Rota el session token al cambiar de tenant. */
  switchTenant(tenantId: string): Observable<SessionResponse> {
    return this.http.post<SessionResponse>(`${this.base}/switch-tenant`, { tenantId });
  }

  /** Invalida el session token y la cookie de refresh. */
  logout(): Observable<void> {
    return this.http.delete<void>(`${this.base}/logout`);
  }
}
