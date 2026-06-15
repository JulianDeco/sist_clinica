# ADR-005: Angular 18 como Framework Frontend

**Estado**: ACCEPTED
**Fecha**: 2026-06-08
**Autor**: Julián Deco
**Relaciona con**: Foundation

---

## Contexto

ClinicaSaaS requiere un frontend con: gestión compleja de formularios (reserva de
turnos con validación en múltiples pasos), control de vistas basado en roles (los
médicos ven vistas distintas a las secretarias), una vista de agenda basada en
calendario, e integración con una API REST de Spring Boot protegida por JWT.

## Decisión

Usar **Angular 18** con **Angular Material** para el frontend.

Angular 18 provee: Signals para la gestión de estado (no se necesita NgRx para el MVP),
interceptores HTTP integrados para encabezados de autenticación, RxJS para operaciones
asíncronas, y Angular Material para la biblioteca completa de componentes.

## Opciones Consideradas

| Alternativa | Por qué se descartó |
|---|---|
| Next.js (App Router) | Stack usado anteriormente. El proyecto migra a Java/Spring — Angular alinea el stack tecnológico de forma más natural para un proyecto empresarial/académico |
| React (Vite SPA) | Sin convenciones integradas de enrutamiento, gestión de estado o interceptores HTTP — requiere ensamblar 4–5 bibliotecas separadas |
| Vue 3 | Ecosistema más pequeño para el equivalente empresarial de Angular Material; menos referencias académicas |

## Consecuencias

**Positivo:**
- Angular Material: biblioteca completa de componentes empresariales (calendario, tablas,
  formularios) sin CSS personalizado
- Inyección de dependencias integrada — consistente con los patrones de Spring Boot
- Interceptores: inyección de encabezados de auth + tenant en un solo lugar
- Modo estricto de TypeScript: reduce errores en tiempo de ejecución para modelos
  de datos complejos
- Angular Signals (17+): no se necesita NgRx para la escala del MVP

**Negativo / compromisos:**
- Curva de aprendizaje más alta que React para componentes simples
- Más boilerplate (módulos/componentes standalone) que Next.js

**Riesgos:**
- Los Signals de Angular 18 son relativamente nuevos — respuestas limitadas en
  StackOverflow comparado con patrones NgRx
