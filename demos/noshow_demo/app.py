import streamlit as st
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import shap
import requests
from datetime import datetime
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import (
    roc_auc_score, roc_curve, classification_report, confusion_matrix
)

st.set_page_config(page_title="No-Show Predictor", layout="wide")

# Intersección Maipú y Córdoba, Rosario, Argentina
LAT = -32.9507
LON = -60.6437

def wmo_to_categoria(code: int) -> str:
    if code == 0:
        return "soleado"
    elif code in (1, 2, 3, 45, 48):
        return "nublado"
    elif code in (51, 53, 61, 63, 80, 81):
        return "lluvia_leve"
    else:
        return "lluvia_intensa"

WMO_DESC = {
    0: "Cielo despejado", 1: "Mayormente despejado", 2: "Parcialmente nublado",
    3: "Cubierto", 45: "Niebla", 48: "Niebla con escarcha",
    51: "Llovizna leve", 53: "Llovizna moderada", 55: "Llovizna densa",
    61: "Lluvia leve", 63: "Lluvia moderada", 65: "Lluvia intensa",
    66: "Lluvia helada leve", 67: "Lluvia helada intensa",
    71: "Nevada leve", 73: "Nevada moderada", 75: "Nevada intensa",
    80: "Chubascos leves", 81: "Chubascos moderados", 82: "Chubascos violentos",
    95: "Tormenta", 96: "Tormenta con granizo", 99: "Tormenta con granizo intenso",
}

@st.cache_data(ttl=1800)
def obtener_clima_rosario() -> dict:
    url = (
        f"https://api.open-meteo.com/v1/forecast"
        f"?latitude={LAT}&longitude={LON}"
        f"&current=temperature_2m,apparent_temperature,precipitation,"
        f"weathercode,windspeed_10m,relativehumidity_2m"
        f"&timezone=America%2FArgentina%2FBuenos_Aires"
    )
    try:
        resp = requests.get(url, timeout=5)
        resp.raise_for_status()
        data = resp.json()["current"]
        code = int(data["weathercode"])
        return {
            "temperatura":    round(data["temperature_2m"], 1),
            "sensacion":      round(data["apparent_temperature"], 1),
            "precipitacion":  round(data["precipitation"], 1),
            "viento":         round(data["windspeed_10m"], 1),
            "humedad":        int(data["relativehumidity_2m"]),
            "wmo_code":       code,
            "descripcion":    WMO_DESC.get(code, f"Código {code}"),
            "categoria":      wmo_to_categoria(code),
            "actualizado_en": datetime.now().strftime("%H:%M"),
            "error":          None,
        }
    except Exception as e:
        return {"error": str(e), "categoria": "nublado"}

OBRAS_SOCIALES = ["IAPOS", "OSDE", "Swiss Medical", "PAMI", "Particular"]
CLIMAS = ["soleado", "nublado", "lluvia_leve", "lluvia_intensa"]
DIAS = ["lunes", "martes", "miércoles", "jueves", "viernes", "sábado"]

CLIMA_ODDS = {"soleado": 0.0, "nublado": 0.05, "lluvia_leve": 0.12, "lluvia_intensa": 0.25}
OBRA_ODDS  = {"IAPOS": 0.0, "OSDE": -0.05, "Swiss Medical": -0.07, "PAMI": 0.08, "Particular": 0.10}
DIA_ODDS   = {"lunes": 0.08, "martes": 0.0, "miércoles": 0.0, "jueves": 0.02, "viernes": 0.12, "sábado": 0.05}


