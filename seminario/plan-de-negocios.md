---
title: "Kuris — Plan de Negocios"
subtitle: "Seminario de Trabajo Final — UAI 2026"
author: "Julián Decoppet"
---

Legajo: B00114457-T — Sede Rosario
Profesores: Ing. Sartorio Alejandro Roberto e Ing. Banega Matias Pablo

#### Ficha de Seguimiento y Revisión

| Fecha | Autor | Versión | Referencia de los cambios |
|---|---|---|---|
| 19/05/2026 | Julián Decoppet | 1.0 | Versión inicial: Resumen Ejecutivo y punto 1 (1.1 y 1.2). |
| 12/05/2026 | Julián Decoppet | 1.1 | Reescritura del resumen ejecutivo, BMC, Contexto Competitivo y fuentes. |
| 16/06/2026 | Julián Decoppet | 2.0 | Segunda entrega: completado punto 1 (1.3 a 1.12), punto 2, punto 3, punto 4 y punto 5 (5.1 y 5.2). BMC movido a bmc.md (anexo). |
| 08/06/2026 | Julián Decoppet | 2.1 | Consolidación del documento único: integración de entrega-2.md, capítulo 5 nuevo, referencias APA 7 completas. |
| 08/06/2026 | Julián Decoppet | 2.2 | Incorporación de capacidades de IA generativa al MVP: resumen automático de historia clínica y sugerencia de CIE-10 mediante LLM (Claude API). Migración de stack a Java 21 + Spring Boot 3 + Angular 18. Actualización de costos operativos (ítem API LLM) y hoja de ruta. |
| 10/06/2026 | Julián Decoppet | 2.3 | Corrección de consistencia: avance técnico actualizado al estado real post-migración (auth two-step en diseño, 15 ADRs, frontend Angular, docs ICONIX), canales de notificación unificados según ADR-013 (Telegram/email en MVP), justificación del posicionamiento de precio, correcciones menores (Nubimed en capa SaaS, hitos de recordatorio 48/24 h). |
| 17/06/2026 | Julián Decoppet | 2.4–2.8 | Ronda de revisión previa a la entrega: cobertura completa de guía UAI (propuesta asociativa, ROI, comparación por eslabón, perfil sociodemográfico), rigor analítico (costos desglosados, punto de equilibrio, competidores internacionales, riesgos de dependencia externa, validación de mercado), corrección de redacción académica (afirmaciones absolutas reformuladas como hallazgos de relevamiento, repeticiones reducidas, tabla de estado de validación) y adaptación a APA 7 (citas migradas a formato `[@clave]` con `referencias.bib`). |

Nota: El Business Model Canvas se presenta como documento independiente
(bmc.md) para facilitar su visualización y actualización separada.
Los nueve bloques del canvas sintetizan los conceptos desarrollados en
los capítulos 1 a 5 de este documento.

\newpage

# Resumen ejecutivo

Kuris es una plataforma de software de gestión clínica por
suscripción, dirigida a consultorios y clínicas médicas privadas de uno a
cinco profesionales en Argentina, con foco inicial en Rosario y zona de
influencia. La plataforma centraliza la administración de pacientes,
turnos, historia clínica y profesionales sobre una arquitectura
interoperable basada en el estándar internacional HL7 FHIR R4, lo que
favorece la portabilidad, la seguridad y la compatibilidad de los datos
clínicos con otros sistemas de salud que adopten el mismo estándar.

Sobre esa base de gestión integral, Kuris incorpora un conjunto de
capacidades de inteligencia operacional diseñadas para resolver problemas
concretos del segmento.

Capacidades comprometidas en el MVP del seminario (mayo–diciembre 2026):

  - Predicción explicable de ausentismo: el sistema estima la
    probabilidad de que un paciente no se presente a su turno y explica,
    en lenguaje comprensible para el profesional, los factores que
    generan ese riesgo. El módulo opera con reglas heurísticas (historial
    de no-show, anticipación del turno, día y franja horaria) y está
    diseñado para evolucionar a modelos entrenados por clínica una vez
    que cada tenant acumule volumen suficiente.

  - Recordatorios inteligentes diferenciados por riesgo: las
    notificaciones a pacientes se priorizan según la probabilidad de
    ausentismo calculada, reduciendo costo de notificación y aumentando
    la tasa de respuesta efectiva. El módulo de notificaciones está
    diseñado con una abstracción de canal intercambiable: el MVP opera
    con Telegram Bot API (gratuito, sin aprobación) y email; la
    integración con WhatsApp Business API se activa en la fase de
    producción comercial sin modificar la lógica de negocio.

  - Overbooking inteligente: cuando un slot tiene alto riesgo de
    ausentismo, el sistema sugiere reservar el horario para sobreturno o
    doble agenda, con tope parametrizable por el profesional, recuperando
    ingresos perdidos sin sobrecargar la operación.

  - Resumen automático de historia clínica mediante IA generativa: al
    finalizar una consulta, el sistema genera un resumen en lenguaje
    natural del Encuentro clínico —incluyendo notas SOAP, signos vitales
    y diagnóstico— a partir de los datos FHIR estructurados. El médico
    revisa y valida el resumen antes de que quede registrado. Esta
    capacidad reduce la carga administrativa del profesional y constituye
    la primera integración del producto con modelos de lenguaje de gran
    escala (LLM), implementada mediante la API de Claude (Anthropic).

  - Sugerencia automática de clasificación CIE-10: al registrar el
    motivo de consulta en texto libre, el sistema sugiere el código
    CIE-10 más probable, reduciendo el tiempo de codificación clínica
    y el error por omisión. El profesional conserva el criterio final
    sobre el código aplicado.

Capacidades en roadmap post-MVP (alcance comercial, no entregable del
seminario):

  - Sugerencia automática de cobertura óptima por franja horaria.
  - Modelos de Machine Learning entrenados por tenant.
  - Integraciones específicas con obras sociales y prestadores vía FHIR.
  - Marketplace B2C de turnos para pacientes finales.

El ausentismo de pacientes afecta a aproximadamente 1 de cada 3 turnos
médicos en América Latina (30 %, sobre un análisis de 1,5 millones de
turnos en Argentina, Uruguay, Chile, Perú y México), un problema operativo
de alto costo para el cual el segmento de clínicas pequeñas en Argentina
no dispone de una solución ampliamente adoptada [@geblix2024]. En el
relevamiento realizado se identificaron soluciones internacionales y
herramientas de infraestructura abierta con implementación nativa de HL7
FHIR R4 (Medplum, Elation Health, HAPI FHIR), así como competidores
regionales con predicción de ausentismo por machine learning ya en
producción comercial [@geblix2024]. En el relevamiento realizado sobre el
segmento de clínicas de 1 a 5 profesionales en Argentina no se encontraron
evidencias públicas de un competidor que combine el estándar HL7 FHIR R4,
predicción explicable de ausentismo por paciente y optimización
automatizada de cobertura integrada al flujo operativo, en el rango de
precio propuesto (desarrollo del relevamiento en la sección 2.2.1).

El contexto de mercado es favorable: el sector de salud digital en América
Latina supera los USD 5.755 millones y crece al 9,5 % anual (Informes de
Expertos, 2024), con el software representando el 52,1 % del valor total.
El segmento de pequeñas clínicas privadas — aproximadamente 5.000
establecimientos solo en Argentina [@refes2023] — permanece desatendido
por soluciones que combinen interoperabilidad estándar e inteligencia
operacional accesible.

El mapa competitivo argentino se estructura en cuatro capas, desde
consolidadores masivos orientados a volumen (Grupo Cormos) e incumbentes
locales sin diferenciación tecnológica, hasta SaaS modernos genéricos que
recién incorporan IA administrativa y Geblix, el competidor regional más
cercano en inteligencia operacional —con predicción de ausentismo por
machine learning ya en producción comercial, pero sin FHIR R4 ni foco en
clínicas de 1 a 5 profesionales en Argentina (desarrollo completo en la
sección 2.2.1)—. Kuris se posiciona en ese hueco intermedio: modernidad
técnica, especialización en clínicas pequeñas argentinas y diferenciación
funcional concreta sobre todas las capas.

El modelo de ingresos es suscripción mensual en dólares (rango tentativo
de USD 35 a USD 50 por clínica/mes en plan base, planes superiores para
clínicas con más de tres profesionales), sin contrato de permanencia, con
acceso íntegramente web. El punto de equilibrio operativo estimado se
alcanza entre 12 y 18 clínicas suscriptas, sobre la base de costos fijos
operativos proyectados entre USD 85 y USD 145 mensuales (detalle en
sección 5.2 y tabla de costos operativos). Este umbral es alcanzable en
la fase inicial sin necesidad de financiamiento externo.

La empresa es unipersonal: Julián Decoppet, estudiante avanzado de
Ingeniería en Sistemas en UAI Rosario, es el único desarrollador y
fundador. Los logros actuales incluyen infraestructura de producción en
funcionamiento, esquema de base de datos multitenant versionado, frontend
de autenticación multiclínica navegable y documentación de ingeniería
completa (15 ADRs, especificaciones SDD y casos de uso ICONIX), con el
producto en desarrollo activo bajo metodología ICONIX.

La rentabilidad proyectada se sustenta en un modelo de costos fijos
controlados y margen bruto alto, característico del modelo SaaS [@bessemer2024]. Los costos operativos fijos proyectados en la
etapa inicial (escenario base) ascienden a entre USD 85 y USD 145
mensuales, discriminados en: infraestructura VPS (USD 20–30), API de
Claude/Anthropic para módulos LLM (USD 15–40 según volumen de consultas),
servicio de correo transaccional (USD 10–15), dominio y certificados
(USD 2 prorrateado), y reserva de contingencia operativa (USD 38–60).
A partir de 18 clínicas suscriptas en plan base (USD 35–50/mes), el
ingreso mensual oscila entre USD 630 y USD 900, lo que genera un margen
bruto sobre costos operativos de entre el 77 % y el 84 %. Este rango es
consistente con los benchmarks del sector SaaS B2B de salud, donde el
margen bruto maduro oscila entre el 70 % y el 85 % [@bessemer2024]. La
reinversión de los primeros ingresos se orientará al
desarrollo de funcionalidades del roadmap post-MVP y a la reducción del
costo de onboarding por cliente.

Propuesta asociativa: en la etapa actual, el proyecto no cuenta con
socios ni inversores formales. La estrategia asociativa contempla, en
orden de prioridad, (a) alianzas de difusión con colegios médicos y
asociaciones profesionales de Rosario como canal de validación sectorial,
(b) eventuales acuerdos de co-distribución con proveedores de software
complementario (laboratorios, farmacias) una vez superada la fase piloto,
y (c) apertura a inversión ángel o aceleración a partir de la
demostración del product-market fit con datos reales de impacto sobre el
ausentismo. Todos estos vínculos son hipótesis a desarrollar en la fase
comercial; ninguno constituye un acuerdo vigente.

