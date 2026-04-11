#!/bin/bash
# setup.sh — Configuración inicial segura del entorno
# Uso: ./setup.sh [--prod]
# Genera .env con secretos aleatorios y levanta Docker Compose.

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$PROJECT_DIR/.env"
PROD_MODE=false
[[ "${1:-}" == "--prod" ]] && PROD_MODE=true

# ─── Colores ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
ok()   { echo -e "${GREEN}✓${NC} $*"; }
warn() { echo -e "${YELLOW}⚠${NC} $*"; }
err()  { echo -e "${RED}✗${NC} $*"; exit 1; }

# ─── 1. Verificar dependencias ────────────────────────────────────────────────
echo "→ Verificando dependencias..."
command -v docker   &>/dev/null || err "Docker no instalado: https://docs.docker.com/engine/install/"
command -v openssl  &>/dev/null || err "openssl no encontrado"
docker compose version &>/dev/null || err "Docker Compose plugin no encontrado"
ok "Dependencias OK"

# ─── 2. Generar .env si no existe ─────────────────────────────────────────────
if [[ -f "$ENV_FILE" ]]; then
    warn ".env ya existe — no se sobreescribe. Borralo manualmente si querés regenerar."
else
    echo "→ Generando .env con secretos aleatorios..."

    # Generadores seguros
    gen_pass() { openssl rand -base64 32 | tr -d '/+=' | head -c 40; }
    gen_key()  { openssl rand -base64 48 | tr -d '/+=' | head -c 64; }

    POSTGRES_PASSWORD=$(gen_pass)
    REDIS_PASSWORD=$(gen_pass)
    SECRET_KEY=$(gen_key)

    if $PROD_MODE; then
        ENVIRONMENT="production"
        BASE_URL="https://TU_DOMINIO.com"
        API_URL="https://TU_DOMINIO.com"
        warn "Modo producción: editá BASE_URL y NEXT_PUBLIC_API_URL en .env con tu dominio real"
    else
        ENVIRONMENT="development"
        BASE_URL="http://localhost"
        API_URL="http://localhost"
    fi

    cat > "$ENV_FILE" << EOF
# Generado automáticamente por setup.sh — $(date -u +"%Y-%m-%dT%H:%M:%SZ")
# NO commitear este archivo (.gitignore lo excluye)

# ─── Base de datos ────────────────────────────────────────────────────────────
POSTGRES_USER=clinica_user
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
POSTGRES_DB=clinica_db
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
DATABASE_URL=postgresql+asyncpg://clinica_user:${POSTGRES_PASSWORD}@postgres:5432/clinica_db

# ─── Redis ────────────────────────────────────────────────────────────────────
REDIS_PASSWORD=${REDIS_PASSWORD}
REDIS_URL=redis://:${REDIS_PASSWORD}@redis:6379/0

# ─── JWT / Auth ───────────────────────────────────────────────────────────────
SECRET_KEY=${SECRET_KEY}
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30
REFRESH_TOKEN_EXPIRE_DAYS=7

# ─── App ──────────────────────────────────────────────────────────────────────
ENVIRONMENT=${ENVIRONMENT}
BASE_URL=${BASE_URL}

# ─── Frontend ─────────────────────────────────────────────────────────────────
NEXT_PUBLIC_API_URL=${API_URL}
NEXT_PUBLIC_CLIENT_ID=clinica-frontend
EOF

    chmod 600 "$ENV_FILE"   # solo el owner puede leer/escribir
    ok ".env generado con secretos aleatorios (permisos 600)"
fi

# ─── 3. Asegurar que .env esté en .gitignore ──────────────────────────────────
GITIGNORE="$PROJECT_DIR/.gitignore"
if ! grep -qx ".env" "$GITIGNORE" 2>/dev/null; then
    echo ".env" >> "$GITIGNORE"
    ok ".env agregado a .gitignore"
fi

# ─── 4. Crear directorios de infra si no existen ─────────────────────────────
mkdir -p "$PROJECT_DIR/infra/nginx"
mkdir -p "$PROJECT_DIR/infra/certbot/conf"
mkdir -p "$PROJECT_DIR/infra/certbot/www"

# Nginx config mínima si no existe
if [[ ! -f "$PROJECT_DIR/infra/nginx/default.conf" ]]; then
    cat > "$PROJECT_DIR/infra/nginx/default.conf" << 'NGINX'
server {
    listen 80;
    server_name _;

    # Health check
    location /health { return 200 "ok"; }

    # ACME challenge para Certbot
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    # API backend
    location /fhir/    { proxy_pass http://backend:8000; proxy_set_header Host $host; }
    location /oauth/   { proxy_pass http://backend:8000; proxy_set_header Host $host; }
    location /admin/   { proxy_pass http://backend:8000; proxy_set_header Host $host; }
    location /.well-known/smart-configuration {
                         proxy_pass http://backend:8000; proxy_set_header Host $host; }

    # Frontend
    location / { proxy_pass http://frontend:3000; proxy_set_header Host $host; }
}
NGINX
    ok "nginx/default.conf creado"
fi

# ─── 5. Levantar servicios ────────────────────────────────────────────────────
echo "→ Levantando servicios..."
cd "$PROJECT_DIR"
docker compose pull --quiet
docker compose up -d --build

# ─── 6. Esperar healthchecks ──────────────────────────────────────────────────
echo "→ Esperando que PostgreSQL esté listo..."
RETRIES=20
until docker compose exec postgres pg_isready -U clinica_user -d clinica_db &>/dev/null || [[ $RETRIES -eq 0 ]]; do
    sleep 2; ((RETRIES--))
done
[[ $RETRIES -eq 0 ]] && err "PostgreSQL no respondió a tiempo"
ok "PostgreSQL listo"

echo "→ Esperando que Redis esté listo..."
RETRIES=10
REDIS_PASS=$(grep '^REDIS_PASSWORD=' "$ENV_FILE" | cut -d= -f2)
until docker compose exec redis redis-cli -a "$REDIS_PASS" ping 2>/dev/null | grep -q PONG || [[ $RETRIES -eq 0 ]]; do
    sleep 2; ((RETRIES--))
done
[[ $RETRIES -eq 0 ]] && err "Redis no respondió a tiempo"
ok "Redis listo"

# ─── 7. Resumen ───────────────────────────────────────────────────────────────
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║              Setup completado exitosamente                  ║"
echo "╠══════════════════════════════════════════════════════════════╣"
echo "║  Backend API:   http://localhost/fhir/R4/metadata           ║"
echo "║  SMART config:  http://localhost/.well-known/smart-configuration ║"
echo "║  Logs:          docker compose logs -f                      ║"
echo "║  Detener:       docker compose down                         ║"
if $PROD_MODE; then
echo "╠══════════════════════════════════════════════════════════════╣"
echo "║  PRODUCCIÓN: editá BASE_URL en .env con tu dominio          ║"
echo "║  SSL: docker compose run certbot certonly --webroot ...     ║"
fi
echo "╚══════════════════════════════════════════════════════════════╝"