@st.cache_data
def generar_datos(n: int = 1500, seed: int = 42) -> pd.DataFrame:
    rng = np.random.default_rng(seed)
    dias_anticipacion = rng.integers(0, 60, n)
    hora_turno        = rng.integers(8, 19, n)
    dia_semana        = rng.choice(DIAS, n)
    historial_noshow  = rng.beta(2, 8, n)
    obra_social       = rng.choice(OBRAS_SOCIALES, n)
    sobreturno        = rng.binomial(1, 0.15, n)
    clima             = rng.choice(CLIMAS, n, p=[0.45, 0.30, 0.15, 0.10])

    logit = (
        -1.8
        + 0.025  * dias_anticipacion
        + 0.04   * (hora_turno - 13)
        + 2.8    * historial_noshow
        + 0.35   * sobreturno
        + np.array([CLIMA_ODDS[c] * 8 for c in clima])
        + np.array([OBRA_ODDS[o] * 6 for o in obra_social])
        + np.array([DIA_ODDS[d] * 6 for d in dia_semana])
        + rng.normal(0, 0.5, n)
    )
    prob_noshow = 1 / (1 + np.exp(-logit))
    noshow = rng.binomial(1, prob_noshow)

    return pd.DataFrame({
        "dias_anticipacion": dias_anticipacion,
        "hora_turno":        hora_turno,
        "dia_semana":        dia_semana,
        "historial_noshow":  historial_noshow.round(3),
        "obra_social":       obra_social,
        "sobreturno":        sobreturno,
        "clima":             clima,
        "no_show":           noshow,
    })


def preparar_features(df: pd.DataFrame) -> pd.DataFrame:
    return pd.get_dummies(df, columns=["dia_semana", "obra_social", "clima"])


@st.cache_resource
def entrenar_modelo(n_estimators: int, max_depth: int, seed: int):
    df  = generar_datos()
    X   = preparar_features(df.drop(columns="no_show"))
    y   = df["no_show"]
    X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.2, random_state=seed)
    model = RandomForestClassifier(
        n_estimators=n_estimators,
        max_depth=max_depth,
        random_state=seed,
        class_weight="balanced",
    )
    model.fit(X_tr, y_tr)
    return model, X_tr, X_te, y_tr, y_te, X.columns.tolist()


# ── Sidebar ───────────────────────────────────────────────────────────────────
st.sidebar.title("Parámetros del modelo")

with st.sidebar.expander("¿Qué hacen estos controles?"):
    st.markdown("""
**Árboles:** El modelo está hecho de muchos árboles de decisión que votan entre sí.
Más árboles = más preciso pero más lento. Probá poner 10 y fijate cómo bajan las métricas.

**Profundidad:** Qué tan complicado puede ser cada árbol. Muy profundo = memoriza los datos
en vez de aprender → las métricas en entrenamiento suben pero en datos nuevos bajan.
Eso se llama *overfitting* (sobreajuste).

**Semilla:** Un número que inicializa el azar. Con la misma semilla siempre obtenés el mismo
resultado. Cambiala y vas a ver que el AUC varía levemente — eso muestra que hay algo de
variabilidad normal en el proceso.
""")

n_estimators = st.sidebar.slider("Árboles (n_estimators)", 10, 300, 100, 10)
max_depth    = st.sidebar.slider("Profundidad máxima", 2, 20, 8)
seed         = st.sidebar.number_input("Semilla", value=42, step=1)

st.sidebar.divider()
st.sidebar.subheader("Clima actual — Rosario")
st.sidebar.caption("Maipú y Córdoba · Open-Meteo · se actualiza cada 30 min")

clima_actual = obtener_clima_rosario()

if clima_actual["error"]:
    st.sidebar.warning(f"No se pudo obtener el clima: {clima_actual['error']}")
else:
    cat = clima_actual["categoria"]
    iconos = {"soleado": "☀️", "nublado": "☁️", "lluvia_leve": "🌦️", "lluvia_intensa": "⛈️"}
    st.sidebar.markdown(
        f"**{iconos[cat]} {clima_actual['descripcion']}**  \n"
        f"{clima_actual['temperatura']}°C · sensación {clima_actual['sensacion']}°C  \n"
        f"Humedad {clima_actual['humedad']}% · Viento {clima_actual['viento']} km/h  \n"
        f"Precipitación {clima_actual['precipitacion']} mm  \n"
        f"*Categoría del modelo:* `{cat}`  \n"
        f"*Actualizado:* {clima_actual['actualizado_en']}"
    )
    with st.sidebar.expander("¿Por qué 4 categorías de clima?"):
        st.markdown("""
El modelo necesita categorías discretas, no texto libre. Convertimos el código
meteorológico de Open-Meteo en 4 grupos:
- `soleado` → cielo despejado
- `nublado` → cubierto, niebla
- `lluvia_leve` → llovizna o chubascos moderados
- `lluvia_intensa` → lluvia fuerte, tormenta, granizo

El modelo aprendió que lluvia intensa sube la probabilidad de no-show porque la gente
prefiere no salir.
""")


