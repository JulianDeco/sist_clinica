package com.clinicasaas.config.filter;

import com.clinicasaas.application.auth.PermissionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Carga permisos granulares tras la validación JWT y los añade al SecurityContext como
 * GrantedAuthority. Orden: después de JwtAuthenticationFilter (ADR-003, T-004).
 */
public class PermissionLoadingFilter extends OncePerRequestFilter {

  private final PermissionService permissionService;

  public PermissionLoadingFilter(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth != null && auth.isAuthenticated() && auth.getCredentials() instanceof Claims claims) {
      String tenantIdStr = (String) claims.get("tenant_id");
      String userIdStr = claims.getSubject();

      if (tenantIdStr != null && userIdStr != null) {
        try {
          UUID tenantId = UUID.fromString(tenantIdStr);
          UUID userId = UUID.fromString(userIdStr);
          Set<String> permissions = permissionService.getPermissions(tenantId, userId);

          List<SimpleGrantedAuthority> authorities =
              permissions.stream().map(SimpleGrantedAuthority::new).toList();

          UsernamePasswordAuthenticationToken enriched =
              new UsernamePasswordAuthenticationToken(
                  auth.getPrincipal(), auth.getCredentials(), authorities);
          SecurityContextHolder.getContext().setAuthentication(enriched);

        } catch (IllegalArgumentException ignored) {
          // UUID malformado — el request seguirá sin permisos y será rechazado por @PreAuthorize
        }
      }
    }

    chain.doFilter(request, response);
  }
}
