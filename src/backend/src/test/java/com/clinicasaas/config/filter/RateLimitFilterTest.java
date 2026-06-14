package com.clinicasaas.config.filter;

import static org.mockito.BDDMockito.*;

import com.clinicasaas.infrastructure.cache.JwtBlocklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

  @Mock JwtBlocklistService blocklist;
  @Mock HttpServletRequest request;
  @Mock HttpServletResponse response;
  @Mock FilterChain chain;

  @InjectMocks RateLimitFilter filter;

  // ── TC-10: 6º intento desde la misma IP → 429 ────────────────────────────
  @Test
  void login_rateLimitExceeded_returns429() throws Exception {
    given(request.getMethod()).willReturn("POST");
    given(request.getRequestURI()).willReturn("/api/v1/auth/login");
    given(request.getHeader("X-Real-IP")).willReturn("10.0.0.1");
    given(blocklist.isRateLimited("10.0.0.1")).willReturn(true);
    given(response.getWriter()).willReturn(new PrintWriter(new StringWriter()));

    filter.doFilterInternal(request, response, chain);

    then(response).should().setStatus(429);
    then(chain).should(never()).doFilter(any(), any());
  }

  // ── TC-10b: dentro del límite → pasa la cadena ────────────────────────────
  @Test
  void login_withinLimit_proceedsToChain() throws Exception {
    given(request.getMethod()).willReturn("POST");
    given(request.getRequestURI()).willReturn("/api/v1/auth/login");
    given(request.getHeader("X-Real-IP")).willReturn("10.0.0.2");
    given(blocklist.isRateLimited("10.0.0.2")).willReturn(false);

    filter.doFilterInternal(request, response, chain);

    then(chain).should().doFilter(request, response);
  }
}