# ── Título principal ──────────────────────────────────────────────────────────
st.title("Predictor de No-Show — Demo educativo")
st.markdown("""
> Este sistema predice si un paciente va a faltar a su turno médico.
> Está entrenado con **datos simulados** (no tenemos pacientes reales todavía),
> pero el modelo y los cálculos son **completamente reales**.
> El clima viene de la API de Open-Meteo en tiempo real.
""")

tabs = st.tabs(["📊 Datos", "🤖 Modelo", "🔍 Explicabilidad SHAP", "🎯 Predecir turno"])


# ══════════════════════════════════════════════════════════════════════════════
# TAB 1 — DATOS
# ══════════════════════════════════════════════════════════════════════════════
with tabs[0]:

    st.info("""
**¿De dónde vienen estos datos?**
Como no tenemos pacientes reales, generamos 1500 turnos inventados — pero con lógica real.
Por ejemplo: la probabilidad de faltar sube si llueve, si es viernes, o si el paciente
ya faltó antes. La última columna `no_show` vale **1** si el paciente no fue y **0** si fue.
""")

    df = generar_datos()
    tasa = df["no_show"].mean()

    col1, col2, col3 = st.columns(3)
    col1.metric("Total turnos simulados", len(df))
    col2.metric("No-shows", df["no_show"].sum())
    col3.metric("Tasa global de no-show", f"{tasa:.1%}")

    with st.expander("¿Qué significan estos 3 números?"):
        st.markdown(f"""
- **1500 turnos:** la cantidad de filas en la tabla. Cada fila es un turno distinto.
- **No-shows ({df['no_show'].sum()}):** cuántos de esos 1500 pacientes no fueron.
- **Tasa global ({tasa:.1%}):** si agarrás un turno al azar, hay un {tasa:.1%} de probabilidad
  de que ese paciente no haya ido. Es el promedio general antes de considerar ningún factor.
  Todo lo que hagamos con el modelo es afinar ese número para cada caso particular.
""")

    st.subheader("Tabla de datos")
    st.dataframe(df.head(20), use_container_width=True)

    with st.expander("¿Qué significa cada columna de la tabla?"):
        st.markdown("""
| Columna | Qué representa |
|---|---|
| `dias_anticipacion` | Cuántos días antes sacó el turno. Más días = más chance de olvidarse |
| `hora_turno` | Hora de la consulta (8 a 18). Los turnos de tarde tienen más no-show |
| `dia_semana` | Día de la semana. Viernes y lunes son los peores |
| `historial_noshow` | Fracción de turnos previos que este paciente no fue. Va de 0 a 1. Es el predictor más fuerte |
| `obra_social` | Cobertura médica del paciente |
| `sobreturno` | 1 = es un turno extra fuera de la agenda; 0 = turno normal |
| `clima` | Condición climática del día del turno |
| `no_show` | **La respuesta:** 1 = faltó, 0 = fue. Esto es lo que el modelo aprende a predecir |
""")

    st.subheader("¿Qué factores tienen más no-shows?")
    st.markdown("Cada gráfico muestra, para cada categoría, qué porcentaje de los pacientes faltaron. La línea roja es el promedio general.")

    fig, axes = plt.subplots(1, 3, figsize=(15, 4))
    for ax, col in zip(axes, ["clima", "dia_semana", "obra_social"]):
        tasa_col = df.groupby(col)["no_show"].mean().sort_values(ascending=False)
        tasa_col.plot(kind="bar", ax=ax, color="steelblue", edgecolor="white")
        ax.set_title(f"No-show por {col}")
        ax.set_ylabel("Proporción de no-shows")
        ax.set_xticklabels(ax.get_xticklabels(), rotation=30, ha="right")
        ax.axhline(tasa, color="red", linestyle="--", linewidth=1, label=f"promedio {tasa:.1%}")
        ax.legend(fontsize=8)
    plt.tight_layout()
    st.pyplot(fig)

    with st.expander("¿Cómo leer estos 3 gráficos de barras?"):
        st.markdown(f"""
**La altura de cada barra** = de cada 100 pacientes con esa característica, cuántos no fueron.

- **Gráfico de clima:** Si la barra de `lluvia_intensa` llega a 0.28, significa que cuando
  llovía fuerte, el 28% de los pacientes faltó. Con sol, quizás fue solo el 10%.
  Todo lo que sobresale de la línea roja (promedio {tasa:.1%}) es un factor de riesgo.

- **Gráfico de día:** Si viernes tiene la barra más alta, los pacientes del viernes faltan más.
  Puede ser porque quieren arrancar el fin de semana temprano.

- **Gráfico de obra social:** Diferencias entre coberturas. PAMI y Particular suelen tener
  más no-shows. No significa que esas personas sean "peores pacientes" — puede haber muchas
  razones (distancia, transporte, etc.).

**Cómo usarlo en la práctica:** si un turno tiene varias barras altas al mismo tiempo
(lluvia + viernes + historial malo), el riesgo de no-show se acumula.
""")

    st.subheader("¿Influye cuántos días antes se sacó el turno?")
    fig2, ax2 = plt.subplots(figsize=(10, 3))
    bins = pd.cut(df["dias_anticipacion"], bins=10)
    df.groupby(bins, observed=True)["no_show"].mean().plot(
        kind="bar", ax=ax2, color="coral", edgecolor="white"
    )
    ax2.set_title("Tasa de no-show según con cuánta anticipación se sacó el turno")
    ax2.set_ylabel("Proporción de no-shows")
    ax2.set_xlabel("Días entre que sacó el turno y la fecha de la consulta")
    ax2.axhline(tasa, color="red", linestyle="--", linewidth=1)
    plt.xticks(rotation=30, ha="right")
    plt.tight_layout()
    st.pyplot(fig2)

    with st.expander("¿Cómo leer este gráfico?"):
        st.markdown("""
El eje horizontal muestra rangos de días. La primera barra es "sacó el turno 0 a 6 días antes",
la siguiente "7 a 12 días antes", y así.

**Lo que deberías ver:** las barras de la derecha (mucha anticipación) son más altas que
las de la izquierda. Eso significa que cuando el paciente sacó el turno con 50 días de
anticipación, tiene más probabilidad de olvidarse o de que cambien sus planes.

Es intuitivo: si hoy te doy un turno para dentro de 2 meses, hay mucho tiempo para que
algo cambie. Si te lo doy para mañana, muy probablemente vayas.
""")


