Kuris — Plan de Negocios
Seminario de Trabajo Final — UAI 2026
Autor: Julián Decoppet
Legajo: B00114457-T — Sede Rosario
Profesores: Ing. Sartorio Alejandro Roberto e Ing. Banega Matias Pablo
================================================================

Ficha de Seguimiento y Revisión

  Fecha        Autor               Versión   Referencia de los cambios
  19/05/2026   Julián Decoppet     1.0       Versión inicial: Resumen Ejecutivo
                                             y punto 1 (1.1 y 1.2).
  12/05/2026   Julián Decoppet     1.1       Reescritura del resumen ejecutivo,
                                             BMC, Contexto Competitivo y fuentes.
  16/06/2026   Julián Decoppet     2.0       Segunda entrega: completado punto 1
                                             (1.3 a 1.12), punto 2, punto 3,
                                             punto 4 y punto 5 (5.1 y 5.2).
                                             BMC movido a bmc.md (anexo).
  08/06/2026   Julián Decoppet     2.1       Consolidación del documento único:
                                             integración de entrega-2.md, capítulo
                                             5 nuevo, referencias APA 7 completas.
  08/06/2026   Julián Decoppet     2.2       Incorporación de capacidades de IA
                                             generativa al MVP: resumen automático
                                             de historia clínica y sugerencia de
                                             CIE-10 mediante LLM (Claude API).
                                             Migración de stack a Java 21 +
                                             Spring Boot 3 + Angular 18.
                                             Actualización de costos operativos
                                             (ítem API LLM) y hoja de ruta.
  10/06/2026   Julián Decoppet     2.3       Corrección de consistencia: avance
                                             técnico actualizado al estado real
                                             post-migración (auth two-step en
                                             diseño, 15 ADRs, frontend Angular,
                                             docs ICONIX), canales de
                                             notificación unificados según
                                             ADR-013 (Telegram/email en MVP),
                                             justificación del posicionamiento
                                             de precio, correcciones menores
                                             (Nubimed en capa SaaS, hitos de
                                             recordatorio 48/24 h).

Nota: El Business Model Canvas se presenta como documento independiente
(bmc.md) para facilitar su visualización y actualización separada.
Los nueve bloques del canvas sintetizan los conceptos desarrollados en
los capítulos 1 a 5 de este documento.

================================================================


================================================================
RESUMEN EJECUTIVO
================================================================

Kuris es una plataforma de software de gestión clínica por
suscripción, dirigida a consultorios y clínicas médicas privadas de uno a
cinco profesionales en Argentina, con foco inicial en Rosario y zona de
influencia. La plataforma centraliza la administración de pacientes,
turnos, historia clínica y profesionales sobre una arquitectura
interoperable basada en el estándar internacional HL7 FHIR R4, lo que
garantiza que los datos clínicos sean portables, seguros y compatibles
con cualquier sistema de salud presente o futuro.

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

El ausentismo de pacientes afecta entre el 23 % y el 30 % de los turnos
en Argentina, un problema operativo de alto costo sin solución tecnológica
adoptada en el segmento de clínicas pequeñas (Geblix, 2024). En el
relevamiento realizado no se identificó ningún competidor del segmento que
combine implementación nativa del estándar HL7 FHIR R4, predicción
explicable de ausentismo por paciente y optimización automatizada de
cobertura integrada al flujo operativo.

El contexto de mercado es favorable: el sector de salud digital en América
Latina supera los USD 5.755 millones y crece al 9,5 % anual (Informes de
Expertos, 2024), con el software representando el 52,1 % del valor total.
El segmento de pequeñas clínicas privadas — aproximadamente 5.000
establecimientos solo en Argentina (REFES, 2023) — permanece desatendido
por soluciones que combinen interoperabilidad estándar e inteligencia
operacional accesible.

