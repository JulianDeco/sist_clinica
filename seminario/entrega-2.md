Facultad de Tecnología Informática

## PROYECTO DE BASE TECNOLÓGICA EN UN ENTORNO

## DISTRIBUIDO HABILITADO PARA LA WEB

```
SEMINARIO DE TRABAJO FINAL
```
# Kuris

_Plataforma SaaS de gestión clínica interoperable con inteligencia operacional_

```
Autor: Julian Decoppet
Legajo: B00114457-T
Sede: Rosario
Profesores: Ing. Sartorio Alejandro Roberto y Ing. Banega Matias Pablo
Fecha: Junio de 2026
```

Facultad Rosario
Ingeniería en Sistemas Informáticos

## Ficha de Seguimiento y Revisión

#### Cambios del autor

```
Fecha       Autor              Versión   Referencia de los cambios
19/05/2026  Julian Decoppet    1.0       Versión inicial: Resumen Ejecutivo y
                                         punto 1 (1.1 y 1.2).
16/06/2026  Julian Decoppet    2.0       Segunda entrega: completado punto 1
                                         (1.3 a 1.12), punto 2 (Análisis
                                         estratégico), punto 3 (FODA) y
                                         punto 4 (Segmentación).
```

#### Revisiones

```
Auditor   Versión aprobada   Responsabilidad / Rol   Fecha
```

#### Descripción del Documento

```
Título del documento    Segunda entrega — Seminario de Trabajo Final
Autor                   Julian Decoppet
Fecha de Creación       Mayo de 2026
Última Actualización    Junio de 2026
```

---

> **Nota sobre esta entrega.** El presente documento da continuidad a la
> primera entrega (Resumen Ejecutivo y puntos 1.1–1.2). Completa el
> capítulo 1 (Descripción General, puntos 1.3 a 1.12) y desarrolla los
> capítulos 2 (Análisis estratégico), 3 (Análisis FODA) y 4
> (Segmentación), de acuerdo con la estructura de la guía del Seminario y
> con el criterio de aproximaciones sucesivas: cada capítulo profundiza
> el nivel de detalle respecto del anterior.

---

Facultad Rosario
Ingeniería en Sistemas Informáticos

## 1. Descripción General (continuación)

### 1.3 ¿Qué hace único a su negocio?

La ventaja competitiva de Kuris no reside en una funcionalidad
aislada, sino en la combinación —no replicada por la oferta relevada en
el segmento de clínicas pequeñas— de tres pilares integrados al mismo
flujo operativo:

- **Interoperabilidad nativa sobre HL7 FHIR R4.** El sistema no almacena
  los datos clínicos en un esquema propietario al que luego se le agrega
  una capa de exportación, sino que adopta el estándar internacional
  FHIR R4 como modelo de dominio desde la base de la arquitectura. Esto
  garantiza que la información de pacientes, turnos, encuentros y
  cobertura sea portable y compatible con sistemas externos —obras
  sociales, laboratorios, prestadores— tanto en el presente como a
  futuro. La complejidad de implementar correctamente el estándar
  constituye, además, una barrera de entrada frente a competidores que
  partieron de esquemas cerrados.

- **Predicción explicable del ausentismo por paciente.** El sistema
  estima la probabilidad de que un paciente no se presente a su turno y
  expone, en lenguaje comprensible para el profesional, los factores que
  generan ese riesgo (historial de inasistencias, anticipación de la
  reserva, día y franja horaria). La condición de *explicabilidad* es
  deliberada: a diferencia de un modelo de caja negra, el profesional
  comprende por qué un turno se marca como riesgoso y conserva el
  criterio sobre la acción a tomar.

- **Inteligencia operacional accionable sobre la predicción.** El riesgo
  calculado no es un dato pasivo: alimenta recordatorios diferenciados
  por canal según la probabilidad de ausentismo y habilita la sugerencia
  de overbooking inteligente sobre los horarios de mayor riesgo, con un
  tope parametrizable por el profesional. La predicción se traduce
  directamente en recuperación de ingresos y en eficiencia de la agenda.

