# Stack — Versiones y tecnologías

> Stack replanificado 2026-06-08: migración completa a Java 21 + Spring Boot + Angular 18.
> Stack anterior (FastAPI + Next.js) descartado.
> ADRs: ADR-001 (Spring Boot), ADR-005 (Angular), ADR-008 (Flyway).
> Entorno: JDK 21, Maven 3.x, Node.js LTS.

## Backend

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje backend, Virtual Threads (Project Loom) |
| Spring Boot | 3.x (latest stable) | Framework principal — web, security, data, scheduler |
| Spring Security | (incluido en Boot) | JWT auth, RBAC, filtros de seguridad |
| Spring Data JPA | (incluido en Boot) | ORM + query derivados + @Query |
| Hibernate | (incluido en Boot) | JPA provider |
| JJWT | 0.12.x | Firma y validación de JWT |
| Flyway | 10.x | Migraciones de base de datos (única fuente de verdad de schema) |
| Maven | 3.9.x | Build + dependency management |
| JUnit 5 | (incluido en Boot Test) | Tests unitarios e integración |
| Mockito | (incluido en Boot Test) | Mocking en tests unitarios |
| Testcontainers | latest | PostgreSQL + Redis reales en tests de integración |
| AssertJ | (incluido en Boot Test) | Assertions fluidas |
| SpringDoc OpenAPI | 2.x | Documentación API (Swagger UI) |
| Bucket4j | latest | Rate limiting |

## Infraestructura

| Tecnología | Versión | Uso |
|---|---|---|
| PostgreSQL | 16 | BD principal (FHIR como JSONB + tablas relacionales) |
| Redis | 8 | Cache permisos (TTL 5min), tope obra social (TTL 1h), JTI blocklist |
| Docker | latest stable | Contenedores de todos los servicios |
| Docker Compose | v2 | Orquestación dev + producción |
| Nginx | latest stable | Reverse proxy, SSL termination, static SPA serving |

## Frontend

| Tecnología | Versión | Uso |
|---|---|---|
| Angular | 18 (latest stable) | Framework SPA |
| TypeScript | 5.x (strict mode) | Lenguaje frontend |
| Angular Material | 18.x | Biblioteca de componentes UI |
| RxJS | 7.x (incluido) | Operaciones asíncronas |
| Angular Signals | (incluido en Angular 18) | State management (reemplaza NgRx para MVP) |
| Jasmine + Karma | (incluido en Angular) | Tests unitarios de componentes y servicios |
| HttpClientTestingModule | (incluido) | Testing de servicios HTTP |

## Motor de inteligencia (MVP)

Implementado en Java dentro del módulo `intelligence`:
- Reglas heurísticas en clases Java puras (sin ML)
- Score 0–100 + lista de factores explicables en lenguaje natural
- Consultas SQL agregadas a través de Spring Data JPA
- Sin dependencias de ML para MVP

## Decisiones de stack (ver ADRs en docs/adr/)

| Decisión | ADR |
|---|---|
| Spring Boot sobre FastAPI | ADR-001 |
| PostgreSQL | ADR-002 |
| Row-level multitenancy | ADR-003 |
| Redis solo como cache | ADR-004 |
| Angular 18 | ADR-005 |
| JWT con refresh rotation | ADR-006 |
| Docker + Compose | ADR-007 |
| Flyway | ADR-008 |
| FHIR como JSONB | ADR-009 |
| Clean Architecture | ADR-010 |
| Spec-Driven Development | ADR-011 |
