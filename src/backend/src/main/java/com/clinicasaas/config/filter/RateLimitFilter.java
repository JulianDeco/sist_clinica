package com.clinicasaas.config.filter;

import com.clinicasaas.infrastructure.cache.JwtBlocklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate limiting en POST /api/v1/auth/login: máximo 5 req/IP/min (FR-09). IP extraída de
 * X-Forwarded-For (primer valor) porque el backend corre detrás de Nginx.
 */
public class RateLimitFilter extends OncePerRequestFilter {

  private static final String LOGIN_PATH = "/api/v1/auth/login";

  private final JwtBlocklistService blocklist;

  public RateLimitFilter(JwtBlocklistService blocklist) {
    this.blocklist = blocklist;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if ("POST".equals(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI())) {
      String ip = extractIp(request);
      if (blocklist.isRateLimited(ip)) {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"RATE_LIMIT_EXCEEDED\"}");
        return;
      }
    }
    chain.doFilter(request, response);
  }

  private String extractIp(HttpServletRequest request) {
    // WHY: Nginx está configurado con `proxy_set_header X-Real-IP $remote_addr` (sobrescribe,
    // no appendea). Usamos X-Real-IP porque es imposible de spoofoear desde el cliente —
    // solo Nginx puede escribirlo. X-Forwarded-For se evita deliberadamente porque un
    // cliente malicioso puede enviar valores prefijados y bypassear el rate limit.
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return realIp.trim();
    }
    return request.getRemoteAddr();
  }
}