En el relevamiento competitivo realizado (mayo de 2026) no se identificó
ningún proveedor del segmento de clínicas pequeñas que combine estos
tres pilares de manera nativa e integrada. Los incumbentes locales
ofrecen gestión administrativa sólida sin interoperabilidad estándar ni
capacidades predictivas; los SaaS modernos genéricos incorporaron
recientemente IA para tareas administrativas (transcripción, asistentes
telefónicos) pero no predicción de ausentismo accionable por paciente; y
las soluciones de consolidación masiva priorizan el volumen y la
estandarización por sobre la personalización del flujo por clínica.

### 1.4 Factores principales que se considera harán exitoso al proyecto

- **Diferenciación funcional concreta y medible.** El producto ataca un
  dolor cuantificado del segmento —el ausentismo, que afecta entre el 23 %
  y el 30 % de los turnos médicos en Argentina (Geblix, 2024)— con una
  capacidad que la competencia directa no ofrece, y cuyo impacto económico
  es demostrable mes a mes mediante el reporte de ausentismo.

- **Especialización en el segmento desatendido.** El foco explícito en
  clínicas de uno a cinco profesionales evita competir de frente con las
  soluciones hospitalarias sobredimensionadas y con los productos masivos
  orientados a volumen, posicionando al producto en un nicho con
  necesidades específicas y baja cobertura tecnológica adecuada.

- **Estructura de costos liviana y bajo punto de equilibrio.** Con costos
  fijos consolidados en torno a USD 50 mensuales y un punto de equilibrio
  operativo estimado entre 12 y 18 clínicas suscriptas, el proyecto es
  viable en su fase inicial sin financiamiento externo, lo que reduce el
  riesgo financiero y otorga tiempo para validar la adopción.

- **Soporte personalizado del fundador como diferencial relacional.** En
  un mercado donde el soporte de los proveedores masivos tiende a ser
  impersonal o tercerizado, el acompañamiento directo en el onboarding y
  en la operación inicial constituye una ventaja relacional concreta para
  reducir la fricción de adopción.

- **Conocimiento técnico especializado del fundador.** La combinación de
  competencias en arquitectura SaaS, implementación de FHIR y modelos
  predictivos aplicados a salud es poco frecuente y difícil de replicar en
  el corto plazo, lo que protege la diferenciación técnica del producto.

### 1.5 Estrategia: Misión, Visión y Propósito Estratégico

**Misión.** Brindar a las clínicas y consultorios médicos pequeños de
Argentina una plataforma de gestión clínica accesible, interoperable e
inteligente que ordene su operación diaria y reduzca la pérdida de
ingresos por ausentismo, sobre estándares internacionales que garanticen
la portabilidad y la seguridad de los datos de sus pacientes.

**Visión.** Convertirse en la plataforma de referencia para la gestión
clínica de pequeñas y medianas clínicas en Argentina y la región,
reconocida por combinar interoperabilidad estándar e inteligencia
operacional accesible, y por habilitar progresivamente un ecosistema
interconectado entre profesionales, pacientes y prestadores.

**Propósito estratégico.** Demostrar que la interoperabilidad sanitaria
basada en estándares abiertos y la inteligencia operacional explicable
—históricamente reservadas a sistemas hospitalarios de gran porte— pueden
ofrecerse de manera accesible al segmento de clínicas pequeñas,
elevando el estándar tecnológico de un mercado estructuralmente
desatendido.

### 1.6 Identificación de la oportunidad de negocio

La oportunidad surge de la convergencia de cuatro condiciones del
contexto:

1. **Un problema operativo costoso y crónico:** el ausentismo del 23 %–30 %
   de los turnos, que se traduce en pérdidas directas de ingresos para los
   profesionales y para el cual el segmento no dispone de una solución
   específica adoptada.

2. **Un mercado en expansión sostenida:** el sector de salud digital en
   América Latina, valuado en USD 5.755 millones en 2024 y con una tasa de
   crecimiento anual compuesta del 9,5 % proyectada hasta 2034, donde el
   software representa el 52,1 % del valor total (Informes de Expertos,
   2024).