El principal riesgo es la lentitud en la adopción inicial frente a una
base instalada significativa de incumbentes locales, mitigable mediante
una estrategia de acceso piloto gratuito para las primeras clínicas y
soporte personalizado del fundador como diferencial frente a proveedores
masivos. Las barreras de entrada construidas — complejidad del estándar
HL7 FHIR R4 implementado nativamente, aislamiento multitenant desde la
arquitectura, y el conjunto de modelos predictivos integrados al flujo
operativo — hacen que replicar el producto requiera tiempo y conocimiento
especializado en interoperabilidad sanitaria y aprendizaje automático
aplicado a salud.


\newpage

# 1. Descripción general


## 1.1 Descripción básica del negocio

#### Idea de negocio

Kuris es una plataforma de software como servicio (SaaS) de gestión
clínica integral, diseñada específicamente para clínicas médicas privadas de
entre 1 y 5 profesionales en Argentina. El sistema centraliza la operación
clínica — agenda de turnos, gestión de pacientes, historial clínico y
administración de profesionales — bajo un estándar internacional de
interoperabilidad médica (FHIR R4), incorporando predicción de ausentismo de
pacientes mediante reglas heurísticas diseñadas para evolucionar a Machine
Learning, con una hoja de ruta que incluye la sugerencia automática de
cobertura de profesionales por franja horaria (post-MVP).

#### Justificación

El sector salud privado en Argentina presenta una oportunidad estructural
desatendida: existen aproximadamente 5.000 establecimientos de salud privados
registrados [@refes2023], mayoritariamente consultorios y centros médicos de
pequeña escala, que operan con herramientas de gestión inadecuadas — planillas
Excel, agendas en papel o sistemas legacy que no se integran entre sí. La tasa
de ausentismo en turnos médicos ronda el 30 % en un análisis regional sobre 1,5
millones de turnos de Argentina, Uruguay, Chile, Perú y México [@geblix2024];
no se dispone de una desagregación pública específica para Argentina, por lo
que esta cifra se toma como referencia regional, no como dato nacional exacto.
El problema representa pérdidas directas de ingresos para los profesionales y
es crónico en el segmento de clínicas pequeñas, que no cuenta con una solución
tecnológica accesible ampliamente adoptada.

El mercado de software médico en Latinoamérica se encuentra en plena expansión:
el sector de salud digital regional está valuado en USD 5.755 millones en 2024 y
crece a una tasa compuesta anual (CAGR) de 9,5 % proyectada hasta 2034, con el
segmento de software representando el 52,1 % del valor total (Informes de
Expertos, 2024); esta cifra proviene de un informe de mercado comercial sin
metodología pública auditable y se presenta como referencia de tendencia
sectorial, no como dato verificado de manera independiente. Los sistemas de
gestión clínica disponibles en el mercado local cobran entre USD 10 y USD 25
mensuales [@medesk2024; @dricloud2024]; de los competidores relevados en este
rango de precio, ninguno combina predicción de ausentismo accionable por
paciente con FHIR R4 para clínicas de este tamaño en Argentina —aunque
@geblix2024, con presencia regional, ya ofrece predicción de ausentismo por
machine learning sin FHIR ni foco específico en el segmento argentino de 1 a 5
profesionales.
El posicionamiento de precio de Kuris por encima de ese rango
(USD 35–50) se sustenta en el retorno económico medible para el cliente.
Considerando una clínica tipo con 100 turnos mensuales, una tasa de
ausentismo del 25 % [@geblix2024] y un honorario promedio de consulta de
$15.000 pesos argentinos (aproximadamente USD 12 al tipo de cambio de
junio 2026), los turnos perdidos representan una pérdida mensual de
$375.000 pesos (≈ USD 300). Una reducción del ausentismo de tan solo el
10 % — umbral conservador para sistemas de recordatorios activos según
@gomes2022 — recupera $37.500 pesos (≈ USD 30) mensuales
adicionales: un retorno que supera el costo de la suscripción en plan
base. El reporte mensual de ausentismo incluido en el producto hace ese
retorno verificable por la clínica mes a mes.

#### Tipo de e-business

Kuris es un negocio de tipo B2B (Business-to-Business): el cliente directo
es la clínica o consultorio médico, no el paciente individual. El modelo de
distribución es exclusivamente digital, sin presencia física, lo que lo clasifica
como un pure player dentro del e-business.

En una segunda etapa (fuera del alcance del presente seminario), la plataforma
incorporará un componente B2C a través de un marketplace de turnos online donde
los pacientes podrán buscar profesionales disponibles, ver slots en tiempo real y
reservar sin intervención de la secretaría, configurando un modelo mixto B2B +
B2C bajo una misma plataforma.

#### Modalidad de e-commerce

La modalidad de comercialización es SaaS por suscripción mensual en dólares
estadounidenses (USD), con planes escalonados según la cantidad de profesionales
y funcionalidades habilitadas. La suscripción es recurrente, procesada de forma
digital, sin contrato de permanencia mínima. El acceso al sistema se realiza
íntegramente vía navegador web, sin instalación local requerida.


## 1.2 Situación actual del negocio

Kuris se encuentra en etapa de desarrollo inicial (pre-revenue). Al
momento de la presente entrega, el proyecto no cuenta con clientes activos ni
ingresos. El emprendimiento nació como proyecto de tesis de la carrera de
Ingeniería en Sistemas de la Universidad Abierta Interamericana (UAI), con la
intención de construir simultáneamente un producto tecnológico real y un caso de
estudio académico riguroso.

Avance técnico al 08/06/2026

El desarrollo comenzó en abril de 2026. A la fecha se encuentran completados:

  - Infraestructura base: entorno Docker Compose con Nginx, PostgreSQL 16 y
    Redis 8, configurado y desplegado en servidor VPS propio.

  - Autenticación multiclínica (diseño completo, implementación iniciada):
    flujo de dos pasos con separación de identidad y membresía — un usuario
    puede pertenecer a varias clínicas con roles distintos — especificado
    bajo SDD (ADR-014) con el frontend de login y selección de clínica ya
    navegable; la implementación del backend es la siguiente tarea del
    cronograma.

  - Arquitectura definida: diseño de schema multitenant con aislamiento por
    tenant_id, módulos planificados de pacientes, profesionales, turnos e
    historial clínico, stack tecnológico seleccionado (Java 21, Spring Boot 3,
    Spring Security, Spring Data JPA, Angular 18, Angular Material) y
    metodología de desarrollo acordada (ICONIX + SDD + TDD + Clean Architecture).

  - Diseño de la base de datos multitenant: especificación de la arquitectura
    de datos con aislamiento por tenant_id, tablas FHIR en JSONB y esquema de
    migraciones con Flyway (diez migraciones aplicadas). Quince decisiones
    arquitectónicas documentadas como Architecture Decision Records
    (ADR-001 a ADR-015).

  - Frontend inicial Angular 18: workspace con Angular Material, flujo de
    autenticación de dos pasos navegable (presentación, login, selección de
    clínica y layout principal con cambio de clínica activa).

  - Documentación ICONIX de los cuatro casos de uso core completa: diagrama
    de casos de uso, especificaciones, modelo de dominio, diagramas de
    robustez, secuencia y clases, y documentación de patrones de diseño.

Alcance del MVP del seminario (mayo a diciembre 2026)

El alcance comprometido para la presentación final del seminario es un MVP
(Minimum Viable Product) demostrable, no un producto comercial completo. La
diferenciación entre "alcance del MVP" y "roadmap post-MVP" es deliberada:
permite priorizar profundidad sobre amplitud, validar la propuesta con clientes
piloto y mantener rigor metodológico sobre cada componente entregado.

Funcionalidades comprometidas para el MVP:

  - Backend FHIR R4 con recursos críticos (Patient, Practitioner, Appointment,
    Encounter, Observation, Coverage) sobre arquitectura multitenant.
  - Módulo de obra social con validación de tope semanal.
  - Frontend funcional: autenticación, agenda interactiva, ficha de paciente con
    historia clínica, reporte mensual de ausentismo.
  - Predicción explicable de ausentismo basada en reglas heurísticas (historial
    de no-show del paciente, anticipación del turno, día y franja horaria), con
    factores interpretables por el profesional.
  - Recordatorios inteligentes diferenciados por riesgo, vía Telegram y email
    en el MVP (WhatsApp Business API y SMS en fase comercial — la abstracción
    de canal permite incorporarlos sin modificar la lógica de negocio),
    priorizando pacientes con mayor probabilidad de ausentismo.
  - Overbooking inteligente: sugerencia automática de sobreturno o doble agenda
    para slots con alto riesgo de ausentismo, con tope parametrizable por el
    profesional.

La documentación ICONIX del seminario se concentra en cuatro casos de uso core
no triviales:

  CU-01 — Reservar turno con validación integral (slot disponible, tope semanal
          de cobertura, cálculo de riesgo, decisión de overbooking).
  CU-02 — Gestionar consulta médica (Encounter + Observations, estados,
          validaciones clínicas).
  CU-03 — Calcular y notificar predicción de ausentismo (trigger automático,
          scoring, selección de canal, envío).
  CU-04 — Sugerir y aplicar overbooking (detección de slots de riesgo,
          configuración por profesional, ajuste de capacidad).

Funcionalidades fuera del alcance del seminario (roadmap post-MVP):

  - Modelos de Machine Learning entrenados por tenant (Random Forest sobre datos
    reales), que requieren volumen acumulado de turnos inexistente en fase inicial.
  - Sugerencia automática de cobertura óptima por franja horaria.
  - Integraciones específicas con obras sociales (validación en línea contra
    padrones).
  - Marketplace B2C de turnos para pacientes finales.
  - Importador masivo desde Excel (en MVP solo carga manual).

#### Recursos actuales

El proyecto es desarrollado en su totalidad por el autor, contando con el
asesoramiento de los docentes del Seminario de Trabajo Final. La infraestructura
opera sobre un VPS con 4 GB de RAM de costo mensual accesible, lo que mantiene
los costos operativos en un mínimo durante la etapa de desarrollo. La asistencia
de herramientas de inteligencia artificial generativa en el ciclo de desarrollo
(generación de código boilerplate, refactorización, revisión de tests, redacción
técnica) es parte explícita de la metodología, no un atajo: las decisiones
arquitectónicas, los modelos de dominio y la validación funcional permanecen bajo
criterio del autor.

#### Validación preliminar de mercado

