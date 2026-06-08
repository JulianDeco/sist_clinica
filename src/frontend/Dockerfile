# ── Stage 1: build Angular ────────────────────────────────────────────────────
# Una vez que T-002 genere el workspace Angular, este stage compila el SPA.
FROM node:22-alpine AS build

WORKDIR /app

# Cachear node_modules separado del código fuente
COPY package*.json ./
RUN npm ci --prefer-offline

# Build de producción
COPY . .
RUN npm run build -- --configuration=production

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
# Nginx sirve los archivos estáticos del build Angular.
FROM nginx:alpine

# Configuración Nginx para SPA (redirige rutas al index.html — Angular Router)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copiar el output del build Angular
COPY --from=build /app/dist/clinicasaas/browser /usr/share/nginx/html

EXPOSE 80
