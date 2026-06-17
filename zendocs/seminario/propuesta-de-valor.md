# Propuesta de valor y contexto de mercado

## El segmento

Consultorios y clínicas médicas privadas de **1 a 5 profesionales en Argentina**, con foco inicial en Rosario y zona de influencia (Santa Fe).

Tres perfiles dentro del segmento:

- **Profesionales independientes** con consultorio propio que buscan digitalizar su operación sin invertir en infraestructura ni en sistemas sobredimensionados.
- **Centros médicos pequeños** (2–5 profesionales) que necesitan coordinación de agenda, historia clínica unificada e interoperabilidad futura con obras sociales.
- **Clínicas con pérdidas por ausentismo** que no cuentan con herramientas analíticas para anticiparlo.

## El problema

El ausentismo de pacientes afecta entre el **23 % y el 30 % de los turnos** en Argentina (Geblix, 2024) — un costo operativo crónico sin solución tecnológica adoptada en el segmento de clínicas pequeñas. Las herramientas disponibles en el mercado cubren gestión administrativa básica pero no predicen ni mitigan el fenómeno.

## Propuesta diferencial

Kuris combina gestión clínica integral con inteligencia operacional sobre una base de interoperabilidad estándar (**HL7 FHIR R4**). Ningún competidor del segmento combina estos tres ejes simultáneamente.

## Mapa competitivo

| Capa | Actores | Limitación frente a Kuris |
|---|---|---|
| Soluciones masivas | Grupo Cormos (DrApp, Docturno, iTurnos, Wiri Salud, Receto — +20M turnos/año) | Orientadas a volumen, no al segmento pequeño |
| Incumbentes locales | Macena/Geclisa (Rosario, +30 años), TecnoMedicus, ConsultSmart, Zindec | Sin diferenciación tecnológica, sin FHIR, sin predicción |
| SaaS modernos genéricos | AgendaPro, Gendu, Turnito, Nubimed | IA administrativa incipiente, sin FHIR nativo ni predicción clínica integrada |

Kuris se posiciona en el hueco intermedio: modernidad técnica, especialización en clínicas pequeñas y diferenciación funcional concreta.

## Modelo de negocio

- **Modelo**: suscripción mensual en dólares, sin contrato de permanencia mínima, acceso 100 % web.
- **Precio tentativo**: USD 35–50/mes plan base · planes superiores para clínicas con más de 3 profesionales.
- **Punto de equilibrio**: entre 12 y 18 clínicas suscriptas (costos fijos consolidados ~USD 50/mes). Alcanzable en fase inicial sin financiamiento externo.

## Contexto de mercado

- Salud digital en América Latina: **USD 5.755 M**, crecimiento del 9,5 % anual (Informes de Expertos, 2024). El software representa el 52,1 % del valor total.
- Clínicas pequeñas privadas en Argentina: ~**5.000 establecimientos** (REFES, 2023) — segmento estructuralmente desatendido.

## Barreras de entrada construidas

- Implementación nativa de HL7 FHIR R4 (complejidad especializada).
- Aislamiento multitenant desde la arquitectura (row-level, sin configuración por tenant).
- Motor predictivo integrado al flujo operativo (no como módulo separado).

Replicar el producto requiere tiempo y conocimiento especializado en interoperabilidad sanitaria y aprendizaje automático aplicado a salud.

## Riesgo principal

Lentitud en adopción inicial frente a base instalada de incumbentes locales.
**Mitigación**: acceso piloto gratuito para las primeras clínicas + soporte personalizado del fundador como diferencial frente a proveedores masivos.