3. **Una base instalada amplia y mal servida:** aproximadamente 5.000
   establecimientos de salud privados registrados en Argentina (REFES,
   2023), mayoritariamente de pequeña escala, que operan con planillas,
   agendas en papel o sistemas heredados sin integración.

4. **Una oferta existente sin diferenciación tecnológica en el segmento:**
   ninguno de los competidores relevados combina FHIR R4 nativo,
   predicción explicable de ausentismo e inteligencia operacional
   integrada, en un rango de precio accesible para clínicas pequeñas.

La conjunción de un dolor cuantificado, un mercado en crecimiento, una
demanda latente amplia y una oferta sin diferenciación define un espacio
de oportunidad concreto y defendible.

### 1.7 Capacidades centrales

Las capacidades centrales del emprendimiento —entendidas como las
competencias distintivas y difíciles de imitar sobre las que se construye
la ventaja— son:

- **Implementación nativa del estándar HL7 FHIR R4**, que requiere
  conocimiento especializado en interoperabilidad sanitaria y constituye
  una barrera técnica de entrada.

- **Diseño de arquitectura multitenant con aislamiento de datos por
  clínica desde la base**, condición no negociable para operar un SaaS en
  salud y ya implementada en el prototipo.

- **Desarrollo de modelos predictivos explicables aplicados al dominio
  clínico**, con una trayectoria diseñada que evoluciona desde heurísticas
  interpretables hacia modelos entrenados por tenant a medida que cada
  clínica acumula volumen de datos.

- **Capacidad de ejecución técnica integral del fundador**, que cubre el
  ciclo completo —backend, frontend, infraestructura y modelos— y permite
  iterar el producto con costo estructural mínimo en la fase inicial.

### 1.8 Propuesta de valor para el cliente

Para una clínica o consultorio pequeño, Kuris ofrece:

- **Orden operativo integral:** agenda de turnos, gestión de pacientes,
  historia clínica electrónica y administración de profesionales en una
  única plataforma web, sin instalación local.

- **Reducción de la pérdida por ausentismo:** anticipación del riesgo por
  turno, recordatorios priorizados según ese riesgo y sugerencia de
  overbooking para recuperar capacidad, con impacto económico medible.

- **Datos portables y a prueba de futuro:** al estar construido sobre FHIR
  R4, la información clínica es compatible con sistemas externos y no queda
  cautiva de un formato propietario.

- **Acceso sin fricción:** suscripción mensual sin contrato de permanencia,
  período de prueba gratuito en el lanzamiento y soporte directo del
  fundador durante el onboarding.

La promesa central, en una frase: *ordenar la operación de la clínica y
recuperar los ingresos que el ausentismo se lleva, sobre una base de datos
clínicos segura, portable e interoperable.*

### 1.9 Valores nucleares de la organización

- **Interoperabilidad por principio:** adopción de estándares abiertos
  como base, no como agregado posterior.
- **Explicabilidad y criterio profesional:** la inteligencia del sistema
  asiste y explica; la decisión clínica permanece en el profesional.
- **Privacidad y seguridad de los datos de salud** como condición no
  negociable, alineada con la normativa argentina vigente.
- **Validación incremental:** preferir pocas funcionalidades operando
  correctamente antes que muchas implementadas de manera superficial.
- **Cercanía con el cliente:** soporte directo y acompañamiento como parte
  del producto, no como costo a minimizar.

### 1.10 Enfoque e iniciativas estratégicas

El enfoque estratégico es de **diferenciación focalizada** (en términos de
Porter): atender un segmento específico —clínicas pequeñas— con una
propuesta de valor que la competencia generalista no ofrece, evitando la
competencia frontal por precio con los productos masivos.

Las iniciativas estratégicas que materializan ese enfoque son:

1. **Construir y defender la diferenciación técnica** (FHIR R4 +
   predicción explicable + inteligencia operacional) como núcleo del
   producto.
2. **Validar la adopción con un piloto real** en Rosario antes de
   escalar, capturando métricas de impacto verificables.
3. **Crecer por referidos dentro del ecosistema médico local**,
   aprovechando las cadenas de derivación profesional como canal orgánico.
4. **Mantener una estructura de costos liviana** que permita alcanzar el
   punto de equilibrio sin financiamiento externo.
