Kuris — Business Model Canvas
Seminario de Trabajo Final — UAI 2026
Autor: Julián Decoppet
================================================================

Ver también: plan-de-negocios.md (contexto estratégico completo)

================================================================
BUSINESS MODEL CANVAS
================================================================

SEGMENTOS DE CLIENTES

Clínicas y consultorios médicos privados de 1 a 5 profesionales en
Argentina, con foco inicial en Rosario y zona de influencia (Santa Fe). El
segmento se caracteriza por: operación con herramientas heterogéneas
(planillas, agendas en papel, sistemas legacy desarticulados), bajo poder
de negociación frente a proveedores de software hospitalario, y costo
operativo crónico por ausentismo de pacientes sin solución específica
adoptada.

Tres perfiles dentro del segmento:

  - Profesionales independientes con consultorio propio que buscan
    digitalizar su operación sin invertir en infraestructura ni en sistemas
    sobredimensionados.
  - Centros médicos pequeños con 2 a 5 profesionales que necesitan
    coordinación de agenda, historia clínica unificada e interoperabilidad
    futura con obras sociales.
  - Clínicas que sufren pérdidas económicas recurrentes por ausentismo y
    no cuentan con herramientas analíticas para anticiparlo.

La elección de mantener el segmento amplio dentro de "clínicas pequeñas",
sin verticalizar por especialidad en esta etapa, responde al objetivo de
validar la propuesta en un mercado más amplio antes de especializar.

PROPUESTA DE VALOR

Kuris es una plataforma de gestión clínica integral que resuelve la
operación diaria de consultorios pequeños — agenda de turnos, gestión de
pacientes, historia clínica electrónica y administración de profesionales
— sobre una arquitectura interoperable basada en el estándar internacional
HL7 FHIR R4, que garantiza que los datos clínicos sean portables y
compatibles con sistemas externos (obras sociales, laboratorios,
prestadores) tanto hoy como a futuro.

Como diferencial sobre la oferta existente, el MVP de Kuris
incorpora:

  - Predicción explicable de ausentismo: estima la probabilidad de que
    un paciente no se presente y explica los factores que generan ese
    riesgo, mediante reglas heurísticas sobre historial de no-show,
    anticipación, día y franja horaria. Diseñado para evolucionar a
    Machine Learning por tenant.

  - Recordatorios inteligentes diferenciados por riesgo: priorización
    de notificaciones (WhatsApp / email / SMS) según probabilidad de
    ausentismo.

  - Overbooking inteligente: sugerencia de sobreturno o doble agenda
    para slots con alto riesgo, con tope parametrizable.

Diferenciales en roadmap post-MVP: resumen automático FHIR con LLM,
cobertura óptima automatizada, sugerencia CIE-10, ML entrenado por
tenant, integraciones con obras sociales y marketplace B2C.

Acceso 100% web, sin instalación, sin contrato de permanencia mínima.

CANALES

Venta directa a dueños y administradores de clínicas mediante
demostraciones online y reuniones presenciales en Rosario. Red de
referidos entre profesionales de la misma cadena de derivación (canal
orgánico clave en el sector salud). Presencia digital orientada al
segmento (contenido sobre gestión de ausentismo, interoperabilidad, casos
de uso con impacto económico medible). Alianzas potenciales con colegios
médicos y asociaciones profesionales de Rosario y Santa Fe como vehículo
de difusión y validación sectorial (hipótesis a validar comercialmente,
no acuerdos vigentes).

RELACIÓN CON CLIENTES

Onboarding guiado con migración de datos desde sistemas previos
(planillas, sistemas legacy) y capacitación inicial de usuarios. Soporte
directo del fundador durante la fase de lanzamiento, como ventaja
diferencial frente a proveedores masivos donde el soporte es impersonal o
tercerizado. Reportes mensuales automáticos de ausentismo y eficiencia
operativa que demuestran el valor del sistema de forma medible mes a mes.
Período de prueba gratuito durante el lanzamiento para reducir la
fricción de adopción inicial.

FUENTES DE INGRESOS

