# ADR-004: Redis Solo para Caché y Estado Efímero

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation

---

## Contexto

Varias operaciones del sistema son hot paths que serían demasiado lentas si
consultaran PostgreSQL en cada solicitud: verificaciones de permisos RBAC (cada llamada
a la API), búsquedas del límite de cobertura de obra social (cada reserva), y
verificaciones de revocación de JWT.

## Decisión

Usar **Redis 8** solo para caché y estado efímero. Redis nunca es el sistema de
registro — PostgreSQL es la fuente autoritativa. Si Redis no está disponible,
el sistema recurre a la base de datos y sigue operando.

Usos permitidos:
1. Caché de permisos RBAC por usuario/tenant (TTL 5 min)
2. Contador semanal de cobertura de obra social (TTL 1 hora)
3. Caché de puntuación de riesgo de ausentismo (TTL 30 min)
4. Lista de revocación JTI de JWT (TTL = TTL restante del token)
5. Claves de deduplicación de notificaciones (TTL 48 horas)

## Opciones Consideradas

| Alternativa | Por qué se descartó |
|---|---|
| Caffeine (caché en memoria) | No compartible entre futuras instancias; pierde datos al reiniciar |
| Hazelcast | Demasiado pesado para VPS 4GB RAM; complejidad operacional |
| Sin caché | La consulta de permisos RBAC a la BD en cada llamada a la API agregaría 5–20ms de latencia por solicitud |

## Consecuencias

**Positivo:**
- Búsquedas de permisos en sub-milisegundos
- Actualizaciones del contador de cobertura atómicas (DECR de Redis es atómico)
- Revocación de JWT sin almacenar todos los tokens en la BD

**Negativo / compromisos:**
- Componente de infraestructura adicional
- La lógica de invalidación de caché debe mantenerse
- Ventana de caché desactualizada: hasta 5 min para permisos (aceptable para el MVP)

**Riesgos:**
- OOM de Redis: `maxmemory-policy allkeys-lru` + límite de 128MB definido en Docker
  evita que el contenedor crezca sin límite
