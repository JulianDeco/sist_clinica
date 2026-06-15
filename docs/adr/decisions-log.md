# Registro de Decisiones (Informal, Solo Adición)

Este archivo captura decisiones no obvias a medida que ocurren, antes de que sean
formalizadas en ADRs. Solo se agregan entradas — nunca se editan las existentes.

---

## 2026-06-08 — Migración completa del stack de Python/FastAPI a Java 21 + Spring Boot

**Contexto**: El proyecto tenía un stack comprometido en Python/FastAPI + Next.js con
código de autenticación y migraciones. Se tomó la decisión de migrar a un stack
empresarial Java.

**Decisión**: Descartar FastAPI + Next.js por completo. Adoptar Java 21 + Spring Boot 3
+ Angular 18. Todos los archivos de contexto, CLAUDE.md y la documentación fueron
reescritos.

**Razón**: Spring Boot provee RBAC más maduro, manejo de excepciones e infraestructura
de testing para la complejidad de FHIR R4 + casos de uso multitenant.
Angular 18 se alinea mejor con un proyecto empresarial/académico que Next.js.

**Formalizado en**: ADR-001 (Spring Boot), ADR-005 (Angular)

---

## 2026-06-08 — Adopción de Spec-Driven Development como workflow obligatorio

**Contexto**: El desarrollo asistido por IA puede generar código que no coincide con
los requisitos. Un proyecto de un solo desarrollador necesita disciplina para evitar
implementaciones basadas en suposiciones. La tesis requiere documentación trazable.

**Decisión**: Cada feature debe comenzar con un archivo de spec
(`docs/modules/{x}/specs/{Feature}.spec.md`) aprobado antes de comenzar a codificar.
Tests escritos a partir de los casos de prueba del spec (TDD desde el spec).

**Razón**: La trazabilidad (UC → spec → test → código) tiene valor académico
y previene comportamientos no documentados en el código generado por IA.

**Formalizado en**: ADR-011

---

## 2026-06-08 — Multitenancy por fila confirmado para el stack Java

**Contexto**: Limitaciones del VPS de 4GB RAM. Una base de datos PostgreSQL compartida
por todos los tenants.

**Decisión**: Misma decisión que antes (trasladada desde el stack Python): por fila
con columna `tenant_id`. Aplicación mediante `TenantContextFilter` ThreadLocal
y clase base `TenantAwareRepository` en Spring Data JPA.

**Formalizado en**: ADR-003

---

## 2026-06-09 — Separar identidad de membresía para usuarios multi-tenant

**Contexto**: El schema original tenía `users.tenant_id` hardcodeado, lo que
impedía que una persona trabajara en múltiples clínicas con una sola cuenta.
Caso real: médico con dos consultorios, secretaria compartida.

**Decisión**: Separar identidad (tabla `users`, email único global) de membresía
(tabla `user_tenants`: user × tenant × role). El login devuelve un identity token
de 5 minutos; el session token con `tenant_id` + `role` se emite al seleccionar
tenant. Un botón "Cambiar clínica" en el navbar permite el switch sin re-login.

**Razón**: UX real para usuarios compartidos. El flujo de dos pasos agrega un
solo round-trip que se omite automáticamente si el usuario pertenece a un solo tenant.

**Formalizado en**: ADR-014