El avance técnico es condición necesaria pero no suficiente para la viabilidad
del negocio. En la etapa actual, la validación con clientes reales es incipiente
y no constituye todavía un proceso de prospección formal: existen conversaciones
exploratorias con conocidos del fundador en el ecosistema médico de Rosario,
sin compromiso ni fecha acordada, que podrían eventualmente convertirse en
candidatos a piloto. El onboarding de un primer piloto en agosto de 2026 (ver
sección 5.2) es un objetivo del cronograma, no un hecho en curso ni un acuerdo
vigente; si no se concreta en ese plazo, el plan de contingencia del seminario
contempla una validación simulada como alternativa (ver Definition of Ready del
proyecto). La principal fuente de validación de reglas de negocio del dominio
clínico (semántica de SOAP, criterios de codificación CIE-10, lógica de tope
semanal de obra social) es, a la fecha, una persona cercana al fundador
próxima a recibirse de médica — una consulta de dominio experto, no un piloto
comercial ni una validación de mercado. Las hipótesis centrales del modelo —
que el ausentismo es un dolor suficientemente costoso como para motivar el
cambio de herramientas y que el rango de precio USD 35–50 es absorbible dentro
de los costos operativos del consultorio — permanecen sin validar con datos
primarios y solo podrán contrastarse si se concreta un piloto real. El cálculo
de retorno económico presentado en la sección 1.1 es, por ahora, una proyección
basada exclusivamente en fuentes secundarias [@geblix2024; @gomes2022].

Estado de validación por componente:

| Componente | Estado |
|---|---|
| MVP técnico (backend FHIR, frontend, motor de predicción) | En desarrollo |
| Validación de dominio clínico (SOAP, CIE-10, tope semanal) | Parcial — consulta a una persona próxima a recibirse de médica, no piloto |
| Validación comercial (adopción, disposición a pagar) | Pendiente — sin clínica piloto comprometida |
| Validación económica (ROI, punto de equilibrio) | Simulada — proyección sobre fuentes secundarias, sin datos primarios |

Esta tabla resume el estado real del proyecto a la fecha y debe leerse junto con
cualquier cifra de impacto o retorno presentada en este documento: ninguna de
ellas constituye, todavía, evidencia validada con datos primarios.

#### Posición competitiva inicial

Los sistemas de gestión clínica disponibles en Argentina para el segmento — desde
Grupo Cormos (DrApp, Meducar, Docturno) en la capa masiva, hasta incumbentes
locales como Macena/Geclisa, TecnoMedicus, ConsultSmart y Zindec en Rosario —
ofrecen funcionalidades de agenda, historia clínica y recordatorios automáticos.
Algunos competidores (Nubimed, SFS) comenzaron a incorporar IA para tareas
administrativas en 2026, y Geblix —con presencia en Argentina y la región—
ya ofrece predicción de ausentismo por machine learning como producto
comercial, aunque sin FHIR R4 ni foco específico en clínicas de 1 a 5
profesionales en Argentina. A nivel de infraestructura abierta e
internacional, existen además implementaciones FHIR R4 nativas y
documentadas (Medplum, Elation Health, HAPI FHIR), desarrolladas en la
sección 2.2. En el relevamiento realizado sobre el mercado argentino del
segmento de clínicas pequeñas no se identificó, sin embargo, ningún
competidor que combine el estándar HL7 FHIR R4 con predicción explicable de
ausentismo integrada al flujo operativo, en el rango de precio propuesto.
Kuris identifica esa brecha específica como su principal hipótesis de
diferenciación competitiva en la fase inicial, a confirmar con el avance
de la adopción.


## 1.3 ¿Qué hace único a su negocio?

La ventaja competitiva de Kuris no reside en una funcionalidad aislada,
sino en la combinación —no replicada por la oferta relevada en el segmento de
clínicas pequeñas— de tres pilares integrados al mismo flujo operativo:

- Interoperabilidad nativa sobre HL7 FHIR R4. El sistema no almacena los datos
  clínicos en un esquema propietario al que luego se le agrega una capa de
  exportación, sino que adopta el estándar internacional FHIR R4 como modelo de
  dominio desde la base de la arquitectura. Esto favorece que la información de
  pacientes, turnos, encuentros y cobertura sea portable y compatible con sistemas
  externos —obras sociales, laboratorios, prestadores— tanto en el presente como
  a futuro. La complejidad de implementar correctamente el estándar constituye,
  además, una barrera de entrada frente a competidores que partieron de esquemas
  cerrados. El propio Estado argentino avanza en esta dirección: la Estrategia
  Nacional de Salud Digital 2025–2030, impulsada con apoyo de la OPS y el BID,
  consolidó HL7 FHIR como estándar de interoperabilidad federal [@ops2026].

- Predicción explicable del ausentismo por paciente. El sistema estima la
  probabilidad de que un paciente no se presente a su turno y expone, en lenguaje
  comprensible para el profesional, los factores que generan ese riesgo (historial
  de inasistencias, anticipación de la reserva, día y franja horaria). La
  condición de explicabilidad es deliberada: a diferencia de un modelo de caja
  negra, el profesional comprende por qué un turno se marca como riesgoso y
  conserva el criterio sobre la acción a tomar.

- Inteligencia operacional accionable sobre la predicción. El riesgo calculado no
  es un dato pasivo: alimenta recordatorios diferenciados por canal según la
  probabilidad de ausentismo y habilita la sugerencia de overbooking inteligente
  sobre los horarios de mayor riesgo, con un tope parametrizable por el
  profesional. La predicción se traduce directamente en recuperación de ingresos y
  en eficiencia de la agenda.

- Asistencia clínica mediante IA generativa. A diferencia de los competidores que
  incorporaron IA para tareas exclusivamente administrativas (transcripción de
  llamados, asistentes telefónicos), Kuris integra IA generativa en el
  flujo clínico: resumen automático de la historia clínica al cierre de cada
  consulta y sugerencia de clasificación CIE-10 a partir del texto libre del
  motivo de consulta. Ambas capacidades están implementadas mediante la API de
  Claude (Anthropic), con el profesional conservando siempre el criterio final
  sobre la información registrada. Esta integración representa una diferenciación
  concreta frente a la oferta del segmento: no IA como decoración, sino IA
  reduciendo la carga administrativa del médico en cada acto clínico.

En el relevamiento competitivo realizado (mayo de 2026) no se identificó ningún
proveedor del segmento de clínicas pequeñas en Argentina que combine estos
cuatro pilares de manera nativa e integrada. Tomados de manera aislada, ninguno
de los cuatro pilares es exclusivo de Kuris: existe software con FHIR R4 nativo
(Medplum, Elation Health, HAPI FHIR) y existe predicción de ausentismo por
machine learning en producción comercial, incluso en la región [@geblix2024]. Los
incumbentes locales ofrecen gestión administrativa sólida sin interoperabilidad
estándar ni capacidades predictivas; los SaaS modernos genéricos incorporaron
recientemente IA para tareas administrativas (transcripción, asistentes
telefónicos) pero no predicción de ausentismo accionable por paciente ni
asistencia en el flujo clínico; las soluciones de consolidación masiva
priorizan el volumen y la estandarización por sobre la personalización del
flujo por clínica; y Geblix, el competidor regional más cercano en
inteligencia operacional, no implementa FHIR R4 ni se especializa en el marco
regulatorio argentino. La combinación específica de los cuatro pilares en el
nicho de clínicas pequeñas argentinas —no la existencia aislada de cada uno— es
lo que constituye la diferenciación de Kuris.


## 1.4 Factores principales que se considera harán exitoso al proyecto

- Diferenciación funcional concreta y medible. El producto ataca un dolor
  cuantificado del segmento —el ausentismo, que afecta a aproximadamente el
  30 % de los turnos médicos a nivel regional [@geblix2024]— con una
  capacidad que la competencia directa en el segmento argentino no ofrece, y
  cuyo impacto económico es demostrable mes a mes mediante el reporte de
  ausentismo.

- Especialización en el segmento desatendido. El foco explícito en clínicas de
  uno a cinco profesionales evita competir de frente con las soluciones
  hospitalarias sobredimensionadas y con los productos masivos orientados a
  volumen, posicionando al producto en un nicho con necesidades específicas y
  baja cobertura tecnológica adecuada.

- Estructura de costos liviana y bajo punto de equilibrio. Con costos fijos
  operativos proyectados entre USD 85 y USD 145 mensuales y un punto de
  equilibrio operativo estimado entre 12 y 18 clínicas suscriptas, el proyecto
  es viable en su fase
  inicial sin financiamiento externo, lo que reduce el riesgo financiero y otorga
  tiempo para validar la adopción.

- Soporte personalizado del fundador como diferencial relacional. En un mercado
  donde el soporte de los proveedores masivos tiende a ser impersonal o
  tercerizado, el acompañamiento directo en el onboarding y en la operación
  inicial constituye una ventaja relacional concreta para reducir la fricción de
  adopción.

- Conocimiento técnico especializado del fundador. La combinación de competencias
  en arquitectura SaaS, implementación de FHIR y modelos predictivos aplicados a
  salud es poco frecuente y difícil de replicar en el corto plazo, lo que protege
  la diferenciación técnica del producto.

- Calidad y experiencia de uso. La plataforma está diseñada para usuarios no
  técnicos (médicos, personal de secretaría) con un frontend basado en Angular
  Material que prioriza la claridad de la interfaz y la velocidad de operación en
  la jornada diaria. La explicabilidad del riesgo de ausentismo —expuesto en
  lenguaje comprensible, no como score opaco— es en sí misma un atributo de
  calidad percibida que diferencia la experiencia de uso frente a competidores que
  muestran indicadores sin contexto. El acceso web sin instalación local, el
  onboarding asistido y el precio en el rango accesible (USD 35–50) completan una
  propuesta donde la calidad del servicio no está reñida con la accesibilidad.


## 1.5 Estrategia: Misión, Visión y Propósito Estratégico

Misión. Brindar a las clínicas y consultorios médicos pequeños de Argentina una
plataforma de gestión clínica accesible, interoperable e inteligente que ordene
su operación diaria y reduzca la pérdida de ingresos por ausentismo, sobre
estándares internacionales que garanticen la portabilidad y la seguridad de los
datos de sus pacientes.

Visión. Convertirse en la plataforma de referencia para la gestión clínica de
pequeñas y medianas clínicas en Argentina y la región, reconocida por combinar
interoperabilidad estándar e inteligencia operacional accesible, y por habilitar
progresivamente un ecosistema interconectado entre profesionales, pacientes y
prestadores.

Propósito estratégico. Demostrar que la interoperabilidad sanitaria basada en
estándares abiertos y la inteligencia operacional explicable —históricamente
reservadas a sistemas hospitalarios de gran porte— pueden ofrecerse de manera
accesible al segmento de clínicas pequeñas, elevando el estándar tecnológico de
un mercado estructuralmente desatendido.


## 1.6 Identificación de la oportunidad de negocio

La oportunidad surge de la convergencia de cuatro condiciones del contexto:

1. Un problema operativo costoso y crónico: el ausentismo de aproximadamente el
   30 % de los turnos a nivel regional [@geblix2024], que se traduce en
   pérdidas directas de ingresos para los profesionales y para el cual el
   segmento de clínicas pequeñas en Argentina no dispone de una solución
   específica ampliamente adoptada.

2. Un mercado en expansión sostenida: el sector de salud digital en América
   Latina, valuado en USD 5.755 millones en 2024 y con una tasa de crecimiento
   anual compuesta del 9,5 % proyectada hasta 2034, donde el software representa
   el 52,1 % del valor total [@informesdeexpertos2024].

3. Una base instalada amplia y mal servida: aproximadamente 5.000
   establecimientos de salud privados registrados en Argentina [@refes2023],
   mayoritariamente de pequeña escala, que operan con planillas, agendas en papel
   o sistemas heredados sin integración.

4. Una oferta existente sin diferenciación tecnológica en el segmento
   específico: si bien existen soluciones internacionales y de infraestructura
   abierta con FHIR R4 nativo (Medplum, Elation Health, HAPI FHIR) y un
   competidor regional con predicción de ausentismo por machine learning
   [@geblix2024], ninguno de los competidores relevados combina FHIR R4,
   predicción explicable de ausentismo e inteligencia operacional integrada, en
   un rango de precio accesible, para el segmento de clínicas de 1 a 5
   profesionales en Argentina.

La conjunción de un dolor cuantificado, un mercado en crecimiento, una demanda
latente amplia y una oferta sin diferenciación define un espacio de oportunidad
concreto y defendible.


## 1.7 Capacidades centrales

Las capacidades centrales del emprendimiento —entendidas como las competencias
distintivas y difíciles de imitar sobre las que se construye la ventaja— son:

- Implementación nativa del estándar HL7 FHIR R4, que requiere conocimiento
  especializado en interoperabilidad sanitaria y constituye una barrera técnica
  de entrada.

- Diseño de arquitectura multitenant con aislamiento de datos por clínica desde
  la base, condición no negociable para operar un SaaS en salud y ya
  implementada en el prototipo.

- Desarrollo de modelos predictivos explicables aplicados al dominio clínico, con
  una trayectoria diseñada que evoluciona desde heurísticas interpretables hacia
  modelos entrenados por tenant a medida que cada clínica acumula volumen de
  datos.

- Capacidad de ejecución técnica integral del fundador, que cubre el ciclo
  completo —backend, frontend, infraestructura y modelos— y permite iterar el
  producto con costo estructural mínimo en la fase inicial.


## 1.8 Propuesta de valor para el cliente

Para una clínica o consultorio pequeño, Kuris ofrece:

- Orden operativo integral: agenda de turnos, gestión de pacientes, historia
  clínica electrónica y administración de profesionales en una única plataforma
  web, sin instalación local.

- Reducción de la pérdida por ausentismo: anticipación del riesgo por turno,
  recordatorios priorizados según ese riesgo y sugerencia de overbooking para
  recuperar capacidad, con impacto económico medible.

- Datos portables y a prueba de futuro: al estar construido sobre FHIR R4, la
  información clínica es compatible con sistemas externos y no queda cautiva de
  un formato propietario.

- Acceso sin fricción: suscripción mensual sin contrato de permanencia, período
  de prueba gratuito en el lanzamiento y soporte directo del fundador durante el
  onboarding.

La promesa central, en una frase: ordenar la operación de la clínica y recuperar
los ingresos que el ausentismo se lleva, sobre una base de datos clínicos segura,
portable e interoperable.


## 1.9 Valores nucleares de la organización

- Interoperabilidad por principio: adopción de estándares abiertos como base, no
  como agregado posterior.
- Explicabilidad y criterio profesional: la inteligencia del sistema asiste y
  explica; la decisión clínica permanece en el profesional.
- Privacidad y seguridad de los datos de salud como condición no negociable,
  alineada con la normativa argentina vigente.
- Validación incremental: preferir pocas funcionalidades operando correctamente
  antes que muchas implementadas de manera superficial.
- Cercanía con el cliente: soporte directo y acompañamiento como parte del
  producto, no como costo a minimizar.


## 1.10 Enfoque e iniciativas estratégicas

El enfoque estratégico es de diferenciación focalizada (en términos de Porter,
1980): atender un segmento específico —clínicas pequeñas— con una propuesta de
valor que la competencia generalista no ofrece, evitando la competencia frontal
por precio con los productos masivos.

Las iniciativas estratégicas que materializan ese enfoque son:

1. Construir y defender la diferenciación técnica (FHIR R4 + predicción
   explicable + inteligencia operacional) como núcleo del producto.
2. Validar la adopción con un piloto real en Rosario antes de escalar,
   capturando métricas de impacto verificables.
3. Crecer por referidos dentro del ecosistema médico local, aprovechando las
   cadenas de derivación profesional como canal orgánico.
4. Mantener una estructura de costos liviana que permita alcanzar el punto de
   equilibrio sin financiamiento externo.
5. Preservar una hoja de ruta de extensión (ML por tenant, cobertura óptima,
   marketplace B2C) diseñada como evolución natural del MVP y no como
   reescritura.


## 1.11 Áreas clave de resultados

Las áreas clave de resultados (ACR) sobre las que se medirá el éxito del
emprendimiento, con su justificación:

| Área clave | Por qué es clave | Indicador |
|---|---|---|
| Adopción | Determina la viabilidad comercial y el alcance del punto de equilibrio | N.º de clínicas suscriptas/activas |
| Impacto en ausentismo | Es el diferencial cuantificable del producto frente a la competencia | Reducción de la tasa de ausentismo en clínicas usuarias |
| Retención | En un SaaS sin permanencia, la recurrencia sostiene los ingresos | Tasa de renovación mensual / churn |
| Eficiencia operativa | El costo variable de onboarding y soporte condiciona la escalabilidad | Costo de onboarding por cliente; tiempo de soporte/cliente |
| Cumplimiento normativo y confianza | La interoperabilidad, la seguridad y la normativa son condición de habilitación comercial en salud; ningún cliente adoptará un sistema que no sea confiable y legal | Conformidad FHIR R4; cumplimiento Ley 27.706 y Ley 25.326; ausencia de incidentes de privacidad de datos |


## 1.12 Ingreso al sector: estrategias de inserción

Estrategia genérica [@porter1980]: diferenciación enfocada. El proyecto no
compite por costo —los incumbentes masivos tienen ventaja de escala— sino por una
propuesta de valor diferenciada dirigida a un segmento acotado, la clínica
pequeña, con necesidades que la oferta generalista no resuelve.

Estrategia frente a la competencia [@kotler2017]: retador / especialista
de nicho. Como entrante sin base instalada, Kuris no disputa frontalmente
la cuota de los líderes consolidados; se inserta como especialista de nicho que
atiende mejor que nadie un segmento específico, construyendo posición desde la
especialización antes de ampliar el alcance geográfico (Santa Fe, interior) y
funcional (roadmap post-MVP).

Mecanismos concretos de inserción:

- Acceso piloto sin costo para las primeras clínicas, reduciendo la barrera de
  adopción y generando casos de referencia.
- Soporte personalizado del fundador como diferencial relacional frente a los
  proveedores masivos.
- Penetración por boca a boca dentro de las redes de derivación profesional de
  Rosario.
- Alianzas potenciales con colegios médicos y asociaciones profesionales como
  vehículo de difusión y validación sectorial (hipótesis a validar
  comercialmente, no acuerdos vigentes).


\newpage

# 2. Análisis estratégico


## 2.1 Análisis de contexto

2.1.1 Descripción del escenario local

El lanzamiento se sitúa en Rosario y su zona de influencia (provincia de Santa
Fe), un polo con alta densidad de profesionales de la salud y presencia de
proveedores de software médico locales con trayectoria. La elección responde a
la cercanía del fundador con el ecosistema médico local —que habilita el canal de
referidos y el acompañamiento presencial del piloto— y a la posibilidad de
validar la propuesta en un mercado abarcable antes de proyectar la expansión
hacia otras ciudades del interior.

2.1.2 Factores económicos

El contexto macroeconómico argentino presenta históricamente alta inflación,
volatilidad del tipo de cambio y restricciones cambiarias, lo que afecta la
previsibilidad de costos e ingresos de cualquier emprendimiento. Kuris
mitiga parte de esta exposición fijando la suscripción en dólares
estadounidenses, lo que estabiliza el ingreso frente a la depreciación de la
moneda local; al mismo tiempo, el precio en dólares puede percibirse como elevado
por clínicas pequeñas con ingresos en pesos, tensión que el rango de precio
accesible (USD 35–50) busca contener. La estructura de costos liviana reduce la
sensibilidad del proyecto a las fluctuaciones macro durante la fase inicial.

2.1.3 Factores políticos

El marco regulatorio relevante incluye la Ley 27.706 (Historia Clínica
Electrónica) y la Ley 25.326 (Protección de Datos Personales), que establecen
obligaciones sobre el tratamiento y la custodia de datos sanitarios. Lejos de ser
solo una restricción, el cumplimiento normativo opera como factor habilitante: la
arquitectura interoperable y el aislamiento de datos por clínica posicionan
favorablemente al producto frente a estos requisitos. La existencia de
iniciativas públicas de información sanitaria (SISA, REFES) y la adopción
creciente de estándares de interoperabilidad configuran un entorno institucional
alineado con la propuesta [@argentinagobar2026].

2.1.4 Factores tecnológicos

La maduración del estándar HL7 FHIR R4 y del ecosistema SMART on FHIR habilita
técnicamente la propuesta de interoperabilidad, y cuenta con respaldo
institucional en Argentina: el Ministerio de Salud de la Nación publica guías
de implementación y estándares de interoperabilidad —incluyendo HL7 FHIR y
SNOMED CT— a través de su Red de Salud Digital [@argentinagobarsf]. Esto
implica que el ecosistema técnico de FHIR en el país ya tiene infraestructura
estatal activa, lo que reduce el riesgo de adopción del estándar pero también
matiza la narrativa de que se trata de una tecnología no explorada en el
mercado local. La disponibilidad de infraestructura cloud y de servidores
virtuales de bajo costo permite operar con una estructura liviana. La
incorporación reciente de inteligencia artificial por parte de algunos
competidores —enfocada en tareas administrativas, y en el caso de Geblix ya
orientada a predicción de ausentismo— confirma que el sector se mueve hacia la
automatización inteligente, validando la dirección estratégica del proyecto;
la diferenciación de Kuris sobre ese eje depende de la combinación con FHIR
R4 nativo y el foco en el segmento de clínicas pequeñas argentinas, no de la
novedad de la predicción de ausentismo en sí, que ya es una categoría de
producto madura a nivel internacional (eClinicalWorks/healow, ClosedLoop,
Arkangel AI).

El proyecto presenta dependencias tecnológicas externas que configuran riesgos
operativos a gestionar:

- Dependencia de la API de Claude (Anthropic): los módulos de resumen de historia
  clínica y sugerencia CIE-10 del MVP dependen de esta API. Un cambio de política
  de precios, una restricción de acceso en Argentina o una interrupción del
  servicio afectaría directamente estas funcionalidades. Mitigación: la
  arquitectura separa el módulo LLM mediante una interfaz intercambiable (puerto
  de salida en Clean Architecture), lo que permite sustituir el proveedor por
  alternativas compatibles (OpenAI, Gemini, modelos open source vía Ollama) sin
  reescribir la lógica de negocio.

- Dependencia de Telegram Bot API: el canal de recordatorios del MVP opera sobre
  Telegram, que es gratuito y no requiere aprobación. Un bloqueo regulatorio o
  cambio de política de Telegram en Argentina eliminaría este canal. Mitigación:
  la abstracción de canal de notificaciones (ADR-013) permite activar email (ya
  implementado como canal secundario) o WhatsApp Business API sin modificar la
  lógica de recordatorios.

Ambas dependencias están gestionadas arquitectónicamente desde el diseño, lo que
reduce el riesgo operativo aunque no lo elimina.

2.1.5 Descripción del escenario: escenario-meta

El escenario-meta es un mercado de clínicas pequeñas en Rosario y Santa Fe que,
en un horizonte de mediano plazo, reconoce la interoperabilidad estándar y la
inteligencia operacional explicable como atributos esperables —no
excepcionales— de un sistema de gestión clínica. En ese escenario, Kuris
opera con una base de clientes que supera el punto de equilibrio, casos de
referencia con impacto medible sobre el ausentismo y un canal de crecimiento
orgánico por referidos profesionales.

Los factores de contexto actúan sobre el negocio del siguiente modo: los
económicos condicionan la sensibilidad al precio y se mitigan con la estructura
liviana y la fijación en dólares; los políticos operan como habilitadores si el
producto cumple la normativa de datos de salud; los tecnológicos habilitan y
validan la propuesta diferencial.

2.1.6 Análisis sectorial: oportunidades y amenazas

Oportunidades:

- Segmento amplio y desatendido (≈ 5.000 establecimientos privados,
  mayoritariamente pequeños).
- Problema cuantificado y costoso (ausentismo ≈ 30 % a nivel regional, Geblix
  2024) sin solución específica ampliamente adoptada en el segmento argentino
  de clínicas pequeñas.
- Mercado de salud digital regional en crecimiento sostenido (CAGR 9,5 %).
- Tendencia sectorial hacia la interoperabilidad y la automatización inteligente.
- Marco normativo y estrategia nacional de salud digital que favorecen la
  adopción de FHIR [@ops2026].

Amenazas:

- Base instalada significativa de incumbentes locales con relación establecida
  con sus clientes.
- Posible incorporación de capacidades predictivas por parte de competidores con
  más recursos.
- Sensibilidad al precio en dólares de clínicas con ingresos en pesos.
- Volatilidad macroeconómica que afecta las decisiones de inversión de los
  clientes.
- Inercia y resistencia al cambio en clínicas acostumbradas a herramientas
  heredadas.


## 2.2 Análisis de la competencia

2.2.1 Principales competidores directos

El mercado argentino de software de gestión clínica para el segmento pequeño se
estructura en cuatro capas:

- Capa de consolidación nacional. Grupo Cormos, que en 2024 adquirió Meducar y
  consolidó un portfolio (DrApp, Docturno, iTurnos, Wiri Salud, Receto) que
  gestiona más de 20 millones de turnos anuales y digitaliza aproximadamente
  25.000 profesionales y 6.000 centros [@grupocormos2024]. Orientado a volumen
  y estandarización, no a integraciones específicas ni a flujos personalizados
  por clínica.

- Capa de incumbentes locales. Proveedores con trayectoria y base de clientes
  establecida: Macena/Geclisa (Rosario, más de 30 años y unos 230 clientes en
  producción), TecnoMedicus, ConsultSmart, Medicloud, Zindec (Rosario). Productos
  sólidos en gestión administrativa y facturación, pero sin arquitectura
  interoperable nativa ni capacidades predictivas integradas al flujo operativo.

- Capa de SaaS modernos genéricos. AgendaPro, Gendu, Turnito, Doctoralia,
  Nubimed: orientados a agenda y recordatorios automáticos, con incorporación
  reciente de IA para tareas administrativas (transcripción de consultas y
  asistentes telefónicos en Nubimed; IA clínica supervisada orientada a
  hospitales en SFS).

- Capa de inteligencia operacional especializada. Geblix (Argentina, con
  expansión a Uruguay, Chile, Perú y México): producto comercial con seis
  módulos integrados —agenda online, recordatorios por WhatsApp, historia
  clínica digital, videoconsultas, gestión financiera y operaciones—, que
  incluye predicción de ausentismo por machine learning con reducción
  reportada de hasta el 40 % [@geblix2024]. Es el competidor regional más
  cercano a la propuesta de inteligencia operacional de Kuris, aunque no
  implementa FHIR R4 ni se especializa en el segmento argentino de 1 a 5
  profesionales ni en el marco regulatorio de obras sociales locales.

El análisis competitivo incluye también soluciones internacionales y de
infraestructura abierta con presencia, accesibilidad o relevancia técnica para
el mercado argentino:

- Medesk (Reino Unido / España): plataforma SaaS de gestión clínica con
  historia electrónica, agenda y recordatorios automáticos; presencia activa en
  Argentina con precios en el rango USD 10–25/mes [@medesk2024]. No implementa
  FHIR R4 de forma nativa ni predicción de ausentismo. Opera en español y es el
  competidor internacional más accesible para el segmento.

- Elation Health (Estados Unidos): plataforma EHR para práctica independiente
  con API FHIR R4 certificada bajo el criterio ONC (g)(10) —incluyendo SMART on
  FHIR, US Core Data v2 y Bulk Data Export 2.0— [@elationhealth2024]; es
  decir, una implementación FHIR R4 más completa y certificada que la que Kuris
  puede demostrar en su etapa actual. No está disponible comercialmente en
  Argentina y sus precios (>USD 300/mes por profesional) la excluyen del
  segmento objetivo de Kuris, pero su existencia refuta la idea de que no haya
  EHRs con FHIR R4 nativo en el mercado internacional.

- Kareo / Tebra (Estados Unidos): suite para clínicas pequeñas con módulos de
  facturación, agenda y telemedicina; opera principalmente sobre una API SOAP
  propia y se encuentra en transición hacia FHIR, sin que esta migración esté
  completa a la fecha [@kareotebra2024]; sin predicción de ausentismo; no
  disponible en Argentina.

- Medplum (Estados Unidos, open source): plataforma de desarrollo de software
  de salud FHIR-native, con servidor FHIR R4 sobre PostgreSQL, soporte
  multitenant, control de acceso basado en roles y ACLs por registro, bajo
  licencia Apache 2.0 [@medplum2025]. No es un producto terminado para
  clínicas sino un framework de desarrollo; su existencia muestra que ya hay
  infraestructura FHIR R4 nativa reutilizable y de bajo costo disponible para
  construir EHRs, lo que reduce —aunque no elimina— la barrera de entrada
  técnica que el documento atribuye en exclusiva al esfuerzo de Kuris.

- HAPI FHIR (open source): servidor FHIR de referencia en Java, recomendado
  explícitamente por el Banco Interamericano de Desarrollo (BID) como
  herramienta para instituciones de salud en América Latina y el Caribe
  [@iadbsf]. Confirma que existe ecosistema FHIR
  abierto y de respaldo institucional en la región, no solo en mercados
  desarrollados.

En el relevamiento sobre el segmento específico de clínicas de 1 a 5
profesionales en Argentina —considerando tanto el mercado local como las
opciones internacionales y de infraestructura abierta relevadas— no se
identificó ningún competidor que combine, de manera nativa e integrada al
flujo operativo y en un rango de precio accesible para ese segmento: (a)
implementación del estándar HL7 FHIR R4, (b) predicción explicable del
ausentismo por paciente con factores interpretables, y (c) inteligencia
operacional accionable (recordatorios diferenciados + overbooking). Esta
afirmación es más acotada que sostener una ausencia general de FHIR R4 o de
predicción de ausentismo en el mercado: ambas existen como categorías de
producto maduras a nivel internacional (Medplum, Elation Health, HAPI FHIR;
healow/eClinicalWorks, ClosedLoop, Arkangel AI) y regional [@geblix2024]. La
ventaja diferencial de Kuris reside en la combinación específica de los tres
elementos para el nicho argentino, no en la novedad aislada de ninguno de
ellos; frente a soluciones internacionales de mayor escala y frente a Geblix,
la diferenciación adicional reside en la especialización en el contexto
regulatorio argentino (Ley 27.706, obras sociales) y en el soporte
personalizado del fundador.

2.2.2 Análisis de cadena de valor

| Eslabón | Cómo lo resuelve Kuris | Aporte de valor |
|---|---|---|
| Desarrollo de producto | Ejecución técnica integral del fundador (backend, frontend, infra, modelos) | Iteración rápida con costo estructural mínimo |
| Infraestructura | VPS propio + Docker/PostgreSQL/Redis ya desplegado | Costo marginal por cliente bajo; escalabilidad sin inversión proporcional |
| Adquisición de clientes | Venta directa + referidos en el ecosistema médico de Rosario (hipótesis de canal, sin referidos activos en esta etapa) | Canal proyectado de bajo costo (hipótesis a validar con el piloto) |
| Onboarding | Migración guiada + capacitación + soporte directo del fundador | Reduce fricción de adopción; diferencial relacional |
| Operación / soporte | Soporte personalizado en fase inicial | Retención; costo variable a gestionar al escalar |
| Demostración de valor | Reporte mensual de ausentismo y eficiencia | Justifica la recurrencia con impacto medible |

Las actividades de soporte (infraestructura, conocimiento técnico, cumplimiento
normativo) habilitan las actividades primarias y constituyen las barreras de
imitación más relevantes del modelo.

Comparación con la competencia por eslabón:

| Eslabón | Incumbentes locales | SaaS modernos (AgendaPro, Nubimed) |
|---|---|---|
| Desarrollo de producto | Equipos consolidados con base instalada y deuda técnica | Equipos mayores, ciclos más lentos por generalismo de mercado |
| Infraestructura | On-premise o cloud legacy; costos de migración altos | Cloud moderno; costos más altos por escala y funcionalidades ociosas |
| Adquisición de clientes | Fuerza de ventas directa y red de referidos establecida | Marketing digital + freemium; sin canal de nicho especializado |
| Onboarding | Proceso estandarizado sin acompañamiento diferenciado | Autoservicio; bajo costo pero sin personalización por clínica |
| Operación / soporte | Soporte multicanal pero impersonal a escala | Soporte por ticket; sin contacto directo con el equipo de producto |
| Demostración de valor | Sin reportes de impacto sobre ausentismo | Métricas de agenda; sin análisis predictivo de ausentismo |