El mapa competitivo argentino se estructura en tres capas: en la cima,
Grupo Cormos (que en 2024 adquirió Meducar y consolidó un portfolio con
DrApp, Docturno, iTurnos, Wiri Salud y Receto, gestionando más de 20
millones de turnos anuales) ofrece soluciones masivas orientadas a
volumen; en el centro, incumbentes locales con base instalada
(Macena/Geclisa en Rosario con más de 30 años de trayectoria,
TecnoMedicus, ConsultSmart, Zindec) cubren la gestión administrativa sin
diferenciación tecnológica; y en la capa de SaaS modernos genéricos
(AgendaPro, Gendu, Turnito, Nubimed) algunos comenzaron en 2026 a
incorporar IA para tareas administrativas. Kuris se posiciona en el
hueco intermedio: modernidad técnica, especialización en clínicas pequeñas
y diferenciación funcional concreta sobre todas las capas.

El modelo de ingresos es suscripción mensual en dólares (rango tentativo
de USD 35 a USD 50 por clínica/mes en plan base, planes superiores para
clínicas con más de tres profesionales), sin contrato de permanencia, con
acceso íntegramente web. El punto de equilibrio operativo estimado se
alcanza entre 12 y 18 clínicas suscriptas, asumiendo costos fijos
consolidados en torno a USD 50 mensuales. Este umbral es alcanzable en la
fase inicial sin necesidad de financiamiento externo.

La empresa es unipersonal: Julián Decoppet, estudiante avanzado de
Ingeniería en Sistemas en UAI Rosario, es el único desarrollador y
fundador. Los logros actuales incluyen infraestructura de producción en
funcionamiento, esquema de base de datos multitenant versionado, frontend
de autenticación multiclínica navegable y documentación de ingeniería
completa (15 ADRs, especificaciones SDD y casos de uso ICONIX), con el
producto en desarrollo activo bajo metodología ICONIX.

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


================================================================
1. DESCRIPCIÓN GENERAL
================================================================


1.1 Descripción básica del negocio
----------------------------------------------------------------

Idea de negocio

Kuris es una plataforma de software como servicio (SaaS) de gestión
clínica integral, diseñada específicamente para clínicas médicas privadas de
entre 1 y 5 profesionales en Argentina. El sistema centraliza la operación
clínica — agenda de turnos, gestión de pacientes, historial clínico y
administración de profesionales — bajo un estándar internacional de
interoperabilidad médica (FHIR R4), incorporando predicción de ausentismo de
pacientes mediante reglas heurísticas diseñadas para evolucionar a Machine
Learning, con una hoja de ruta que incluye la sugerencia automática de
cobertura de profesionales por franja horaria (post-MVP).

Justificación

El sector salud privado en Argentina presenta una oportunidad estructural
desatendida: existen aproximadamente 5.000 establecimientos de salud privados
registrados (REFES, 2023), mayoritariamente consultorios y centros médicos de
pequeña escala, que operan con herramientas de gestión inadecuadas — planillas
Excel, agendas en papel o sistemas legacy que no se integran entre sí. La tasa
de ausentismo en turnos médicos en Argentina ronda el 23 %–30 % (análisis sobre
1,5 millones de turnos, Geblix, 2024), lo que representa pérdidas directas de
ingresos para los profesionales y un problema operativo crónico sin solución
tecnológica accesible para el segmento de clínicas pequeñas.

El mercado de software médico en Latinoamérica se encuentra en plena expansión:
el sector de salud digital regional está valuado en USD 5.755 millones en 2024 y
crece a una tasa compuesta anual (CAGR) de 9,5 % proyectada hasta 2034, con el
segmento de software representando el 52,1 % del valor total (Informes de
Expertos, 2024). Los sistemas de gestión clínica disponibles en el mercado local
cobran entre USD 10 y USD 25 mensuales (Medesk, 2024; DriCloud, 2024), pero
ninguno incorpora capacidades de predicción de ausentismo ni sugerencia de
cobertura basada en inteligencia artificial para clínicas de este tamaño.
El posicionamiento de precio de Kuris por encima de ese rango
(USD 35–50) se sustenta en el retorno medible: la recuperación de una
fracción de los turnos perdidos por ausentismo supera el costo mensual de
la suscripción para una clínica tipo, y el reporte mensual de ausentismo
hace ese retorno verificable.