5. **Preservar una hoja de ruta de extensión** (ML por tenant, cobertura
   óptima, marketplace B2C) diseñada como evolución natural del MVP y no
   como reescritura.

### 1.11 Áreas clave de resultados

Las áreas clave de resultados (ACR) sobre las que se medirá el éxito del
emprendimiento, con su justificación:

| Área clave | Por qué es clave | Indicador asociado |
|---|---|---|
| Adopción | Determina la viabilidad comercial y el alcance del punto de equilibrio | N.º de clínicas suscriptas / activas |
| Impacto en ausentismo | Es el diferencial cuantificable del producto frente a la competencia | Reducción de la tasa de ausentismo en clínicas usuarias |
| Retención | En un SaaS sin permanencia, la recurrencia sostiene los ingresos | Tasa de renovación mensual / *churn* |
| Eficiencia operativa | El costo variable de onboarding y soporte condiciona la escalabilidad | Costo de onboarding por cliente; tiempo de soporte por cliente |
| Solidez técnica | La interoperabilidad y la seguridad son la base de la confianza en salud | Conformidad FHIR R4; aislamiento multitenant verificado |

### 1.12 Ingreso al sector: estrategias de inserción

**Estrategia genérica (Porter): diferenciación enfocada.** El proyecto no
compite por costo —los incumbentes masivos tienen ventaja de escala— sino
por una propuesta de valor diferenciada dirigida a un segmento acotado, la
clínica pequeña, con necesidades que la oferta generalista no resuelve.

**Estrategia frente a la competencia (Kotler): retador / especialista de
nicho.** Como entrante sin base instalada, Kuris no disputa
frontalmente la cuota de los líderes consolidados; se inserta como
especialista de nicho que atiende mejor que nadie un segmento específico,
construyendo posición desde la especialización antes de ampliar el alcance
geográfico (Santa Fe, interior) y funcional (roadmap post-MVP).

**Mecanismos concretos de inserción:**

- **Acceso piloto sin costo** para las primeras clínicas, reduciendo la
  barrera de adopción y generando casos de referencia.
- **Soporte personalizado del fundador** como diferencial relacional
  frente a los proveedores masivos.
- **Penetración por boca a boca** dentro de las redes de derivación
  profesional de Rosario.
- **Alianzas potenciales con colegios médicos y asociaciones
  profesionales** como vehículo de difusión y validación sectorial
  (hipótesis a validar comercialmente, no acuerdos vigentes).

---

Facultad Rosario
Ingeniería en Sistemas Informáticos

## 2. Análisis estratégico

### 2.1 Análisis de contexto

#### 2.1.1 Descripción del escenario local

El lanzamiento se sitúa en **Rosario y su zona de influencia (provincia de
Santa Fe)**, un polo con alta densidad de profesionales de la salud y
presencia de proveedores de software médico locales con trayectoria. La
elección responde a la cercanía del fundador con el ecosistema médico
local —que habilita el canal de referidos y el acompañamiento presencial
del piloto— y a la posibilidad de validar la propuesta en un mercado
abarcable antes de proyectar la expansión hacia otras ciudades del
interior.

#### 2.1.2 Factores económicos

El contexto macroeconómico argentino presenta históricamente alta
inflación, volatilidad del tipo de cambio y restricciones cambiarias, lo
que afecta la previsibilidad de costos e ingresos de cualquier
emprendimiento. Kuris mitiga parte de esta exposición fijando la
suscripción en dólares estadounidenses, lo que estabiliza el ingreso
frente a la depreciación de la moneda local; al mismo tiempo, el precio en
dólares puede percibirse como elevado por clínicas pequeñas con ingresos
en pesos, tensión que el rango de precio accesible (USD 35–50) busca
contener. La estructura de costos liviana reduce la sensibilidad del
proyecto a las fluctuaciones macro durante la fase inicial.

#### 2.1.3 Factores políticos

