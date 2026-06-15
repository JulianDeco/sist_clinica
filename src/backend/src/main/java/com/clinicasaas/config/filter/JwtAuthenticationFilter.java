package com.clinicasaas.config.filter;

import com.clinicasaas.application.auth.TokenBlocklistPort;
import com.clinicasaas.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Valida el Bearer token en cada request y puebla el SecurityContext (FR-06). Rechaza tokens con
 * JTI en el blocklist Redis (AC-09).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtConfig jwtConfig;
  private final TokenBlocklistPort blocklist;

  public JwtAuthenticationFilter(JwtConfig jwtConfig, TokenBlocklistPort blocklist) {
    this.jwtConfig = jwtConfig;
    this.blocklist = blocklist;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = header.substring(7);
    try {
      Claims claims = jwtConfig.parseAndValidate(token);

      // Rechazar identity tokens (purpose=tenant-select) como session tokens
      if (claims.get("purpose") != null) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      if (blocklist.isBlocked(claims.getId())) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      String role = (String) claims.get("role");
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(
              claims.getSubject(),
              claims,
              role != null ? List.of(new SimpleGrantedAuthority("ROLE_" + role)) : List.of());
      SecurityContextHolder.getContext().setAuthentication(auth);

    } catch (JwtException e) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    chain.doFilter(request, response);
  }
}