Tipo de E-Business

Kuris es un negocio de tipo B2B (Business-to-Business): el cliente directo
es la clínica o consultorio médico, no el paciente individual. El modelo de
distribución es exclusivamente digital, sin presencia física, lo que lo clasifica
como un pure player dentro del e-business.

En una segunda etapa (fuera del alcance del presente seminario), la plataforma
incorporará un componente B2C a través de un marketplace de turnos online donde
los pacientes podrán buscar profesionales disponibles, ver slots en tiempo real y
reservar sin intervención de la secretaría, configurando un modelo mixto B2B +
B2C bajo una misma plataforma.

Modalidad de E-Commerce

La modalidad de comercialización es SaaS por suscripción mensual en dólares
estadounidenses (USD), con planes escalonados según la cantidad de profesionales
y funcionalidades habilitadas. La suscripción es recurrente, procesada de forma
digital, sin contrato de permanencia mínima. El acceso al sistema se realiza
íntegramente vía navegador web, sin instalación local requerida.


1.2 Situación actual del negocio
----------------------------------------------------------------

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

Recursos actuales

El proyecto es desarrollado en su totalidad por el autor, contando con el
asesoramiento de los docentes del Seminario de Trabajo Final. La infraestructura
opera sobre un VPS con 4 GB de RAM de costo mensual accesible, lo que mantiene
los costos operativos en un mínimo durante la etapa de desarrollo. La asistencia
de herramientas de inteligencia artificial generativa en el ciclo de desarrollo
(generación de código boilerplate, refactorización, revisión de tests, redacción
técnica) es parte explícita de la metodología, no un atajo: las decisiones
arquitectónicas, los modelos de dominio y la validación funcional permanecen bajo
criterio del autor.

Posición competitiva inicial

Los sistemas de gestión clínica disponibles en Argentina para el segmento — desde
Grupo Cormos (DrApp, Meducar, Docturno) en la capa masiva, hasta incumbentes
locales como Macena/Geclisa, TecnoMedicus, ConsultSmart y Zindec en Rosario —
ofrecen funcionalidades de agenda, historia clínica y recordatorios automáticos.
Algunos competidores (Nubimed, SFS) comenzaron a incorporar IA para tareas
administrativas en 2026. En el relevamiento realizado no se identificó ningún
competidor del segmento que combine implementación nativa del estándar HL7 FHIR
R4 con predicción explicable de ausentismo integrada al flujo operativo.
Kuris identifica esta brecha como su ventaja competitiva principal. El
detalle completo del mapa competitivo se desarrolla en la sección 2.2.


1.3 ¿Qué hace único a su negocio?
----------------------------------------------------------------

La ventaja competitiva de Kuris no reside en una funcionalidad aislada,
sino en la combinación —no replicada por la oferta relevada en el segmento de
clínicas pequeñas— de tres pilares integrados al mismo flujo operativo:

- Interoperabilidad nativa sobre HL7 FHIR R4. El sistema no almacena los datos
  clínicos en un esquema propietario al que luego se le agrega una capa de
  exportación, sino que adopta el estándar internacional FHIR R4 como modelo de
  dominio desde la base de la arquitectura. Esto garantiza que la información de
  pacientes, turnos, encuentros y cobertura sea portable y compatible con sistemas
  externos —obras sociales, laboratorios, prestadores— tanto en el presente como
  a futuro. La complejidad de implementar correctamente el estándar constituye,
  además, una barrera de entrada frente a competidores que partieron de esquemas
  cerrados. El propio Estado argentino avanza en esta dirección: la Estrategia
  Nacional de Salud Digital 2025–2030, impulsada con apoyo de la OPS y el BID,
  consolidó HL7 FHIR como estándar de interoperabilidad federal (Organización
  Panamericana de la Salud, 2026).

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
proveedor del segmento de clínicas pequeñas que combine estos cuatro pilares de
manera nativa e integrada. Los incumbentes locales ofrecen gestión administrativa
sólida sin interoperabilidad estándar ni capacidades predictivas; los SaaS
modernos genéricos incorporaron recientemente IA para tareas administrativas
(transcripción, asistentes telefónicos) pero no predicción de ausentismo
accionable por paciente ni asistencia en el flujo clínico; y las soluciones de
consolidación masiva priorizan el volumen y la estandarización por sobre la
personalización del flujo por clínica.