El marco regulatorio relevante incluye la **Ley 27.706 (Historia Clínica
Electrónica)** y la **Ley 25.326 (Protección de Datos Personales)**, que
establecen obligaciones sobre el tratamiento y la custodia de datos
sanitarios. Lejos de ser solo una restricción, el cumplimiento normativo
opera como factor habilitante: la arquitectura interoperable y el
aislamiento de datos por clínica posicionan favorablemente al producto
frente a estos requisitos. La existencia de iniciativas públicas de
información sanitaria (SISA, REFES) y la adopción creciente de estándares
de interoperabilidad configuran un entorno institucional alineado con la
propuesta.

#### 2.1.4 Factores tecnológicos

La maduración del estándar **HL7 FHIR R4** y del ecosistema **SMART on
FHIR** habilita técnicamente la propuesta de interoperabilidad. La
disponibilidad de infraestructura cloud y de servidores virtuales de bajo
costo permite operar con una estructura liviana. La incorporación reciente
de inteligencia artificial por parte de algunos competidores —enfocada en
tareas administrativas— confirma que el sector se mueve hacia la
automatización inteligente, validando la dirección estratégica del
proyecto, aunque sobre un eje (ausentismo accionable por paciente)
distinto al que la competencia ha tomado.

#### 2.1.5 Descripción del escenario: escenario-meta

El escenario-meta es un mercado de clínicas pequeñas en Rosario y Santa Fe
que, en un horizonte de mediano plazo, reconoce la interoperabilidad
estándar y la inteligencia operacional explicable como atributos
esperables —no excepcionales— de un sistema de gestión clínica. En ese
escenario, Kuris opera con una base de clientes que supera el punto
de equilibrio, casos de referencia con impacto medible sobre el ausentismo
y un canal de crecimiento orgánico por referidos profesionales.

Los factores de contexto actúan sobre el negocio del siguiente modo: los
**económicos** condicionan la sensibilidad al precio y se mitigan con la
estructura liviana y la fijación en dólares; los **políticos** operan como
habilitadores si el producto cumple la normativa de datos de salud; los
**tecnológicos** habilitan y validan la propuesta diferencial.

#### 2.1.6 Análisis sectorial: oportunidades y amenazas

**Oportunidades:**

- Segmento amplio y desatendido (≈ 5.000 establecimientos privados,
  mayoritariamente pequeños).
- Problema cuantificado y costoso (ausentismo 23 %–30 %) sin solución
  específica adoptada.
- Mercado de salud digital regional en crecimiento sostenido (CAGR 9,5 %).
- Tendencia sectorial hacia la interoperabilidad y la automatización
  inteligente.

**Amenazas:**

- Base instalada significativa de incumbentes locales con relación
  establecida con sus clientes.
- Posible incorporación de capacidades predictivas por parte de
  competidores con más recursos.
- Sensibilidad al precio en dólares de clínicas con ingresos en pesos.
- Volatilidad macroeconómica que afecta las decisiones de inversión de los
  clientes.
- Inercia y resistencia al cambio en clínicas acostumbradas a herramientas
  heredadas.

### 2.2 Análisis de la competencia

#### 2.2.1 Principales competidores directos

El mercado argentino de software de gestión clínica para el segmento
pequeño se estructura en tres capas:

- **Capa de consolidación nacional.** *Grupo Cormos*, que en 2024 adquirió
  Meducar y consolidó un portfolio (DrApp, Docturno, iTurnos, Wiri Salud,
  Receto) que gestiona más de 20 millones de turnos anuales y digitaliza
  aproximadamente 25.000 profesionales y 6.000 centros. Orientado a
  volumen y estandarización, no a integraciones específicas ni a flujos
  personalizados por clínica.

- **Capa de incumbentes locales.** Proveedores con trayectoria y base de
  clientes establecida: *Macena/Geclisa* (Rosario, más de 30 años y unos
  230 clientes en producción), *TecnoMedicus*, *ConsultSmart*,
  *Medicloud*, *Zindec* (Rosario). Productos sólidos en gestión
  administrativa y facturación, pero sin arquitectura interoperable nativa
  ni capacidades predictivas integradas al flujo operativo.