La ventaja de Kuris se concentra en los eslabones de onboarding,
demostración de valor y desarrollo de producto, donde la especialización
en el nicho y la capacidad de ejecución técnica integral del fundador
generan un diferencial no replicable por los competidores de mayor escala
sin una reestructura significativa de su modelo operativo.

2.2.3 Factores Críticos de Éxito (FCE)

1. Lograr y sostener la diferenciación técnica (FHIR + predicción + inteligencia
   operacional) integrada al flujo operativo.
2. Validar el impacto sobre el ausentismo con datos reales de al menos un piloto.
3. Mantener un costo de onboarding y soporte controlado para que el modelo
   escale.
4. Construir un canal de referidos confiable dentro del ecosistema médico local.
5. Garantizar seguridad y cumplimiento normativo de los datos de salud.
6. Alcanzar el punto de equilibrio sin financiamiento externo.

2.2.4 Fortalezas y debilidades del negocio

Fortalezas: diferenciación técnica única en el segmento; interoperabilidad nativa
como barrera de entrada; estructura de costos liviana y bajo punto de equilibrio;
conocimiento técnico especializado del fundador; capacidad de soporte cercano.

Debilidades: equipo unipersonal con capacidad de ejecución acotada y riesgo de
cuello de botella; ausencia de base instalada, marca y casos de referencia;
recursos comerciales y de marketing limitados; dependencia crítica de una sola
persona; aún sin validación comercial real.


\newpage

# 3. Análisis FODA


## 3.1 Cuadro FODA

|  | Origen interno | Origen externo |
|---|---|---|
| **Positivo** | **FORTALEZAS**<br>• Diferenciación técnica única (FHIR R4 + predicción explicable + inteligencia operacional)<br>• Interoperabilidad nativa como barrera de entrada<br>• Estructura de costos liviana; punto de equilibrio bajo (12–18 clínicas)<br>• Conocimiento técnico especializado del fundador<br>• Soporte personalizado como diferencial relacional<br>• Infraestructura propia ya operativa | **OPORTUNIDADES**<br>• Segmento amplio y desatendido (≈ 5.000 establecimientos)<br>• Ausentismo cuantificado (≈ 30 % regional) sin solución adoptada en AR<br>• Mercado de salud digital en expansión (CAGR 9,5 %)<br>• Tendencia sectorial a interoperabilidad e IA<br>• Marco normativo y estrategia nacional de salud digital que favorece FHIR |
| **Negativo** | **DEBILIDADES**<br>• Equipo unipersonal; capacidad de ejecución acotada<br>• Sin base instalada, marca ni casos de referencia<br>• Recursos comerciales y de marketing limitados<br>• Dependencia crítica de una sola persona<br>• Sin validación comercial real | **AMENAZAS**<br>• Incumbentes locales con base instalada y relación establecida<br>• Posible incorporación de IA predictiva por competidores con más recursos<br>• Sensibilidad al precio en dólares (ingresos de clientes en pesos)<br>• Volatilidad macroeconómica argentina<br>• Resistencia al cambio en clínicas con herramientas heredadas |


## 3.2 Análisis de Fortalezas, Oportunidades, Debilidades y Amenazas

El cruce de factores orienta las estrategias del proyecto:

Estrategias FO (fortalezas + oportunidades) — ofensivas. Apalancar la
diferenciación técnica única sobre un segmento amplio y desatendido, posicionando
la interoperabilidad y la predicción de ausentismo como respuesta directa a un
dolor cuantificado que la oferta actual no resuelve.

Estrategias FA (fortalezas + amenazas) — defensivas. Frente a la base instalada
de los incumbentes y a la eventual reacción de competidores con más recursos,
usar la barrera técnica (FHIR nativo, especialización) y el soporte cercano para
construir relaciones difíciles de desplazar; fijar el precio en dólares para
protegerse de la volatilidad sin perder accesibilidad.

Estrategias DO (debilidades + oportunidades) — de reorientación. Compensar la
falta de base instalada y de recursos comerciales mediante el piloto gratuito y
los referidos profesionales como canal de bajo costo, convirtiendo casos de
impacto medible en la palanca de credibilidad que sustituye a la marca.

Estrategias DA (debilidades + amenazas) — de supervivencia. Mitigar el riesgo
del equipo unipersonal con una estructura de costos liviana que permita sostener
el proyecto sin presión financiera, y con un alcance de MVP recortado y
priorizado que concentre el esfuerzo en lo diferencial y no negociable
(multitenant y FHIR core).


## 3.3 Conclusión: atractivo de la industria y fortalezas del negocio

La industria del software de gestión clínica para el segmento pequeño es
atractiva: mercado amplio y en crecimiento, problema costoso y cuantificado, y
una oferta existente sin diferenciación tecnológica en el nicho. Las fortalezas
del negocio —diferenciación técnica única, interoperabilidad como barrera de
entrada y estructura de costos liviana— están alineadas con las oportunidades del
sector.

Sin embargo, la viabilidad comercial del proyecto aún no está demostrada: a la
fecha del presente documento no existe ningún cliente activo ni validación
primaria de las hipótesis de precio y adopción. Los dos riesgos que condicionan
la viabilidad real son: (a) que las clínicas objetivo no perciban el dolor del
ausentismo como suficientemente costoso para justificar el cambio de herramientas,
y (b) que el precio en dólares sea una barrera de adopción en un contexto de
ingresos en pesos con alta volatilidad cambiaria. Ambos riesgos son detectables
temprano si se logra concretar un piloto real: el cronograma de agosto a
noviembre de 2026 (sección 5.2) plantea ese piloto como objetivo, pero a la
fecha no hay ninguna clínica comprometida, solo conversaciones exploratorias
sin acuerdo. Si el piloto no se concreta en ese plazo, el plan de contingencia
académico contempla una validación simulada como alternativa. El desafío
central de esta etapa es, por lo tanto, doble: conseguir al menos un piloto
real o ejecutar la alternativa simulada con rigor, y convertir la ventaja
técnica en evidencia —real o simulada— que demuestre valor medible, sin lo
cual la propuesta competitiva permanece enteramente en el terreno de la
hipótesis.


\newpage

# 4. Segmentación


## 4.1 Segmentación de consumidores y/o negocios

Al tratarse de un negocio B2B, la unidad de segmentación es la organización
cliente (la clínica o el consultorio), no el paciente individual. Los criterios
de segmentación aplicados son:

- Ubicación geográfica: Rosario y zona de influencia (Santa Fe) en la fase
  inicial; expansión progresiva al interior del país.
- Tamaño del cliente: clínicas y consultorios de 1 a 5 profesionales.
- Tipo de cliente: establecimientos de salud privados que operan con herramientas
  heterogéneas (planillas, papel, sistemas heredados).
- Uso del producto / dolor predominante: necesidad de orden operativo, de
  interoperabilidad futura y/o de reducción de pérdidas por ausentismo.


## 4.2 Identificación de grupos diferenciados de consumidores

Dentro del segmento se distinguen tres perfiles:

1. Profesional independiente con consultorio propio. Busca digitalizar su
   operación sin invertir en infraestructura ni en sistemas sobredimensionados.
   Sensible al precio y a la simplicidad de uso.

2. Centro médico pequeño (2 a 5 profesionales). Necesita coordinación de agenda,
   historia clínica unificada e interoperabilidad futura con obras sociales.
   Valora la integración y la escalabilidad.

3. Clínica con pérdidas recurrentes por ausentismo. No cuenta con herramientas
   analíticas para anticiparlo; es el perfil donde el diferencial de predicción y
   overbooking tiene mayor impacto percibido y donde el valor económico del
   producto es más fácil de demostrar.

La decisión de mantener el segmento amplio dentro de "clínicas pequeñas", sin
verticalizar por especialidad médica en esta etapa, responde al objetivo de
validar la propuesta en un mercado más amplio antes de especializar.

Perfil sociodemográfico del usuario operativo (factores socioculturales):

El usuario que opera el sistema cotidianamente —tanto el decisor de compra como
el usuario diario— presenta un perfil relativamente homogéneo dentro del
segmento:

  - Edad: entre 30 y 55 años. Los profesionales médicos que gestionan su propio
    consultorio se encuentran mayoritariamente en ese rango etario; el personal
    administrativo es algo más joven (25–45 años).
  - Nivel educacional: universitario completo (médicos) o terciario/universitario
    en curso (personal administrativo). Alta familiaridad con herramientas
    digitales de consumo (WhatsApp, Google Calendar, apps bancarias), pero
    experiencia variable con software de gestión clínica especializado.
  - Nivel socioeconómico: medio-alto. Los profesionales de salud independientes
    con consultorio propio en Rosario corresponden a sectores con ingresos
    suficientes para absorber una suscripción en dólares dentro de los costos
    operativos del consultorio.
  - Ubicación: urbana (Rosario y zona de influencia en la fase inicial); acceso
    desde dispositivos de escritorio y móviles en jornada laboral.

Estos atributos inciden directamente en las decisiones de diseño del producto:
interfaz clara y sin curva de aprendizaje pronunciada, acceso web sin instalación,
notificaciones vía Telegram (canal ya incorporado por el segmento) y
documentación en español rioplatense.


## 4.3 ¿Quiénes son los potenciales usuarios / compradores del negocio?

Conviene distinguir entre quién decide la compra y quién usa el sistema:

- Decisor / comprador: el dueño o administrador de la clínica —frecuentemente un
  profesional médico que también gestiona el negocio—, cuya decisión se guía por
  el retorno económico (reducción de ausentismo), el costo de la suscripción y la
  facilidad de adopción.

- Usuarios:
  - Personal administrativo / secretaría: gestiona la agenda, las reservas y los
    recordatorios; valora la rapidez y la claridad de la interfaz.
  - Profesionales médicos: consultan y registran la historia clínica; valoran la
    información ordenada y la explicabilidad del riesgo de ausentismo.

El producto debe satisfacer simultáneamente el criterio económico del decisor y
la usabilidad cotidiana de los usuarios operativos.


## 4.4 Pautas de comportamiento esperado de cada segmento

- Frecuencia de uso: diaria e intensiva (la agenda y la ficha de paciente son
  herramientas de uso continuo en la jornada).
- Razón de uso: ordenar la operación, reducir pérdidas por ausentismo y
  centralizar la información clínica.
- Sensibilidad al precio: moderada a alta, especialmente en el perfil del
  profesional independiente; el precio en dólares es un punto de fricción que el
  rango accesible y la demostración de retorno buscan contener.
- Proceso de adopción: influido fuertemente por la recomendación de pares
  (referidos) y por la prueba sin costo; baja predisposición a migrar desde un
  sistema heredado salvo que el beneficio sea claro y el onboarding, asistido.