1.4 Factores principales que se considera harán exitoso al proyecto
----------------------------------------------------------------

- Diferenciación funcional concreta y medible. El producto ataca un dolor
  cuantificado del segmento —el ausentismo, que afecta entre el 23 % y el 30 %
  de los turnos médicos en Argentina (Geblix, 2024)— con una capacidad que la
  competencia directa no ofrece, y cuyo impacto económico es demostrable mes a
  mes mediante el reporte de ausentismo.

- Especialización en el segmento desatendido. El foco explícito en clínicas de
  uno a cinco profesionales evita competir de frente con las soluciones
  hospitalarias sobredimensionadas y con los productos masivos orientados a
  volumen, posicionando al producto en un nicho con necesidades específicas y
  baja cobertura tecnológica adecuada.

- Estructura de costos liviana y bajo punto de equilibrio. Con costos fijos
  consolidados en torno a USD 50 mensuales y un punto de equilibrio operativo
  estimado entre 12 y 18 clínicas suscriptas, el proyecto es viable en su fase
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


1.5 Estrategia: Misión, Visión y Propósito Estratégico
----------------------------------------------------------------

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


1.6 Identificación de la oportunidad de negocio
----------------------------------------------------------------

La oportunidad surge de la convergencia de cuatro condiciones del contexto:

1. Un problema operativo costoso y crónico: el ausentismo del 23 %–30 % de los
   turnos, que se traduce en pérdidas directas de ingresos para los profesionales
   y para el cual el segmento no dispone de una solución específica adoptada.

2. Un mercado en expansión sostenida: el sector de salud digital en América
   Latina, valuado en USD 5.755 millones en 2024 y con una tasa de crecimiento
   anual compuesta del 9,5 % proyectada hasta 2034, donde el software representa
   el 52,1 % del valor total (Informes de Expertos, 2024).

3. Una base instalada amplia y mal servida: aproximadamente 5.000
   establecimientos de salud privados registrados en Argentina (REFES, 2023),
   mayoritariamente de pequeña escala, que operan con planillas, agendas en papel
   o sistemas heredados sin integración.

4. Una oferta existente sin diferenciación tecnológica en el segmento: ninguno
   de los competidores relevados combina FHIR R4 nativo, predicción explicable de
   ausentismo e inteligencia operacional integrada, en un rango de precio
   accesible para clínicas pequeñas.

La conjunción de un dolor cuantificado, un mercado en crecimiento, una demanda
latente amplia y una oferta sin diferenciación define un espacio de oportunidad
concreto y defendible.


1.7 Capacidades centrales
----------------------------------------------------------------

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


1.8 Propuesta de valor para el cliente
----------------------------------------------------------------

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


1.9 Valores nucleares de la organización
----------------------------------------------------------------

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


1.10 Enfoque e iniciativas estratégicas
----------------------------------------------------------------

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


1.11 Áreas clave de resultados
----------------------------------------------------------------

Las áreas clave de resultados (ACR) sobre las que se medirá el éxito del
emprendimiento, con su justificación:

  Área clave          Por qué es clave                           Indicador
  ─────────────────────────────────────────────────────────────────────────
  Adopción            Determina la viabilidad comercial y el     N.º de clínicas
                      alcance del punto de equilibrio            suscriptas/activas
  ─────────────────────────────────────────────────────────────────────────
  Impacto en          Es el diferencial cuantificable del        Reducción de la
  ausentismo          producto frente a la competencia           tasa de ausentismo
                                                                 en clínicas usuarias
  ─────────────────────────────────────────────────────────────────────────
  Retención           En un SaaS sin permanencia, la             Tasa de renovación
                      recurrencia sostiene los ingresos          mensual / churn
  ─────────────────────────────────────────────────────────────────────────
  Eficiencia          El costo variable de onboarding y          Costo de onboarding
  operativa           soporte condiciona la escalabilidad        por cliente; tiempo
                                                                 de soporte/cliente
  ─────────────────────────────────────────────────────────────────────────
  Solidez técnica     La interoperabilidad y la seguridad        Conformidad FHIR R4;
                      son la base de la confianza en salud       aislamiento multitenant
                                                                 verificado