- **Capa de SaaS modernos genéricos.** *AgendaPro*, *Gendu*, *Turnito*,
  *Doctoralia*, *Meducar*: orientados a agenda y recordatorios
  automáticos, con incorporación reciente de IA para tareas
  administrativas (transcripción de consultas y asistentes telefónicos en
  *Nubimed*; IA clínica supervisada orientada a hospitales en *SFS*).

En el relevamiento sobre el segmento de clínicas pequeñas no se identificó
ningún competidor que combine, de manera nativa e integrada al flujo
operativo: (a) implementación del estándar HL7 FHIR R4, (b) predicción
explicable del ausentismo por paciente con factores interpretables, y
(c) inteligencia operacional accionable (recordatorios diferenciados +
overbooking).

#### 2.2.2 Análisis de cadena de valor

| Eslabón | Cómo lo resuelve Kuris | Aporte de valor |
|---|---|---|
| Desarrollo de producto | Ejecución técnica integral del fundador (backend, frontend, infra, modelos) | Iteración rápida con costo estructural mínimo |
| Infraestructura | VPS propio + Docker/PostgreSQL/Redis ya desplegado | Costo marginal por cliente bajo; escalabilidad sin inversión proporcional |
| Adquisición de clientes | Venta directa + referidos en el ecosistema médico de Rosario | Canal orgánico de bajo costo y alta credibilidad |
| Onboarding | Migración guiada + capacitación + soporte directo del fundador | Reduce fricción de adopción; diferencial relacional |
| Operación / soporte | Soporte personalizado en fase inicial | Retención; costo variable a gestionar al escalar |
| Demostración de valor | Reporte mensual de ausentismo y eficiencia | Justifica la recurrencia con impacto medible |

Las actividades de soporte (infraestructura, conocimiento técnico,
cumplimiento normativo) habilitan las actividades primarias y constituyen
las barreras de imitación más relevantes del modelo.

#### 2.2.3 Factores Críticos de Éxito (FCE)

1. **Lograr y sostener la diferenciación técnica** (FHIR + predicción +
   inteligencia operacional) integrada al flujo operativo.
2. **Validar el impacto sobre el ausentismo con datos reales** de al menos
   un piloto.
3. **Mantener un costo de onboarding y soporte controlado** para que el
   modelo escale.
4. **Construir un canal de referidos confiable** dentro del ecosistema
   médico local.
5. **Garantizar seguridad y cumplimiento normativo** de los datos de
   salud.
6. **Alcanzar el punto de equilibrio sin financiamiento externo.**

#### 2.2.4 Fortalezas y debilidades del negocio

**Fortalezas:** diferenciación técnica única en el segmento;
interoperabilidad nativa como barrera de entrada; estructura de costos
liviana y bajo punto de equilibrio; conocimiento técnico especializado del
fundador; capacidad de soporte cercano.

**Debilidades:** equipo unipersonal con capacidad de ejecución acotada y
riesgo de cuello de botella; ausencia de base instalada, marca y casos de
referencia; recursos comerciales y de marketing limitados; dependencia
crítica de una sola persona; aún sin validación comercial real.

---

Facultad Rosario
Ingeniería en Sistemas Informáticos

## 3. Análisis FODA

### 3.1 Cuadro FODA

| | **Origen interno** | **Origen externo** |
|---|---|---|
| **Positivo** | **Fortalezas**<br>• Diferenciación técnica única en el segmento (FHIR R4 + predicción explicable + inteligencia operacional)<br>• Interoperabilidad nativa como barrera de entrada<br>• Estructura de costos liviana; punto de equilibrio bajo (12–18 clínicas)<br>• Conocimiento técnico especializado del fundador<br>• Soporte personalizado como diferencial relacional<br>• Infraestructura propia ya operativa | **Oportunidades**<br>• Segmento amplio y desatendido (≈ 5.000 establecimientos)<br>• Ausentismo cuantificado (23 %–30 %) sin solución adoptada<br>• Mercado de salud digital en expansión (CAGR 9,5 %)<br>• Tendencia sectorial a interoperabilidad e IA<br>• Marco normativo que favorece la interoperabilidad |
| **Negativo** | **Debilidades**<br>• Equipo unipersonal; capacidad de ejecución acotada<br>• Sin base instalada, marca ni casos de referencia<br>• Recursos comerciales y de marketing limitados<br>• Dependencia crítica de una sola persona<br>• Sin validación comercial real | **Amenazas**<br>• Incumbentes locales con base instalada y relación establecida<br>• Posible incorporación de IA predictiva por competidores con más recursos<br>• Sensibilidad al precio en dólares (ingresos de clientes en pesos)<br>• Volatilidad macroeconómica argentina<br>• Resistencia al cambio en clínicas con herramientas heredadas |