- Criterio de permanencia: la recurrencia se sostiene si el sistema demuestra
  impacto medible (reporte mensual de ausentismo) y si el soporte resuelve los
  problemas con rapidez.

Perfil del visitante del sitio / usuario del sistema:

  - Sexo: sin diferencia significativa por segmento (tanto hombres como mujeres
    ejercen medicina independiente y ocupan roles administrativos en clínicas
    pequeñas en Argentina).
  - Edad: 30–55 años (decisores); 25–45 años (usuarios operativos).
  - Nivel de ingreso: medio-alto (profesionales independientes con consultorio
    propio o integrantes de centros médicos privados).
  - Frecuencia de uso: diaria, en jornada laboral (mañana y tarde).
  - Razón de uso: gestión de agenda, registro de historia clínica y seguimiento
    de ausentismo como tareas recurrentes.
  - Motivación adicional: reducción de carga administrativa, acceso a información
    clínica ordenada y anticipación de turnos en riesgo.

La información de mercado que sustenta esta segmentación proviene de fuentes
secundarias [@refes2023; @geblix2024; @informesdeexpertos2024] y del
relevamiento competitivo propio de mayo de 2026. La validación con fuentes
primarias se realizará durante el piloto previsto en el cronograma del proyecto.


\newpage

# 5. Plan de acción


## 5.1 Programas generales de acción

El plan de acción de Kuris se estructura en tres programas generales que
cubren el horizonte mayo–diciembre 2026 (etapa de construcción y validación del
MVP) y la proyección inmediata posterior (primer semestre 2027, etapa de
crecimiento inicial).

Programa G1 — Construcción y validación del producto

Objetivo: completar el MVP funcional y demostrable, validar su funcionamiento
técnico sobre al menos un piloto real, y obtener las primeras métricas
verificables de impacto sobre el ausentismo.

Este programa es la condición necesaria para todo lo demás: sin un producto
funcionando y sin datos reales de impacto, no es posible sostener el argumento
comercial ni avanzar hacia el crecimiento. La prioridad es clara: la
interoperabilidad FHIR y el aislamiento multitenant son las capacidades no
negociables sobre las que se construye todo lo demás; la predicción heurística,
los recordatorios diferenciados y el overbooking son las capacidades diferenciales
que justifican la adopción.

La metodología ICONIX — con sus cuatro casos de uso core (CU-01 a CU-04) — rige
el desarrollo y garantiza que cada componente sea especificado, implementado y
verificado antes de avanzar al siguiente.

Programa G2 — Adquisición y retención de clientes piloto

Objetivo: incorporar entre 1 y 3 clínicas piloto durante el período de
desarrollo, en modalidad de acceso sin costo a cambio de uso activo y
retroalimentación estructurada, con el doble propósito de validar la adopción y
generar los primeros casos de referencia verificables.

La estrategia de adquisición en esta etapa no es comercial en sentido estricto:
no se persigue revenue inmediato sino evidencia. El canal prioritario es la red
de contactos directos del fundador en el ecosistema médico de Rosario
(derivaciones, colegas, redes profesionales). La retención de los pilotos se
gestiona mediante soporte personalizado semanal y entrega de reportes mensuales
de ausentismo que demuestren valor medible.

La evidencia empírica en B2B SaaS indica que entre 3 y 5 clientes piloto activos
son suficientes para alcanzar product-market fit inicial en nichos acotados
[@squadsventures2025]. En sectores de salud regulados, la validación con datos
reales de al menos un piloto es condición previa al escalamiento de la propuesta
comercial [@bessemer2024].

Programa G3 — Estructura operativa y cumplimiento

Objetivo: mantener la estructura de costos liviana, asegurar el cumplimiento
normativo de los datos de salud y preparar los procesos de onboarding para
escalar de 3 a 12–18 clínicas sin degradar la calidad del servicio.

Este programa opera en paralelo con los anteriores: el cumplimiento de la Ley
27.706 y la Ley 25.326 no es negociable desde el primer cliente, y el proceso de
onboarding debe estar suficientemente documentado para que la incorporación de
cada nueva clínica no consuma tiempo desproporcionado del fundador.

El contexto regulatorio argentino opera como habilitador: la Estrategia Nacional
de Salud Digital 2025–2030 consolidó en junio de 2026 la adopción de HL7 FHIR
como estándar de interoperabilidad federal [@ops2026; @argentinagobar2026].
La arquitectura de Kuris está
alineada con esa hoja de ruta desde su diseño, lo que convierte el cumplimiento
normativo en ventaja competitiva adicional.


## 5.2 Programas específicos de acción

Los programas generales se descomponen en acciones concretas organizadas por
fase. El horizonte es el período mayo 2026 – junio 2027, que cubre desde el
inicio del desarrollo hasta el punto de equilibrio operativo estimado.


### Tabla 1. Costos operativos proyectados

| Ítem | Costo mensual (USD) | Notas |
|---|---|---|
| VPS (4 GB RAM, 2 vCPU, 80 GB SSD) | 20–30 | Proveedor: Contabo o equivalente; escala a 8 GB con >10 tenants |
| API LLM — Claude (Anthropic) | 15–40 | Estimación: 500–2.000 llamadas/mes × ~USD 0,02 por llamada (claude-haiku-4-5); variable con volumen de pilotos |
| Servicio de correo transaccional (SMTP / email API) | 10–15 | Resend o equivalente; hasta 10.000 emails/mes |
| Dominio + certificado SSL | 2 | Prorrateado anual |
| Contingencia operativa (backups, monitoreo, herramientas menores) | 38–58 | Reserva del 30 % sobre los costos anteriores |
| **TOTAL ESTIMADO (escenario base)** | **85–145** | |
| TOTAL ESTIMADO (escenario de crecimiento, 10+ tenants activos) | 150–220 | Con VPS de 8 GB y mayor volumen de API |

Nota: el costo del tiempo del fundador no se contabiliza como costo operativo
en efectivo en esta etapa (bootstrapping unipersonal), pero representa un costo
de oportunidad real que se reconoce como tal. El análisis de punto de equilibrio
opera sobre costos en efectivo; una evaluación de rentabilidad para un eventual
inversor debería incluir una remuneración de mercado para el rol de CTO/CEO
(referencia: USD 800–1.200/mes para desarrollador senior en Argentina en dólares
oficiales, según mercado freelance tech, 2026).

Punto de equilibrio operativo (sobre costos en efectivo):
- Escenario base (costos USD 145/mes): 4 clínicas × USD 40 = USD 160 → equilibrio
  individual de cada tenant adicional, antes de alcanzar el umbral de escala
- Escenario realista (10 clínicas): ingresos USD 400 − costos USD 145 = margen USD 255 (64 %)
- Escenario objetivo (18 clínicas): ingresos USD 720 − costos USD 165 = margen USD 555 (77 %)

Nota: el umbral de "12 a 18 clínicas" citado en el resto del documento
corresponde al escenario de crecimiento sostenible con margen operativo
saludable (64–77 %), no al punto de equilibrio matemático estricto (4
clínicas), que se alcanza antes pero con margen prácticamente nulo.


### Fase 1 — Construcción del MVP (mayo–agosto 2026)

Acciones técnicas:

  - Completar el backend FHIR R4 con los seis recursos críticos (Patient,
    Practitioner, Appointment, Encounter, Observation, Coverage) sobre
    arquitectura multitenant con aislamiento por tenant_id.
    Hito: todos los endpoints FHIR responden con recursos válidos según el
    validador oficial de HL7.

  - Implementar el módulo de obra social con validación de tope semanal (CU-01).
    Hito: la reserva de turno rechaza correctamente un paciente que supera el
    tope semanal de cobertura.

  - Implementar el motor de predicción heurística de ausentismo (CU-03): scoring
    por historial de no-show, anticipación, día y franja horaria.
    Hito: el sistema asigna un score de riesgo a cada turno reservado y lo expone
    en la interfaz del profesional con factores interpretables.

  - Implementar el módulo de recordatorios inteligentes diferenciados por riesgo
    (CU-03): priorización de notificaciones vía Telegram y email según score de
    ausentismo.
    Hito: pacientes de alto riesgo reciben recordatorios 48 y 24 horas antes
    del turno.

  - Implementar el módulo de overbooking inteligente (CU-04): sugerencia
    automática de sobreturno en slots con score de riesgo elevado, con tope
    configurable por profesional.
    Hito: el sistema sugiere overbooking cuando la probabilidad de ausentismo
    supera el umbral parametrizable.

  - Integrar la API de Claude (Anthropic) para resumen automático de historia
    clínica (CU-02): al cerrar un Encounter, el sistema envía los datos FHIR
    estructurados al LLM y devuelve un resumen en lenguaje natural para
    validación del médico.
    Hito: el médico recibe un borrador de resumen en menos de 3 segundos al
    cerrar la consulta; la tasa de aceptación sin edición supera el 50 % en
    el piloto.

  - Integrar la API de Claude para sugerencia automática de CIE-10: al ingresar
    el motivo de consulta en texto libre, el sistema sugiere el código CIE-10
    más probable con una breve justificación.
    Hito: el sistema sugiere el código correcto en el primer resultado en más
    del 70 % de los casos evaluados sobre el piloto.

Acciones comerciales y piloto:

  - Identificar y contactar entre 3 y 5 clínicas o consultorios en Rosario como
    candidatos a piloto. Perfil objetivo: profesionales con historial de
    ausentismo percibido como problema y disposición a adoptar herramientas
    digitales.

  - Acordar los términos del piloto: acceso sin costo durante 3 meses, compromiso
    de uso activo (mínimo 20 turnos/semana cargados en el sistema) y sesión
    mensual de retroalimentación con el fundador.

  - Realizar el onboarding de al menos 1 clínica piloto antes del cierre de
    agosto de 2026.

Indicadores de la fase:

  - Cobertura FHIR R4: 6 de 6 recursos implementados.
  - Score de ausentismo activo en producción piloto.
  - Al menos 1 clínica piloto activa con datos reales.


### Fase 2 — Validación y ajuste (septiembre–noviembre 2026)

Acciones técnicas:

  - Completar el frontend funcional: autenticación, agenda interactiva, ficha de
    paciente con historia clínica SOAP básica (CU-02), reporte mensual de
    ausentismo.
    Hito: el personal administrativo del piloto opera el sistema sin asistencia
    del fundador para las tareas diarias.

  - Ajustar los umbrales heurísticos del motor de predicción a partir de los
    primeros datos reales del piloto.
    Hito: el score de ausentismo predice correctamente en más del 60 % de los
    casos (métrica provisional sobre la base de los datos disponibles).

  - Validar la tasa de respuesta por Telegram en el piloto. Si supera el 50 %
    de confirmaciones efectivas, el canal se considera aceptable para la fase
    comercial; si no, se adelanta la integración de WhatsApp Business API.