1.12 Ingreso al sector: estrategias de inserción
----------------------------------------------------------------

Estrategia genérica (Porter, 1980): diferenciación enfocada. El proyecto no
compite por costo —los incumbentes masivos tienen ventaja de escala— sino por una
propuesta de valor diferenciada dirigida a un segmento acotado, la clínica
pequeña, con necesidades que la oferta generalista no resuelve.

Estrategia frente a la competencia (Kotler et al., 2017): retador / especialista
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


================================================================
2. ANÁLISIS ESTRATÉGICO
================================================================


2.1 Análisis de contexto
----------------------------------------------------------------

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
alineado con la propuesta (Argentina.gob.ar, 2026).

2.1.4 Factores tecnológicos

La maduración del estándar HL7 FHIR R4 y del ecosistema SMART on FHIR habilita
técnicamente la propuesta de interoperabilidad. La disponibilidad de
infraestructura cloud y de servidores virtuales de bajo costo permite operar con
una estructura liviana. La incorporación reciente de inteligencia artificial por
parte de algunos competidores —enfocada en tareas administrativas— confirma que
el sector se mueve hacia la automatización inteligente, validando la dirección
estratégica del proyecto, aunque sobre un eje (ausentismo accionable por
paciente) distinto al que la competencia ha tomado.

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
- Problema cuantificado y costoso (ausentismo 23 %–30 %) sin solución específica
  adoptada.
- Mercado de salud digital regional en crecimiento sostenido (CAGR 9,5 %).
- Tendencia sectorial hacia la interoperabilidad y la automatización inteligente.
- Marco normativo y estrategia nacional de salud digital que favorecen la
  adopción de FHIR (Organización Panamericana de la Salud, 2026).

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


2.2 Análisis de la competencia
----------------------------------------------------------------

2.2.1 Principales competidores directos

El mercado argentino de software de gestión clínica para el segmento pequeño se
estructura en tres capas:

- Capa de consolidación nacional. Grupo Cormos, que en 2024 adquirió Meducar y
  consolidó un portfolio (DrApp, Docturno, iTurnos, Wiri Salud, Receto) que
  gestiona más de 20 millones de turnos anuales y digitaliza aproximadamente
  25.000 profesionales y 6.000 centros (Grupo Cormos, 2024). Orientado a volumen
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

En el relevamiento sobre el segmento de clínicas pequeñas no se identificó
ningún competidor que combine, de manera nativa e integrada al flujo operativo:
(a) implementación del estándar HL7 FHIR R4, (b) predicción explicable del
ausentismo por paciente con factores interpretables, y (c) inteligencia
operacional accionable (recordatorios diferenciados + overbooking).

2.2.2 Análisis de cadena de valor

  Eslabón                  Cómo lo resuelve Kuris             Aporte de valor
  ──────────────────────────────────────────────────────────────────────────────────
  Desarrollo de producto   Ejecución técnica integral del fundador   Iteración rápida con
                           (backend, frontend, infra, modelos)       costo estructural mínimo
  ──────────────────────────────────────────────────────────────────────────────────
  Infraestructura          VPS propio + Docker/PostgreSQL/Redis       Costo marginal por cliente
                           ya desplegado                             bajo; escalabilidad sin
                                                                     inversión proporcional
  ──────────────────────────────────────────────────────────────────────────────────
  Adquisición de clientes  Venta directa + referidos en el           Canal orgánico de bajo
                           ecosistema médico de Rosario              costo y alta credibilidad
  ──────────────────────────────────────────────────────────────────────────────────
  Onboarding               Migración guiada + capacitación +         Reduce fricción de
                           soporte directo del fundador              adopción; diferencial
                                                                     relacional
  ──────────────────────────────────────────────────────────────────────────────────
  Operación / soporte      Soporte personalizado en fase inicial     Retención; costo variable
                                                                     a gestionar al escalar
  ──────────────────────────────────────────────────────────────────────────────────
  Demostración de valor    Reporte mensual de ausentismo y           Justifica la recurrencia
                           eficiencia                                con impacto medible

