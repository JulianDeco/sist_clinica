package com.kuris.config.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extrae tenant_id del SecurityContext (claims JWT) y lo almacena en TenantContextHolder (FR-07).
 * Corre después de JwtAuthenticationFilter.
 */
public class TenantContextFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.getCredentials() instanceof Claims claims) {
        String tenantId = (String) claims.get("tenant_id");
        if (tenantId != null) {
          TenantContextHolder.set(UUID.fromString(tenantId));
        }
      }
      chain.doFilter(request, response);
    } finally {
      TenantContextHolder.clear();
    }
  }
}
