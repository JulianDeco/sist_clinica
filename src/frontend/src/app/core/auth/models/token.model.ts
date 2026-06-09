/** Respuesta del endpoint POST /auth/login. */
export interface LoginResponse {
  readonly identityToken: string;
  readonly tenants: import('./tenant-membership.model').TenantMembership[];
}

/** Respuesta del endpoint POST /auth/select-tenant y /auth/switch-tenant. */
export interface SessionResponse {
  readonly accessToken: string;
}

/** Credenciales enviadas al login. */
export interface LoginCredentials {
  readonly email: string;
  readonly password: string;
}
