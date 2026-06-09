/** Identidad global del usuario autenticado. */
export interface User {
  readonly id: string;
  readonly email: string;
  readonly fullName: string;
}