Suscripción mensual en dólares estadounidenses por clínica, con planes
escalonados según cantidad de profesionales activos (rango tentativo:
USD 35 a USD 50 por clínica/mes en plan base; planes superiores para
clínicas con más de 3 profesionales). Sin costo de instalación ni
permanencia mínima. En etapa posterior: ingreso adicional por
implementaciones complejas (migración de historia clínica preexistente,
integración con sistemas externos de obras sociales).

RECURSOS CLAVE

Implementación nativa del estándar HL7 FHIR R4, diferencial técnico que
no se identifica en los competidores directos del segmento y que
constituye una barrera de entrada por la complejidad y especialización
requerida. Arquitectura multitenant con aislamiento de datos por tenant
desde la base, condición no negociable para operación SaaS en salud.
Módulo de predicción de ausentismo adaptable a los patrones de cada
clínica: heurísticas en etapa inicial, modelos por tenant al acumular
volumen suficiente. Infraestructura propia de bajo costo operativo (VPS,
PostgreSQL, Redis, Docker) ya desplegada y funcional. Conocimiento
técnico del fundador en arquitectura SaaS, FHIR y modelos predictivos
aplicados al dominio de salud.

ACTIVIDADES CLAVE

Desarrollo y mantenimiento del producto (Java 21 + Spring Boot 3 + Angular 18
sobre arquitectura FHIR). Evolución del módulo de predicción de ausentismo:
ajuste de heurísticas iniciales y entrenamiento de modelos por tenant
cuando cada clínica acumula volumen de turnos suficiente. Mantenimiento
de conformidad con FHIR R4 y la regulación argentina de historia clínica
electrónica (Ley 27.706) y protección de datos personales (Ley 25.326).
Onboarding personalizado y soporte directo de clientes en fase inicial.
Ventas directas y construcción de red de referidos en el sector salud de
Rosario y Santa Fe.

SOCIOS CLAVE

  - Proveedores de infraestructura cloud y servicios de hosting (VPS,
    dominios, certificados SSL).
  - Proveedores de servicios de autenticación, correo y notificaciones
    (SMTP, WhatsApp Business API, SMS).
  - Estándares y ecosistema de interoperabilidad sanitaria (HL7
    International, SMART on FHIR, comunidad de implementadores FHIR
    Argentina).
  - Obras sociales y sistemas públicos (SISA, REFES) como contrapartes
    de integración futura vía FHIR.
  - Asesoría jurídica en protección de datos sanitarios y cumplimiento
    normativo, requerida a partir de la fase de escalamiento.
  - Alianzas estratégicas potenciales con colegios médicos y asociaciones
    profesionales de Rosario y Santa Fe (relaciones por desarrollar, no
    acuerdos vigentes).

ESTRUCTURA DE COSTOS

Costos fijos mensuales: infraestructura de servidor (VPS 4 GB RAM,
dominio, certificado SSL) en torno a USD 25 a USD 35 mensuales en fase
inicial; servicios de notificaciones (SMTP transaccional y eventualmente
WhatsApp Business API) proporcionales al volumen y marginales en fase
inicial.

Costos variables por cliente: onboarding y migración inicial de datos —
costo operativo no trivial, ya que requiere tiempo de configuración,
capacitación y soporte personalizado del fundador en las primeras semanas
de cada cliente — y soporte recurrente, variable según madurez del
cliente y decreciente con el tiempo.

Costos no monetarios: el desarrollo está a cargo del fundador en fase
inicial, lo que elimina el rubro de mayor peso en un SaaS temprano
(equipo técnico) a cambio de una capacidad de ejecución acotada.

El costo tecnológico marginal por cliente adicional es bajo y permite
escalar la base de clientes sin inversión proporcional en infraestructura.
Los costos operativos (onboarding, capacitación, soporte) sí son
variables y deben gestionarse explícitamente a medida que la base de
clientes crece.

Punto de equilibrio operativo estimado: entre 12 y 18 clínicas suscriptas
en plan base, asumiendo ticket promedio de USD 35 a USD 50 mensuales y
costos fijos operativos proyectados entre USD 85 y USD 145 mensuales
(detalle en plan-de-negocios.md, sección 5.2). Esta proyección es
alcanzable en la fase inicial sin requerir financiamiento externo, dada
la estructura de costos descrita.


================================================================
Versión 1.0 — 12/05/2026
================================================================