Las actividades de soporte (infraestructura, conocimiento técnico, cumplimiento
normativo) habilitan las actividades primarias y constituyen las barreras de
imitación más relevantes del modelo.

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


================================================================
3. ANÁLISIS FODA
================================================================


3.1 Cuadro FODA
----------------------------------------------------------------

                   Origen interno              Origen externo
  ─────────────────────────────────────────────────────────────
  Positivo   FORTALEZAS                    OPORTUNIDADES
             • Diferenciación técnica      • Segmento amplio y
               única (FHIR R4 +              desatendido (≈ 5.000
               predicción explicable +       establecimientos)
               inteligencia operacional)   • Ausentismo cuantificado
             • Interoperabilidad nativa      (23 %–30 %) sin solución
               como barrera de entrada       adoptada
             • Estructura de costos        • Mercado de salud digital
               liviana; punto de            en expansión (CAGR 9,5 %)
               equilibrio bajo (12–18      • Tendencia sectorial a
               clínicas)                    interoperabilidad e IA
             • Conocimiento técnico        • Marco normativo y
               especializado del            estrategia nacional de
               fundador                     salud digital que favorece
             • Soporte personalizado         FHIR
               como diferencial
               relacional
             • Infraestructura propia
               ya operativa
  ─────────────────────────────────────────────────────────────
  Negativo   DEBILIDADES                   AMENAZAS
             • Equipo unipersonal;         • Incumbentes locales con
               capacidad de ejecución        base instalada y relación
               acotada                       establecida
             • Sin base instalada,         • Posible incorporación de
               marca ni casos de            IA predictiva por
               referencia                   competidores con más
             • Recursos comerciales y        recursos
               de marketing limitados      • Sensibilidad al precio en
             • Dependencia crítica de        dólares (ingresos de
               una sola persona             clientes en pesos)
             • Sin validación comercial    • Volatilidad macroeconómica
               real                         argentina
                                          • Resistencia al cambio en
                                            clínicas con herramientas
                                            heredadas


3.2 Análisis de Fortalezas, Oportunidades, Debilidades y Amenazas
----------------------------------------------------------------

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


3.3 Conclusión: atractivo de la industria y fortalezas del negocio
----------------------------------------------------------------

La industria del software de gestión clínica para el segmento pequeño es
atractiva: mercado amplio y en crecimiento, problema costoso y cuantificado, y
una oferta existente sin diferenciación tecnológica en el nicho. Las fortalezas
del negocio —diferenciación técnica única, interoperabilidad como barrera de
entrada y estructura de costos liviana— están alineadas con las oportunidades del
sector. El desafío central no es la viabilidad de la propuesta, sino la ejecución
y la validación de la adopción con recursos acotados, lo que define la prioridad
estratégica de la etapa: convertir la ventaja técnica en casos de referencia
reales antes de escalar.


================================================================
4. SEGMENTACIÓN
================================================================


4.1 Segmentación de consumidores y/o negocios
----------------------------------------------------------------

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


4.2 Identificación de grupos diferenciados de consumidores
----------------------------------------------------------------

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


4.3 ¿Quiénes son los potenciales usuarios / compradores del negocio?
----------------------------------------------------------------

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


4.4 Pautas de comportamiento esperado de cada segmento
----------------------------------------------------------------

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

La información de mercado que sustenta esta segmentación proviene de fuentes
secundarias (REFES, 2023; Geblix, 2024; Informes de Expertos, 2024) y del
relevamiento competitivo propio de mayo de 2026. La validación con fuentes
primarias se realizará durante el piloto previsto en el cronograma del proyecto.


================================================================
5. PLAN DE ACCIÓN
================================================================