Acciones comerciales:

  - Documentar el impacto del piloto con datos reales: tasa de ausentismo
    pre/post adopción del sistema de recordatorios, cantidad de slots de
    overbooking activados y recuperados. Este informe es el primer activo de
    ventas verificable.

  - Presentar el caso de referencia en al menos 2 reuniones con clínicas
    candidatas en Rosario.

  - Incorporar entre 1 y 2 clínicas adicionales al piloto en la misma modalidad
    sin costo.

Indicadores de la fase:

  - Frontend operativo y usado por al menos 1 piloto activo.
  - Primer reporte de impacto sobre ausentismo con datos reales.
  - Entre 2 y 3 clínicas piloto activas al cierre de noviembre de 2026.


### Fase 3 — Transición a ingresos (diciembre 2026–junio 2027)

El punto de equilibrio operativo estimado se alcanza entre 12 y 18 clínicas
suscriptas en plan base (USD 35–50 por clínica/mes), con costos fijos
operativos proyectados entre USD 85 y USD 145 mensuales. A ese nivel de escala,
los ingresos cubren los costos operativos sin financiamiento externo. La evidencia empírica en
SaaS B2B de salud indica que el ciclo de ventas en segmentos de clínicas pequeñas
es significativamente más corto que en hospitales —semanas en lugar de meses—
cuando el argumento se apoya en casos de referencia con impacto medible [@bessemer2024].

Acciones técnicas:

  - Preparar el proceso de onboarding autoasistido: guía de migración de datos
    desde planillas, documentación de usuario y video de incorporación, reduciendo
    la carga de soporte del fundador por cliente nuevo.

  - Implementar el módulo de facturación y gestión de suscripciones.

  - Establecer el proceso de monitoreo de uptime e incidentes en producción.

Acciones comerciales:

  - Activar la transición de los pilotos al plan pago: oferta de primer mes con
    descuento como puente desde el piloto gratuito.

  - Iniciar la prospección activa por referidos: solicitar a cada clínica piloto
    activa la recomendación de 2 contactos dentro de su red profesional.

  - Explorar alianza con al menos una asociación médica o colegio profesional de
    Rosario como canal de difusión (hipótesis a validar, no acuerdo vigente).

  - Meta de suscriptores al cierre de junio 2027: entre 6 y 12 clínicas activas
    en plan pago, lo que representa entre el 33 % y el 67 % del punto de
    equilibrio.

Acciones de cumplimiento normativo:

  - Formalizar la figura jurídica del emprendimiento para la facturación a
    clientes (monotributo o sociedad simple, según asesoramiento impositivo).

  - Verificar el cumplimiento de la Ley 25.326 para el tratamiento de datos
    personales de pacientes, en particular las obligaciones de información,
    consentimiento y seguridad.

  - Establecer los términos y condiciones del servicio y el acuerdo de
    procesamiento de datos (DPA) para los clientes.

Indicadores de la fase:

  - Clínicas en plan pago: meta 6–12 al cierre de junio 2027.
  - Churn mensual objetivo: menor al 5 % (referencia sectorial para SaaS B2B en
    salud: entre 3,5 % y 7,5 % mensual según @wearefounders2026).
  - Costo de onboarding por cliente: reducción del 50 % respecto del onboarding
    de los pilotos, a través de documentación y proceso autoasistido.


### Tabla 2. Resumen de hitos

| Hito | Fecha objetivo | Depende de |
|---|---|---|
| Backend FHIR R4 completo (6 recursos) | Agosto 2026 | Solo desarrollo propio |
| Motor de predicción activo (en entorno de prueba) | Agosto 2026 | Solo desarrollo propio |
| Integración LLM: resumen historia clínica | Agosto 2026 | Solo desarrollo propio |
| Integración LLM: sugerencia CIE-10 | Agosto 2026 | Solo desarrollo propio |
| 1 clínica piloto activa con datos reales | Agosto 2026 | Conseguir un piloto real (no asegurado; ver nota) |
| Frontend operativo (agenda + ficha + SOAP) | Octubre 2026 | Solo desarrollo propio |
| Primer reporte de impacto (datos reales) | Noviembre 2026 | Tener un piloto real activo |
| 2–3 clínicas piloto activas | Noviembre 2026 | Conseguir pilotos reales (no asegurado) |
| Módulo de facturación y suscripciones | Diciembre 2026 | Solo desarrollo propio |
| Transición de pilotos a plan pago | Enero 2027 | Tener pilotos reales que transicionar |
| 6–12 clínicas en plan pago | Junio 2027 | Conseguir pilotos y convertirlos en clientes pagos |
| Punto de equilibrio operativo | Junio–agosto 2027 | Alcanzar 12–18 clínicas suscriptas |

Nota: a la fecha de este documento no hay ninguna clínica piloto comprometida
—solo conversaciones exploratorias sin acuerdo ni fecha—, por lo que todos los
hitos marcados como dependientes de "conseguir un piloto real" son objetivos de
cronograma, no compromisos en curso. Si el piloto no se concreta en el plazo
previsto, el plan de contingencia académico contempla una validación simulada
como alternativa (ver 1.2 y 3.3). Los hitos de desarrollo propio (backend,
frontend, integraciones LLM) no dependen de esta validación y están bajo
control directo del fundador.


# Apéndice A — Uso de inteligencia artificial generativa y agentes especializados en el proceso de desarrollo de Kuris

## A.1 Contexto y justificación del uso de IA en el proyecto

Kuris es desarrollado por un único fundador con una disponibilidad real de
15 a 20 horas semanales. En este contexto, la incorporación de herramientas
de inteligencia artificial generativa en el ciclo de desarrollo no es un
atajo metodológico sino una decisión estratégica explícita que amplía la
capacidad de ejecución técnica del equipo sin incrementar el costo operativo.

Esta aproximación está alineada con investigaciones recientes sobre el impacto
de los asistentes de código basados en LLM en la productividad del desarrollador
individual. @peng2023 demostraron experimentalmente que el uso de
GitHub Copilot aumenta la velocidad de finalización de tareas de codificación
en un 55 % respecto del grupo de control; @barke2023 caracterizaron
los patrones de uso efectivo de estos asistentes, diferenciando el modo
exploratorio (generación de código nuevo) del modo acelerador (completado de
código conocido). El proyecto Kuris incorpora ambos modos de manera deliberada
y documentada.

La decisión de declarar explícitamente el uso de IA en este documento responde
a un criterio de transparencia metodológica: las herramientas de IA asisten la
implementación, pero las decisiones arquitectónicas, los modelos de dominio, la
validación funcional y la responsabilidad sobre el producto final permanecen bajo
criterio del autor.

## A.2 Herramienta principal: Claude Code (Anthropic)

La herramienta central del ciclo de desarrollo es Claude Code, una interfaz de
línea de comandos (CLI) desarrollada por Anthropic que integra un modelo de
lenguaje de gran escala directamente en el entorno de trabajo del desarrollador
[@anthropic2025b]. Claude Code opera con acceso al árbol de archivos del
repositorio, historial de git, resultados de tests y salida de compilación, lo
que le permite entender el contexto real del proyecto en lugar de operar sobre
fragmentos aislados.

En el contexto de Kuris, Claude Code cumple las siguientes funciones en el flujo
de desarrollo:

1. Generación de código estructural (TDD Green): dado un test escrito por el
   autor en la fase Red (test fallando, sin clase de producción), Claude Code
   genera la implementación mínima necesaria para que el test pase. El autor
   verifica el comportamiento y aprueba o corrige antes del commit.

2. Revisión de código y detección de inconsistencias: antes de cada commit,
   Claude Code revisa el diff completo contra los estándares del proyecto
   (documentados en CLAUDE.md) y advierte sobre violaciones de convenciones,
   anti-patrones identificados o inconsistencias con decisiones arquitectónicas
   previas (ADRs).

3. Generación de documentación JavaDoc y specs SDD: Claude Code genera el
   esqueleto de documentación técnica (JavaDoc, especificaciones de casos de uso)
   que el autor completa y valida.

4. Refactorización guiada: en la fase Refactor del ciclo TDD, Claude Code propone
   simplificaciones, detecta código duplicado y sugiere extracciones, manteniendo
   los tests en verde como condición de aceptación.

## A.3 Integración con la metodología ICONIX + SDD + TDD

El uso de IA se integra de forma explícita en el flujo de trabajo del proyecto,
que combina las metodologías ICONIX (para el análisis orientado a objetos),
SDD (Specification-Driven Development, para el diseño previo al código) y TDD
(Test-Driven Development, para la implementación):

| Paso metodológico | Rol del desarrollador | Rol de la IA (Claude Code) |
|---|---|---|
| 1. Spec SDD | Redacta el spec y lo aprueba (APPROVED) | Asiste en la redacción, detecta ambigüedades, propone casos de borde |
| 2. ADR (si aplica) | Toma la decisión arquitectónica | Provee contexto sobre trade-offs y precedentes |
| 3. Diseño de dominio | Define entidades y agregados DDD | Genera los stubs de clases Java anotadas |
| 4. TDD Red | Escribe el test con aserción real | Ninguno: el test debe fallar sin intervención |
| 5. TDD Green | Aprueba o corrige la implementación generada | Genera el código mínimo de producción |
| 6. TDD Refactor | Supervisa y aprueba | Propone simplificaciones, mantiene tests en verde |
| 7. Documentación | Valida y completa | Genera esqueleto JavaDoc y actualiza módulos |
| 8. Review | Aprueba el PR | Agente code-reviewer ejecuta checklist DoD |

La invariante que rige todo el flujo: la IA no aprueba nada. Toda salida del
asistente es propuesta; el desarrollador es quien acepta, rechaza o modifica
antes del commit. Esta distinción entre autoría y asistencia es estructural
en la metodología del proyecto.

## A.4 Límites explícitos del uso de IA

Para evitar la degradación de la calidad del producto y mantener la
responsabilidad técnica del autor, el proyecto establece límites explícitos
sobre lo que la IA no hace:

- No decide la arquitectura: las decisiones de diseño están documentadas en ADRs
  firmados por el autor; la IA puede proponer alternativas pero no toma la
  decisión final.
- No escribe tests: los casos de prueba son responsabilidad del autor, ya que
  son la especificación ejecutable del comportamiento esperado. Un test generado
  por IA podría validar el código incorrecto.
- No aprueba PRs: el código generado es revisado por el agente code-reviewer,
  pero la aprobación del merge es una acción del autor con evidencia de tests
  pasando.
- No valida reglas de negocio del dominio clínico: las decisiones sobre qué
  constituye un turno válido, cómo se calcula el tope semanal de una obra social
  o qué factores componen el riesgo de ausentismo son definidas por el autor a
  partir del análisis del dominio, no generadas por el modelo.

\newpage

# Referencias

::: {#refs}
:::
