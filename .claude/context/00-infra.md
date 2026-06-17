# Infra — Docker, Nginx, Variables de entorno

> Stack: Java 21 + Spring Boot 3 + Angular 18 (migrado 2026-06-08).
> ADR-007: Docker + Compose como único mecanismo de despliegue.

## Arquitectura de servicios

```
Internet → Nginx (443/80) → Spring Boot (8080) → PostgreSQL 16 (5432)
                                                → Redis 8 (6379)
           Nginx (443/80) → Angular SPA (estático, servido por Nginx)
```

## Variables de entorno requeridas

| Variable | Requerida | Descripción |
|---|---|---|
| `DB_HOST` | Sí | Host PostgreSQL (default: `postgres` en Docker) |
| `DB_PORT` | No | Puerto PostgreSQL (default: 5432) |
| `DB_NAME` | Sí | Nombre de la base de datos |
| `DB_USERNAME` | Sí | Usuario PostgreSQL |
| `DB_PASSWORD` | Sí | Contraseña PostgreSQL |
| `REDIS_HOST` | Sí | Host Redis (default: `redis` en Docker) |
| `REDIS_PORT` | No | Puerto Redis (default: 6379) |
| `REDIS_PASSWORD` | Sí | Contraseña Redis |
| `JWT_SECRET` | Sí | Clave JWT — mínimo 512 bits (64 chars aleatorios) |
| `JWT_EXPIRATION_MINUTES` | No | TTL access token (default: 30) |
| `REFRESH_TOKEN_EXPIRATION_DAYS` | No | TTL refresh token (default: 7) |
| `ALLOWED_ORIGINS` | Sí | CORS allowed origins (ej: `https://clinica.example.com`) |
| `ENVIRONMENT` | No | `development` / `production` |
| `API_URL` | Sí (frontend) | URL del backend para el build Angular |

## Archivos de infra planificados

| Archivo | Rol |
|---|---|
| `docker-compose.yml` | Orquestación de servicios (backend, frontend, postgres, redis, nginx) |
| `docker-compose.dev.yml` | Override para desarrollo local (hot reload, puertos expuestos) |
| `infra/nginx/default.conf` | Reverse proxy + SSL termination + static Angular serve |
| `infra/postgres/init.sql` | Init script PostgreSQL (extensiones: uuid-ossp, pg_trgm) |
| `.env.example` | Plantilla de variables — nunca commitear `.env` real |

## Inicio del servicio backend

Spring Boot corre Flyway automáticamente al arrancar (antes de servir requests):

```yaml
# docker-compose.yml
backend:
  image: kuris-backend:latest
  mem_limit: 800m
  environment:
    SPRING_PROFILES_ACTIVE: prod
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 3
  depends_on:
    postgres:
      condition: service_healthy
    redis:
      condition: service_healthy
```

## Límites de memoria (VPS 4GB RAM)

| Servicio | Límite |
|---|---|
| backend (JVM) | 800 MB |
| frontend (Nginx static) | 64 MB |
| postgres | 512 MB |
| redis | 128 MB |
| nginx (proxy) | 32 MB |

## Notas

- VPS: 4GB RAM — no agregar servicios sin evaluar impacto de RAM
- `spring.jpa.hibernate.ddl-auto=validate` — Flyway es el único mecanismo de migraciones
- PostgreSQL y Redis no exponen puertos al exterior (solo red interna Docker)
- Angular build: `ng build --configuration production` genera `dist/` servido por Nginx
- JVM flags recomendados: `-XX:+UseG1GC -XX:MaxRAMPercentage=75.0`
