package com.kuris.config.filter;

import java.util.UUID;

/** ThreadLocal con el tenant_id de la sesión actual (FR-07). */
public final class TenantContextHolder {

  private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

  private TenantContextHolder() {}

  public static void set(UUID tenantId) {
    CURRENT.set(tenantId);
  }

  public static UUID get() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }
}