5.1 Programas generales de acción
----------------------------------------------------------------

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
(SquadS Ventures, 2025). En sectores de salud regulados, la validación con datos
reales de al menos un piloto es condición previa al escalamiento de la propuesta
comercial (Bessemer Venture Partners, 2024).

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
como estándar de interoperabilidad federal (Organización Panamericana de la
Salud, 2026; Argentina.gob.ar, 2026). La arquitectura de Kuris está
alineada con esa hoja de ruta desde su diseño, lo que convierte el cumplimiento
normativo en ventaja competitiva adicional.


5.2 Programas específicos de acción
----------------------------------------------------------------

Los programas generales se descomponen en acciones concretas organizadas por
fase. El horizonte es el período mayo 2026 – junio 2027, que cubre desde el
inicio del desarrollo hasta el punto de equilibrio operativo estimado.


FASE 1 — Construcción del MVP (mayo–agosto 2026)

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


FASE 2 — Validación y ajuste (septiembre–noviembre 2026)

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


FASE 3 — Transición a ingresos (diciembre 2026 – junio 2027)

El punto de equilibrio operativo estimado se alcanza entre 12 y 18 clínicas
suscriptas en plan base (USD 35–50 por clínica/mes), con costos fijos
consolidados en torno a USD 50 mensuales. A ese nivel de escala, los ingresos
cubren los costos operativos sin financiamiento externo. La evidencia empírica en
SaaS B2B de salud indica que el ciclo de ventas en segmentos de clínicas pequeñas
es significativamente más corto que en hospitales —semanas en lugar de meses—
cuando el argumento se apoya en casos de referencia con impacto medible (Bessemer
Venture Partners, 2024).

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
    salud: entre 3,5 % y 7,5 % mensual según We Are Founders, 2026).
  - Costo de onboarding por cliente: reducción del 50 % respecto del onboarding
    de los pilotos, a través de documentación y proceso autoasistido.


TABLA RESUMEN DE HITOS

  Hito                                            Fecha objetivo
  ───────────────────────────────────────────────────────────────
  Backend FHIR R4 completo (6 recursos)           Agosto 2026
  Motor de predicción activo en piloto            Agosto 2026
  Integración LLM: resumen historia clínica       Agosto 2026
  Integración LLM: sugerencia CIE-10             Agosto 2026
  1 clínica piloto activa con datos reales        Agosto 2026
  Frontend operativo (agenda + ficha + SOAP)      Octubre 2026
  Primer reporte de impacto (datos reales)        Noviembre 2026
  2–3 clínicas piloto activas                     Noviembre 2026
  Módulo de facturación y suscripciones           Diciembre 2026
  Transición de pilotos a plan pago               Enero 2027
  6–12 clínicas en plan pago                      Junio 2027
  Punto de equilibrio operativo                   Junio–agosto 2027


================================================================
REFERENCIAS (APA 7)
================================================================

Anthropic. (2025). *Claude API documentation*. https://docs.anthropic.com/

Argentina.gob.ar. (2026, junio). *Se realizó encuentro para avanzar en la
estrategia de salud digital*. Ministerio de Salud de la Nación Argentina.
https://www.argentina.gob.ar/noticias/se-realizo-encuentro-para-avanzar-en-la-estrategia-de-salud-digital

Argentina.gob.ar. (s. f.). *Estándares de salud digital*. Ministerio de Salud de
la Nación Argentina.
https://www.argentina.gob.ar/salud/digital/estandares

Bessemer Venture Partners. (2024). *State of health tech 2024*.
https://www.bvp.com/atlas/state-of-health-tech-2024

DriCloud. (2024). *Software médico en la nube para gestión clínica*.
https://dricloud.com/

Geblix. (2024). *Análisis del ausentismo en turnos médicos en Argentina sobre 1,5
millones de turnos*.
https://blog.geblix.com/ausentismo-data-science-analisis

Grupo Cormos. (2024). *Adquisición de Meducar y consolidación del portfolio del
mercado local*.
https://thepostarg.com/institucionales/grupo-cormos-adquirio-meducar-y-ya-cuenta-con-el-50-de-las-empresas-del-mercado-local/

