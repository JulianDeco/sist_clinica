# ADR-010: Capas de Clean Architecture

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation, estándares backend

---

## Contexto

El proyecto tiene reglas de negocio complejas (UC-01 a UC-04) que involucran
múltiples objetos de dominio colaboradores, servicios externos y límites de transacción.
Sin una estructura de capas clara, las reglas de negocio tienden a filtrarse
hacia los controladores o repositorios, volviéndolas imposibles de probar de forma aislada.

## Decisión

Adoptar **Clean Architecture** (Robert C. Martin) adaptada para Spring Boot:

```
Capa API (controladores, DTOs)
  → Capa de aplicación (casos de uso, servicios de aplicación)
    → Capa de dominio (entidades, value objects, interfaces de repositorio)
      ← Capa de infraestructura (repos JPA, adaptadores Redis, clientes HTTP)
```

Regla de dependencia: las capas internas no saben nada sobre las capas externas.
Las entidades de dominio no tienen dependencias de Spring.
Los servicios de aplicación dependen de interfaces de dominio, no de implementaciones
de infraestructura.

## Consecuencias

**Positivo:**
- Casos de uso testeables en tests unitarios puros (sin contexto Spring, sin base de datos)
- Infraestructura intercambiable (por ejemplo, cambiar Redis por Hazelcast) sin tocar
  la lógica de negocio
- Lugar claro para cada clase — sin preguntas de "¿dónde va esto?"
- Académicamente defendible para la tesis (DDD + Clean Architecture bien documentados
  en la literatura)

**Negativo / compromisos:**
- Más clases que un enfoque MVC simple de 3 capas
- Indirección: una solicitud de reserva toca 5–6 clases vs 2 en un enfoque simple

**Riesgos:**
- Sobreingeniería de CRUD simple: para endpoints de gestión de datos pura (configuración
  de tenant, administración de usuarios), un enfoque liviano (sin clase de caso de uso,
  servicio directo → repositorio) es aceptable y está documentado como excepción
