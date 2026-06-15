# ADR-001: Spring Boot 3 como Framework Backend

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation

---

## Contexto

ClinicaSaaS requiere un backend que maneje CRUD de FHIR R4, autenticación JWT,
aislamiento de datos multitenant, trabajos programados (recordatorios de notificaciones)
e integración con servicios externos (WhatsApp Business API, email SMTP).

El stack anterior (FastAPI / Python) fue evaluado y descartado en favor de un framework
empresarial más completo, mejor adaptado a la complejidad del dominio y a los requisitos
de mantenibilidad a largo plazo del equipo.

## Decisión

Usar **Spring Boot 3.x** sobre **Java 21** como framework de aplicación backend.

Spring Boot 3 provee: Spring Security (auth, RBAC), Spring Data JPA
(ORM + query DSL), Spring Scheduler (jobs de notificación), Spring Actuator
(health, métricas), y un ecosistema maduro para cada integración necesaria.

Los Virtual Threads de Java 21 (Project Loom) permiten alta concurrencia para
el despacho de notificaciones sin bloquear hilos ni introducir complejidad reactiva.

## Alternativas Consideradas

| Alternativa | Por qué se descartó |
|---|---|
| FastAPI (Python) | Usado anteriormente; descartado — carece de biblioteca RBAC madura, complejidad de async SQLAlchemy para multitenancy, patrones de manejo de excepciones menos enterprise-grade |
| Quarkus | Ecosistema más pequeño, menor familiaridad del equipo, menos equivalentes de Spring Security para RBAC granular |
| Micronaut | Brecha de ecosistema similar; los beneficios de imagen nativa con GraalVM no son críticos para este despliegue en VPS |

## Consecuencias

**Positivo:**
- Spring Security provee JWT + RBAC de nivel producción out of the box
- Spring Data JPA elimina la mayor parte del boilerplate para consultas multitenant
- Spring Scheduler cubre UC-03 (job de notificación) de forma nativa
- Maduro y bien documentado — referencias académicas disponibles para la tesis
- Tiempo de inicio de JVM aceptable (< 10s) con healthcheck de Docker

**Negativo / compromisos:**
- Mayor huella de memoria que FastAPI (línea base JVM ~200MB vs ~50MB)
- Inicio en frío más lento (mitigado: el healthcheck de Docker retrasa el tráfico)
- Más verboso que Python para CRUD simple

**Riesgos:**
- VPS 4GB RAM: el límite de memoria de la JVM debe estar explícitamente limitado
  en Docker (máx. 800MB) para dejar espacio a PostgreSQL y Redis

## Notas

Spring Boot 3 requiere Java 17 como mínimo; se usa Java 21 para Virtual Threads.
Maven se usa como herramienta de build (Gradle es igualmente válido pero Maven es más
común en entornos académicos/empresariales para propósitos de documentación).
