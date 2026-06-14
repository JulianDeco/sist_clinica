package com.clinicasaas.config;

import com.clinicasaas.config.filter.JwtAuthenticationFilter;
import com.clinicasaas.config.filter.RateLimitFilter;
import com.clinicasaas.config.filter.TenantContextFilter;
import com.clinicasaas.infrastructure.cache.JwtBlocklistService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración de seguridad JWT stateless (T-003, ADR-006, FR-08). Endpoints públicos: login ·
 * select-tenant · refresh · actuator/health. Switch-tenant y logout requieren accessToken válido.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtConfig jwtConfig;
  private final JwtBlocklistService blocklist;

  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  public SecurityConfig(JwtConfig jwtConfig, JwtBlocklistService blocklist) {
    this.jwtConfig = jwtConfig;
    this.blocklist = blocklist;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Públicos (FR-08)
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/auth/login",
                        "/api/v1/auth/select-tenant",
                        "/api/v1/auth/refresh")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/actuator/health")
                    .permitAll()
                    // Todo lo demás requiere autenticación
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(new RateLimitFilter(blocklist), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtConfig, blocklist),
            UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(new TenantContextFilter(), JwtAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
