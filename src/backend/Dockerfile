# ── Stage 1: build ────────────────────────────────────────────────────────────
# Descarga dependencias y compila el JAR. Este layer se cachea si pom.xml
# no cambia, acelerando builds sucesivos.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Cachear dependencias separado del código fuente
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Compilar y empaquetar (sin tests — se corren en CI, no en build de imagen)
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
# Imagen mínima JRE — sin Maven, sin fuentes, sin herramientas de build.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Usuario no-root para correr la aplicación
RUN addgroup -S clinica && adduser -S clinica -G clinica

# Copiar solo el JAR final del stage de build
COPY --from=build /app/target/clinicasaas-backend-*.jar app.jar

# El JAR no necesita ser escribible
RUN chown clinica:clinica app.jar

USER clinica

# Puerto expuesto internamente (Nginx hace el proxy — no se publica directo)
EXPOSE 8080

# JVM tuning para contenedor con límite de memoria (512M en compose):
# -XX:MaxRAMPercentage=75  → usa hasta 75% del límite del contenedor (≈384MB heap)
# -XX:+UseContainerSupport → respeta cgroup memory limits (default en JDK 11+)
# -Djava.security.egd    → acelera startup en contenedores Linux
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