# ══════════════════════════════════════════════════════════════════════════════
# TAB 2 — MODELO
# ══════════════════════════════════════════════════════════════════════════════
with tabs[1]:

    st.info("""
**¿Qué pasa en esta tab?**
Acá entrenamos el modelo con el 80% de los datos y lo evaluamos con el 20% restante
(datos que el modelo nunca vio durante el entrenamiento). Esto nos dice qué tan bien
aprendió a generalizar, no solo a memorizar.
""")

    model, X_tr, X_te, y_tr, y_te, feature_cols = entrenar_modelo(
        n_estimators, max_depth, seed
    )

    y_pred  = model.predict(X_te)
    y_proba = model.predict_proba(X_te)[:, 1]
    auc     = roc_auc_score(y_te, y_proba)
    accuracy = (y_pred == y_te).mean()
    recall_noshow = (y_pred[y_te == 1] == 1).mean()

    col1, col2, col3 = st.columns(3)
    col1.metric("AUC-ROC", f"{auc:.3f}")
    col2.metric("Precisión general", f"{accuracy:.1%}")
    col3.metric("No-shows detectados", f"{recall_noshow:.1%}")

    with st.expander("¿Qué significan estos 3 números?"):
        st.markdown(f"""
**AUC-ROC ({auc:.3f}):** Agarrás un paciente que faltó y uno que fue, y le preguntás al
modelo cuál tiene más riesgo de no-show. El AUC es el porcentaje de veces que lo adivina bien.
- 0.50 = no sabe nada (como tirar una moneda)
- 0.70 = aceptable
- 0.85 = bueno ← estamos acá
- 1.00 = perfecto (sospechoso, probablemente está haciendo trampa)

**Precisión general ({accuracy:.1%}):** De todos los turnos del conjunto de prueba, en qué
porcentaje el modelo acertó si el paciente fue o no fue.
Ojo: este número puede ser engañoso si hay muchos más pacientes que van que los que faltan.

**No-shows detectados ({recall_noshow:.1%}):** De todos los pacientes que realmente faltaron,
en qué porcentaje el modelo lo predijo correctamente. Este es el número más importante para
nosotros: queremos no perdernos no-shows para poder llamar al paciente a tiempo.
""")

    st.divider()
    col_roc, col_cm = st.columns(2)

    with col_roc:
        st.subheader("Curva ROC")
        fpr, tpr, _ = roc_curve(y_te, y_proba)
        fig, ax = plt.subplots(figsize=(5, 4))
        ax.plot(fpr, tpr, color="steelblue", linewidth=2, label=f"Modelo (AUC = {auc:.3f})")
        ax.plot([0, 1], [0, 1], "k--", linewidth=1, label="Azar (AUC = 0.50)")
        ax.fill_between(fpr, tpr, alpha=0.1, color="steelblue")
        ax.set_xlabel("Tasa de falsas alarmas")
        ax.set_ylabel("Tasa de no-shows detectados")
        ax.set_title("Curva ROC")
        ax.legend()
        plt.tight_layout()
        st.pyplot(fig)

        with st.expander("¿Cómo leer la curva ROC?"):
            st.markdown("""
Imaginá que podés elegir qué tan "sensible" querés que sea el modelo:

- **Muy sensible** (punto arriba a la derecha): detecta casi todos los no-shows,
  pero también da muchas falsas alarmas — llamarías a pacientes que iban a venir igual.
- **Poco sensible** (punto abajo a la izquierda): pocas falsas alarmas,
  pero te perdés muchos no-shows.

La curva muestra todos los puntos intermedios posibles. La línea punteada diagonal es lo que
haría un modelo que no sabe nada (azar puro). **Cuanto más lejos está la curva azul de esa
diagonal (más arriba y a la izquierda), mejor es el modelo.**

El área bajo la curva (AUC) resume eso en un solo número.
""")

    with col_cm:
        st.subheader("Matriz de confusión")
        cm = confusion_matrix(y_te, y_pred)
        fig, ax = plt.subplots(figsize=(4, 3))
        sns.heatmap(
            cm, annot=True, fmt="d", cmap="Blues", ax=ax,
            xticklabels=["Predijo: fue", "Predijo: faltó"],
            yticklabels=["Realmente fue", "Realmente faltó"],
        )
        ax.set_title("Resultados del modelo")
        plt.tight_layout()
        st.pyplot(fig)

        with st.expander("¿Cómo leer la matriz de confusión?"):
            tn, fp, fn, tp = cm.ravel()
            st.markdown(f"""
Es una tabla 2×2 que compara lo que predijo el modelo contra lo que pasó en realidad.

| | |
|---|---|
| **{tn}** (arriba izquierda) ✅ | Pacientes que fueron y el modelo dijo que iban a ir. **Acierto.** |
| **{tp}** (abajo derecha) ✅ | Pacientes que faltaron y el modelo lo predijo. **Acierto.** |
| **{fp}** (arriba derecha) ❌ | Pacientes que fueron pero el modelo dijo que iban a faltar. **Falsa alarma** — los habrías llamado innecesariamente. |
| **{fn}** (abajo izquierda) ❌ | Pacientes que faltaron pero el modelo no lo vio venir. **El peor error** — el médico quedó con el turno vacío. |

**Los números en la diagonal ({tn} y {tp}) son aciertos.**
Los otros dos ({fp} y {fn}) son los dos tipos de error posibles.
En medicina preferimos minimizar los {fn} (falsos negativos), aunque eso genere más llamadas innecesarias.
""")

    st.divider()
    st.subheader("¿Qué información usó más el modelo?")
    importances = pd.Series(model.feature_importances_, index=feature_cols).sort_values(ascending=False)
    fig, ax = plt.subplots(figsize=(10, 4))
    importances.head(15).plot(kind="bar", ax=ax, color="teal", edgecolor="white")
    ax.set_title("Top 15: variables que más usó el modelo para decidir")
    ax.set_ylabel("Importancia relativa")
    plt.xticks(rotation=35, ha="right")
    plt.tight_layout()
    st.pyplot(fig)

    with st.expander("¿Cómo leer el gráfico de importancia de variables?"):
        top1 = importances.index[0]
        top2 = importances.index[1]
        st.markdown(f"""
**La barra más larga = la variable que más usó el modelo para tomar decisiones.**

Cuando el modelo construye sus árboles de decisión, registra cuántas veces usó cada variable
para separar bien "fue" de "faltó". La que más veces apareció en los cortes importantes
es la más útil.

En este caso, `{top1}` es la más importante, seguida de `{top2}`.

**¿Por qué importa saber esto?**
- Si `historial_noshow` es la más importante, significa que el pasado del paciente es el
  mejor predictor de su futuro. Tiene sentido.
- Si `clima_lluvia_intensa` aparece alta, el clima realmente importa para que los pacientes
  vayan a sus turnos.
- Si una variable que pusimos no aparece casi nada (barra muy corta), el modelo encontró
  que esa información no le sirve mucho para predecir.

Las variables con nombres como `dia_semana_viernes` o `obra_social_PAMI` son versiones
codificadas de las variables originales — el modelo las convierte en columnas de 0 y 1
porque no entiende texto directamente.
""")

    with st.expander("Reporte técnico completo"):
        st.markdown("""
Este reporte muestra métricas detalladas por clase. `precision` = de los que predije como
no-show, cuántos realmente lo fueron. `recall` = de los no-shows reales, cuántos detecté.
`f1-score` = promedio entre ambas.
""")
        st.text(classification_report(y_te, y_pred, target_names=["Asistió", "No-show"]))


