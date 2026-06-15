# ADR-007: Docker + Docker Compose para Despliegue

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation, infraestructura

---

## Contexto

El sistema corre en un solo VPS con 4GB RAM. El entorno de desarrollo debe
coincidir con el de producción lo más posible. El equipo es un solo desarrollador.

## Decisión

Usar contenedores **Docker** para todos los servicios y **Docker Compose** para
la orquestación (desarrollo y producción). Sin Kubernetes para el MVP.

Servicios: `nginx`, `backend` (Spring Boot), `frontend` (Angular compilado estático),
`postgres`, `redis`.

## Consecuencias

**Positivo:**
- Paridad dev/prod: las mismas imágenes localmente y en el VPS
- Nginx, PostgreSQL, Redis no requieren instalación manual
- `docker compose up` inicia el stack completo
- Límites de memoria por contenedor aplicables mediante `mem_limit`

**Negativo / compromisos:**
- Sin auto-escalado (VPS único, no necesario para el MVP con 1–5 clínicas)
- Sin despliegues progresivos (breve tiempo de inactividad con `docker compose up`
  con nueva imagen)

**Riesgos:**
- VPS 4GB RAM: la memoria total de los contenedores debe mantenerse por debajo de
  1.5GB para dejar margen al sistema operativo
  Ver: [Arquitectura de Alto Nivel §7](../architecture/01-high-level-architecture.md) para los límites
