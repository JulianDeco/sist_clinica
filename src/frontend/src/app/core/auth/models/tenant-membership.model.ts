/** Membresía de un usuario en un tenant con su rol asignado (ADR-014). */
export interface TenantMembership {
  readonly tenantId: string;
  readonly tenantName: string;
  readonly role: 'ADMIN' | 'DOCTOR' | 'SECRETARY';
}