# ══════════════════════════════════════════════════════════════════════════════
# TAB 3 — SHAP
# ══════════════════════════════════════════════════════════════════════════════
with tabs[2]:

    st.subheader("SHAP — ¿Por qué el modelo toma cada decisión?")

    st.warning("""
**El problema de la "caja negra"**

Un modelo puede ser muy preciso pero completamente opaco: predice bien pero no te dice por qué.
Eso es peligroso en medicina. SHAP resuelve eso — abre la caja negra y explica cada predicción.
""")

    st.info("""
**¿Qué hace SHAP exactamente?**

Para cada turno, SHAP le pregunta al modelo: *"¿cuánto cambiaría tu predicción si no supieras
el clima?"*. La diferencia es la contribución del clima. Lo mismo para cada variable.
Al final sabés exactamente cuánto "empujó" cada factor hacia "va a faltar" o "va a venir".

Es como repartir la responsabilidad de la predicción entre todas las variables.
""")

    n_shap = st.slider("¿Cuántos turnos analizar?", 50, 300, 100, 50)
    st.caption("Más turnos = gráfico más representativo pero tarda más en calcular.")

    if st.button("Calcular SHAP", type="primary"):
        with st.spinner("Calculando... (puede tardar unos segundos)"):
            explainer   = shap.TreeExplainer(model)
            shap_values = explainer.shap_values(X_te.iloc[:n_shap])
            sv = shap_values[1] if isinstance(shap_values, list) else shap_values[:, :, 1]

            fig, ax = plt.subplots(figsize=(10, 6))
            shap.summary_plot(sv, X_te.iloc[:n_shap], show=False, plot_size=None)
            plt.tight_layout()
            st.pyplot(plt.gcf())
            plt.close()

        with st.expander("¿Cómo leer este gráfico? (importante)"):
            st.markdown("""
Es el gráfico más denso de toda la app. Te lo explico parte por parte:

**Cada fila** es una variable (historial_noshow, dias_anticipacion, etc.).
Las filas están ordenadas de más a menos importante — la de arriba es la que más impacta.

**Cada puntito** es un turno del conjunto analizado.

**Posición horizontal del punto:**
- Punto a la **derecha** → esa variable empujó al modelo a decir *"este paciente va a faltar"*
- Punto a la **izquierda** → esa variable empujó a decir *"este paciente va a venir"*
- Punto en el **centro** → esa variable no influyó mucho en ese turno particular

**Color del punto:**
- **Rojo** → el valor de esa variable era alto en ese turno
- **Azul** → el valor era bajo

**Ejemplo para leerlo:**
Mirá la fila `historial_noshow`. Deberías ver puntos **rojos a la derecha**: eso significa
que cuando el historial de faltas era alto (rojo), el modelo predijo que iba a faltar (derecha).
Y puntos **azules a la izquierda**: historial bajo → el modelo predijo que iba a venir.

**Eso es exactamente lo que esperabas**, ¿no? Si alguien faltó muchas veces antes,
es más probable que falte de nuevo.

Si ves puntos rojos a la *izquierda* en alguna variable, significa que esa variable
*reduce* el riesgo cuando su valor es alto — por ejemplo, una obra social con alta adherencia.
""")