HL7 International. (s. f.). *HL7 FHIR Release 4 (R4)*. https://hl7.org/fhir/R4/

Informes de Expertos. (2024). *Mercado de salud digital en América Latina: tamaño,
participación y crecimiento 2024-2034*.
https://www.informesdeexpertos.com/informes/mercado-de-salud-digital-en-america-latina

Kotler, P., Keller, K. L., Brady, M., Goodman, M., y Hansen, T. (2017).
*Marketing management* (3.ª ed.). Pearson Education.

Medesk. (2024). *Software de gestión para clínicas y consultorios médicos*.
https://www.medesk.net/

Ministerio de Salud de la Nación Argentina. (2023). *Registro Federal de
Establecimientos de Salud (REFES)*.
https://datos.gob.ar/dataset/salud-listado-establecimientos-salud-asentados-registro-federal-refes

Organización Panamericana de la Salud. (2026, junio 5). *OPS coopera en la
transformación digital en salud pública en Argentina*.
https://www.paho.org/es/noticias/5-6-2026-ops-coopera-transformacion-digital-salud-publica-argentina

Porter, M. E. (1980). *Competitive strategy: Techniques for analyzing industries
and competitors*. Free Press.

República Argentina. (2000). *Ley 25.326 de Protección de Datos Personales*.
Boletín Oficial de la República Argentina.

República Argentina. (2023). *Ley 27.706 de Historia Clínica Electrónica*.
Boletín Oficial de la República Argentina.

SMART Health IT. (s. f.). *SMART on FHIR*. https://docs.smarthealthit.org/

SquadS Ventures. (2025). *Empresas B2B SaaS: early exits y modelos alternativos*.
https://squads.ventures/empresas-b2b-saas-early-exits/

Turnito. (2026). *Los mejores software de turnos para clínicas en Argentina [2026]*.
https://turnito.app/blog/los-mejores-software-de-turnos-para-clinicas-en-argentina-2026/

We Are Founders. (2026). *SaaS churn rates and customer acquisition costs by
industry: 2026 benchmarks*.
https://www.wearefounders.uk/saas-churn-rates-and-customer-acquisition-costs-by-industry-2025-data/


================================================================
Versión 2.2 — 08/06/2026

Cambios respecto a versión 2.1:
  - Incorporación de dos capacidades de IA generativa al MVP del
    seminario: resumen automático de historia clínica y sugerencia de
    clasificación CIE-10, implementadas mediante la API de Claude
    (Anthropic). Ambas funcionalidades figuraban en el roadmap post-MVP
    de versiones anteriores; la decisión de incluirlas en el MVP
    responde a la disponibilidad de APIs de LLM accesibles y al impacto
    diferencial que representan en el flujo clínico diario.
  - Actualización del stack tecnológico: migración de FastAPI +
    SQLAlchemy 2.0 + Next.js 14 a Java 21 + Spring Boot 3 + Spring
    Security + Spring Data JPA + Angular 18 + Angular Material. La
    migración obedece a la mayor madurez del ecosistema Java/Spring para
    RBAC empresarial, manejo de excepciones y testing estructurado,
    y a la mejor alineación con patrones de arquitectura académicamente
    documentados (Clean Architecture, DDD).
  - Tabla de hitos actualizada con los hitos de integración LLM.
  - Sección 1.3 ampliada con un cuarto pilar diferencial: asistencia
    clínica mediante IA generativa.

Cambios respecto a versión 1.1 (resumidos):
  - Documento consolidado único: integración del contenido de
    entrega-2.md (puntos 1.3 a 4.4) al cuerpo principal.
  - Business Model Canvas movido a documento separado (bmc.md).
  - Incorporación del Capítulo 5: Plan de Acción (5.1 Programas
    generales y 5.2 Programas específicos), con cronograma de hitos
    mayo 2026 – junio 2027 y tres fases operativas.
  - Referencias completamente reescrita en formato APA 7.
================================================================
