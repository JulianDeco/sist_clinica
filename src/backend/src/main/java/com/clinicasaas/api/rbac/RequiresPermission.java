package com.clinicasaas.api.rbac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Declara el permiso granular requerido para invocar un endpoint. Uso:
 * {@code @RequiresPermission("APPOINTMENT_CREATE")}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority('{value}')")
public @interface RequiresPermission {
  String value();
}