### 3.2 Análisis de Fortalezas, Oportunidades, Debilidades y Amenazas

El cruce de factores orienta las estrategias del proyecto:

**Estrategias FO (fortalezas + oportunidades) — ofensivas.** Apalancar la
diferenciación técnica única sobre un segmento amplio y desatendido,
posicionando la interoperabilidad y la predicción de ausentismo como
respuesta directa a un dolor cuantificado que la oferta actual no resuelve.

**Estrategias FA (fortalezas + amenazas) — defensivas.** Frente a la base
instalada de los incumbentes y a la eventual reacción de competidores con
más recursos, usar la barrera técnica (FHIR nativo, especialización) y el
soporte cercano para construir relaciones difíciles de desplazar; fijar el
precio en dólares para protegerse de la volatilidad sin perder
accesibilidad.

**Estrategias DO (debilidades + oportunidades) — de reorientación.**
Compensar la falta de base instalada y de recursos comerciales mediante el
piloto gratuito y los referidos profesionales como canal de bajo costo,
convirtiendo casos de impacto medible en la palanca de credibilidad que
sustituye a la marca.

**Estrategias DA (debilidades + amenazas) — de supervivencia.** Mitigar el
riesgo del equipo unipersonal con una estructura de costos liviana que
permita sostener el proyecto sin presión financiera, y con un alcance de
MVP recortado y priorizado que concentre el esfuerzo en lo diferencial y
no negociable (multitenant y FHIR core).

### 3.3 Conclusión: atractivo de la industria y fortalezas del negocio

La industria del software de gestión clínica para el segmento pequeño es
**atractiva**: mercado amplio y en crecimiento, problema costoso y
cuantificado, y una oferta existente sin diferenciación tecnológica en el
nicho. Las **fortalezas del negocio** —diferenciación técnica única,
interoperabilidad como barrera de entrada y estructura de costos liviana—
están alineadas con las oportunidades del sector. El desafío central no es
la viabilidad de la propuesta, sino la **ejecución y la validación de la
adopción** con recursos acotados, lo que define la prioridad estratégica de
la etapa: convertir la ventaja técnica en casos de referencia reales antes
de escalar.

---

Facultad Rosario
Ingeniería en Sistemas Informáticos

## 4. Segmentación

### 4.1 Segmentación de consumidores y/o negocios

Al tratarse de un negocio B2B, la unidad de segmentación es la
**organización cliente** (la clínica o el consultorio), no el paciente
individual. Los criterios de segmentación aplicados son:

- **Ubicación geográfica:** Rosario y zona de influencia (Santa Fe) en la
  fase inicial; expansión progresiva al interior del país.
- **Tamaño del cliente:** clínicas y consultorios de 1 a 5 profesionales.
- **Tipo de cliente:** establecimientos de salud privados que operan con
  herramientas heterogéneas (planillas, papel, sistemas heredados).
- **Uso del producto / dolor predominante:** necesidad de orden
  operativo, de interoperabilidad futura y/o de reducción de pérdidas por
  ausentismo.

### 4.2 Identificación de grupos diferenciados de consumidores

Dentro del segmento se distinguen tres perfiles:

1. **Profesional independiente con consultorio propio.** Busca digitalizar
   su operación sin invertir en infraestructura ni en sistemas
   sobredimensionados. Sensible al precio y a la simplicidad de uso.

2. **Centro médico pequeño (2 a 5 profesionales).** Necesita coordinación
   de agenda, historia clínica unificada e interoperabilidad futura con
   obras sociales. Valora la integración y la escalabilidad.