# ══════════════════════════════════════════════════════════════════════════════
# TAB 4 — PREDICCIÓN INDIVIDUAL
# ══════════════════════════════════════════════════════════════════════════════
with tabs[3]:

    st.subheader("Simulá un turno y predecí si el paciente va a faltar")

    st.success("""
**¿Qué pasa cuando apretás "Predecir"?**
Los datos que ingresás pasan por los 100 árboles del Random Forest. Cada árbol da su
veredicto. El porcentaje final es el promedio de todos los votos. Todo el cálculo
ocurre en tiempo real en tu computadora — no hay nada pre-calculado.
""")

    clima_default_idx = CLIMAS.index(clima_actual["categoria"]) if not clima_actual["error"] else 0
    if not clima_actual["error"]:
        st.info(
            f"Clima en Rosario ahora mismo: **{clima_actual['descripcion']}** "
            f"({clima_actual['temperatura']}°C) → el campo clima ya viene pre-seleccionado con `{clima_actual['categoria']}`"
        )

    st.markdown("**Completá los datos del turno:**")

    col1, col2 = st.columns(2)
    with col1:
        dias_ant = st.slider(
            "Días de anticipación",
            0, 60, 7,
            help="¿Cuántos días antes de la consulta sacó el turno el paciente? Más días = más riesgo de olvidarse."
        )
        hora = st.slider(
            "Hora del turno",
            8, 18,
            datetime.now().hour if 8 <= datetime.now().hour <= 18 else 10,
            help="Hora de la consulta. Los turnos de tarde (16-18 hs) tienen más no-show."
        )
        historial = st.slider(
            "Historial de no-shows del paciente",
            0.0, 1.0, 0.1, 0.01,
            help="Fracción de turnos previos que no fue. 0 = nunca faltó. 1 = siempre faltó. Es el predictor más importante."
        )
        es_sobreturno = st.checkbox(
            "Es sobreturno",
            help="Los sobreturnos son turnos extra fuera de la agenda. Tienen algo más de no-show porque el paciente los siente como 'menos confirmados'."
        )
    with col2:
        dia = st.selectbox(
            "Día de la semana",
            DIAS,
            index=min(datetime.now().weekday(), 5),
            help="Viernes y lunes son los días con mayor tasa de no-show históricamente."
        )
        obra = st.selectbox(
            "Obra social",
            OBRAS_SOCIALES,
            help="Distintas coberturas tienen distintas tasas de adherencia a los turnos."
        )
        clima = st.selectbox(
            "Clima del día del turno",
            CLIMAS,
            index=clima_default_idx,
            help="Pre-cargado con el clima real de Rosario ahora mismo (Open-Meteo)."
        )

    with st.expander("Entendé qué hace cada control antes de predecir"):
        st.markdown(f"""
| Control | Por qué importa |
|---|---|
| **Días de anticipación ({dias_ant})** | Cuanto más tiempo pase entre que sacó el turno y la consulta, más fácil olvidarse o que cambien los planes |
| **Hora del turno ({hora}:00)** | Mañana temprano la gente va más. Al caer la tarde ya tienen más cosas encima |
| **Historial no-show ({historial:.0%})** | El mejor predictor. Si alguien faltó el 50% de las veces, el modelo lo tiene muy en cuenta |
| **Sobreturno ({'sí' if es_sobreturno else 'no'})** | Los sobreturnos se sienten "menos oficiales" para el paciente |
| **Día: {dia}** | Los extremos de la semana tienen más no-show |
| **Obra social: {obra}** | Afecta por diferencias de acceso, copago y adherencia |
| **Clima: {clima}** | Viene del clima real de ahora en Rosario. Lluvia intensa sube bastante el riesgo |
""")

    if st.button("Predecir", type="primary"):
        model, _, X_te, _, _, feature_cols = entrenar_modelo(n_estimators, max_depth, seed)

        row = {
            "dias_anticipacion": dias_ant,
            "hora_turno":        hora,
            "historial_noshow":  historial,
            "sobreturno":        int(es_sobreturno),
        }
        for d in DIAS:
            row[f"dia_semana_{d}"] = int(dia == d)
        for o in OBRAS_SOCIALES:
            row[f"obra_social_{o}"] = int(obra == o)
        for c in CLIMAS:
            row[f"clima_{c}"] = int(clima == c)

        input_df = pd.DataFrame([row]).reindex(columns=feature_cols, fill_value=0)
        proba    = model.predict_proba(input_df)[0][1]

        st.divider()
        if proba >= 0.5:
            st.error(f"⚠️ Alta probabilidad de no-show: **{proba:.1%}**")
            st.markdown("**Sugerencia:** considerá llamar al paciente para confirmar el turno.")
        elif proba >= 0.30:
            st.warning(f"🟡 Riesgo moderado de no-show: **{proba:.1%}**")
            st.markdown("**Sugerencia:** podría valer la pena enviar un recordatorio por WhatsApp.")
        else:
            st.success(f"✅ Baja probabilidad de no-show: **{proba:.1%}**")
            st.markdown("**Sugerencia:** este paciente probablemente vaya. No necesita seguimiento especial.")

        st.progress(float(proba), text=f"Probabilidad de no-show: {proba:.1%}")

        with st.expander("¿Cómo se calculó este porcentaje?"):
            st.markdown(f"""
Tus respuestas se convirtieron en una fila de números (el modelo no entiende texto).
Esa fila recorrió los {n_estimators} árboles del Random Forest. Cada árbol dijo un número
entre 0 y 1. El promedio de todos esos números es **{proba:.1%}**.

Si el número supera 0.50 (50%), el modelo clasifica el turno como "probable no-show".
Ese umbral de 50% se puede ajustar según qué error te molesta más: preferís llamar de más
o preferís arriesgarte a tener el consultorio vacío.
""")

        st.subheader("¿Qué factores pesaron más en esta predicción?")
        explainer   = shap.TreeExplainer(model)
        shap_values = explainer.shap_values(input_df)
        sv = shap_values[1] if isinstance(shap_values, list) else shap_values[:, :, 1]

        shap_df = pd.Series(sv[0], index=feature_cols).sort_values(ascending=False).head(8)
        colors  = ["salmon" if v > 0 else "steelblue" for v in shap_df.values]

        fig, ax = plt.subplots(figsize=(8, 3))
        shap_df.plot(kind="barh", ax=ax, color=colors[::-1], edgecolor="white")
        ax.invert_yaxis()
        ax.axvline(0, color="black", linewidth=0.8)
        ax.set_title("Contribución de cada factor a esta predicción")
        ax.set_xlabel("← bajó el riesgo   |   subió el riesgo →")
        plt.tight_layout()
        st.pyplot(fig)

        with st.expander("¿Cómo leer este gráfico?"):
            st.markdown(f"""
Cada barra representa un factor del turno que ingresaste.

- **Barra roja (hacia la derecha):** ese factor *subió* la probabilidad de no-show.
  Cuanto más larga, más influyó negativamente.
- **Barra azul (hacia la izquierda):** ese factor *bajó* la probabilidad de no-show.
  Cuanto más larga, más jugó a favor de que el paciente venga.

La probabilidad final ({proba:.1%}) es básicamente el resultado de sumar todas esas barras
rojas y restarles las azules, partiendo de una base del ~14% (la tasa promedio general).

Si la barra más larga es `historial_noshow` en rojo, significa que el historial de
ese paciente fue el factor determinante para esta predicción.
""")