3. **Clínica con pérdidas recurrentes por ausentismo.** No cuenta con
   herramientas analíticas para anticiparlo; es el perfil donde el
   diferencial de predicción y overbooking tiene mayor impacto percibido y
   donde el valor económico del producto es más fácil de demostrar.

La decisión de mantener el segmento amplio dentro de "clínicas pequeñas",
sin verticalizar por especialidad médica en esta etapa, responde al
objetivo de validar la propuesta en un mercado más amplio antes de
especializar.

### 4.3 ¿Quiénes son los potenciales usuarios / compradores del negocio?

Conviene distinguir entre **quién decide la compra** y **quién usa el
sistema**:

- **Decisor / comprador:** el dueño o administrador de la clínica
  —frecuentemente un profesional médico que también gestiona el negocio—,
  cuya decisión se guía por el retorno económico (reducción de ausentismo),
  el costo de la suscripción y la facilidad de adopción.

- **Usuarios:**
  - *Personal administrativo / secretaría:* gestiona la agenda, las
    reservas y los recordatorios; valora la rapidez y la claridad de la
    interfaz.
  - *Profesionales médicos:* consultan y registran la historia clínica;
    valoran la información ordenada y la explicabilidad del riesgo de
    ausentismo.

El producto debe satisfacer simultáneamente el criterio económico del
decisor y la usabilidad cotidiana de los usuarios operativos.

### 4.4 Pautas de comportamiento esperado de cada segmento

- **Frecuencia de uso:** diaria e intensiva (la agenda y la ficha de
  paciente son herramientas de uso continuo en la jornada).
- **Razón de uso:** ordenar la operación, reducir pérdidas por ausentismo
  y centralizar la información clínica.
- **Sensibilidad al precio:** moderada a alta, especialmente en el perfil
  del profesional independiente; el precio en dólares es un punto de
  fricción que el rango accesible y la demostración de retorno buscan
  contener.
- **Proceso de adopción:** influido fuertemente por la recomendación de
  pares (referidos) y por la prueba sin costo; baja predisposición a migrar
  desde un sistema heredado salvo que el beneficio sea claro y el
  onboarding, asistido.
- **Criterio de permanencia:** la recurrencia se sostiene si el sistema
  demuestra impacto medible (reporte mensual de ausentismo) y si el soporte
  resuelve los problemas con rapidez.

> *La información de mercado que sustenta esta segmentación proviene de
> fuentes secundarias (REFES 2023, Geblix 2024, Informes de Expertos 2024)
> y del relevamiento competitivo propio de mayo de 2026. La validación con
> fuentes primarias se realizará durante el piloto previsto en el
> cronograma del proyecto.*

---

## Referencias

DriCloud. (2024). *Software médico en la nube para gestión clínica.*
https://dricloud.com/

Geblix. (2024). *Análisis del ausentismo en turnos médicos en Argentina
sobre 1,5 millones de turnos.*
https://blog.geblix.com/ausentismo-data-science-analisis

Grupo Cormos. (2024). *Adquisición de Meducar y consolidación del
portfolio del mercado local.*
https://thepostarg.com/institucionales/grupo-cormos-adquirio-meducar-y-ya-cuenta-con-el-50-de-las-empresas-del-mercado-local/

HL7 International. (s. f.). *HL7 FHIR Release 4 (R4).*
https://hl7.org/fhir/R4/

Informes de Expertos. (2024). *Mercado de salud digital en América Latina:
tamaño, participación y crecimiento 2024-2034.*
https://www.informesdeexpertos.com/informes/mercado-de-salud-digital-en-america-latina

Medesk. (2024). *Software de gestión para clínicas y consultorios médicos.*
https://www.medesk.net/

Ministerio de Salud de la Nación Argentina. (2023). *Registro Federal de
Establecimientos de Salud (REFES).*
https://datos.gob.ar/dataset/salud-listado-establecimientos-salud-asentados-registro-federal-refes

República Argentina. *Ley 25.326 de Protección de Datos Personales.*

República Argentina. *Ley 27.706 de Historia Clínica Electrónica.*

SMART Health IT. (s. f.). *SMART on FHIR.* https://docs.smarthealthit.org/
