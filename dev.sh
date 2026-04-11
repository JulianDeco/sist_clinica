#!/bin/bash
# dev.sh — Setup local para desarrollo y testing
# Solo levanta PostgreSQL + Redis (no necesita backend/frontend todavía).
# Genera .env si no existe y verifica la conexión.

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$PROJECT_DIR/.env"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
ok()   { echo -e "${GREEN}✓${NC} $*"; }
warn() { echo -e "${YELLOW}⚠${NC} $*"; }
err()  { echo -e "${RED}✗${NC} $*"; exit 1; }

# ─── 1. Verificar Docker ──────────────────────────────────────────────────────
command -v docker        &>/dev/null || err "Docker no instalado"
docker compose version   &>/dev/null || err "Docker Compose plugin no encontrado"
ok "Docker disponible"

# ─── 2. Generar .env si no existe ─────────────────────────────────────────────
if [[ -f "$ENV_FILE" ]]; then
    warn ".env ya existe — usando el existente"
else
    echo "→ Generando .env con secretos aleatorios..."

    gen_pass() { openssl rand -base64 32 | tr -d '/+=' | head -c 40; }
    gen_key()  { openssl rand -base64 48 | tr -d '/+=' | head -c 64; }

    cat > "$ENV_FILE" << EOF
# Generado por dev.sh — $(date -u +"%Y-%m-%dT%H:%M:%SZ")
# NO commitear este archivo

POSTGRES_USER=clinica_user
POSTGRES_PASSWORD=$(gen_pass)
POSTGRES_DB=clinica_db
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
DATABASE_URL=postgresql+asyncpg://clinica_user:$(grep POSTGRES_PASSWORD= "$ENV_FILE" 2>/dev/null | cut -d= -f2 || echo "PENDIENTE")@localhost:5432/clinica_db

REDIS_PASSWORD=$(gen_pass)
REDIS_URL=redis://:$(grep REDIS_PASSWORD= "$ENV_FILE" 2>/dev/null | cut -d= -f2 || echo "PENDIENTE")@localhost:6379/0

SECRET_KEY=$(gen_key)
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30
REFRESH_TOKEN_EXPIRE_DAYS=7

ENVIRONMENT=development
BASE_URL=http://localhost
NEXT_PUBLIC_API_URL=http://localhost
NEXT_PUBLIC_CLIENT_ID=clinica-frontend
EOF

    # Regenerar con los valores reales (la primera pasada no puede auto-referenciarse)
    POSTGRES_PASSWORD=$(openssl rand -base64 32 | tr -d '/+=' | head -c 40)
    REDIS_PASSWORD=$(openssl rand -base64 32 | tr -d '/+=' | head -c 40)
    SECRET_KEY=$(openssl rand -base64 48 | tr -d '/+=' | head -c 64)

    cat > "$ENV_FILE" << EOF
# Generado por dev.sh — $(date -u +"%Y-%m-%dT%H:%M:%SZ")
# NO commitear este archivo

POSTGRES_USER=clinica_user
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
POSTGRES_DB=clinica_db
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
DATABASE_URL=postgresql+asyncpg://clinica_user:${POSTGRES_PASSWORD}@localhost:5432/clinica_db

REDIS_PASSWORD=${REDIS_PASSWORD}
REDIS_URL=redis://:${REDIS_PASSWORD}@localhost:6379/0

SECRET_KEY=${SECRET_KEY}
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30
REFRESH_TOKEN_EXPIRE_DAYS=7

ENVIRONMENT=development
BASE_URL=http://localhost
NEXT_PUBLIC_API_URL=http://localhost
NEXT_PUBLIC_CLIENT_ID=clinica-frontend
EOF

    chmod 600 "$ENV_FILE"
    ok ".env generado (permisos 600)"
fi

# ─── 3. .gitignore ────────────────────────────────────────────────────────────
GITIGNORE="$PROJECT_DIR/.gitignore"
for entry in ".env" "*.pyc" "__pycache__/" ".next/" "node_modules/"; do
    grep -qxF "$entry" "$GITIGNORE" 2>/dev/null || echo "$entry" >> "$GITIGNORE"
done
ok ".gitignore actualizado"

# ─── 4. Levantar solo infra ───────────────────────────────────────────────────
echo "→ Levantando PostgreSQL y Redis..."
cd "$PROJECT_DIR"
set -a; source "$ENV_FILE"; set +a
docker compose -f docker-compose.dev.yml up -d

# ─── 5. Esperar PostgreSQL ────────────────────────────────────────────────────
echo "→ Esperando PostgreSQL..."
RETRIES=30
until docker compose -f docker-compose.dev.yml exec -T postgres \
    pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" &>/dev/null; do
    sleep 1; ((RETRIES--))
    [[ $RETRIES -eq 0 ]] && err "PostgreSQL no levantó en 30s — revisá: docker compose -f docker-compose.dev.yml logs postgres"
done
ok "PostgreSQL listo en localhost:5432"

# ─── 6. Esperar Redis ─────────────────────────────────────────────────────────
echo "→ Esperando Redis..."
RETRIES=15
until docker compose -f docker-compose.dev.yml exec -T redis \
    redis-cli -a "$REDIS_PASSWORD" ping 2>/dev/null | grep -q PONG; do
    sleep 1; ((RETRIES--))
    [[ $RETRIES -eq 0 ]] && err "Redis no levantó en 15s — revisá: docker compose -f docker-compose.dev.yml logs redis"
done
ok "Redis listo en localhost:6379"

# ─── 7. Verificar conexión real a BD ─────────────────────────────────────────
echo "→ Verificando conexión a la BD..."
docker compose -f docker-compose.dev.yml exec -T postgres \
    psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT version();" &>/dev/null \
    && ok "Conexión a PostgreSQL verificada" \
    || warn "No se pudo verificar la conexión — revisá los logs"

# ─── 8. Resumen ───────────────────────────────────────────────────────────────
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║              Entorno de desarrollo listo                    ║"
echo "╠══════════════════════════════════════════════════════════════╣"
printf "║  PostgreSQL:  postgresql://%s@localhost:5432/%s\n" "$POSTGRES_USER" "$POSTGRES_DB"
echo  "║  Redis:       redis://:****@localhost:6379/0                ║"
echo  "╠══════════════════════════════════════════════════════════════╣"
echo  "║  Credenciales completas en: .env                           ║"
echo  "║  Detener:  docker compose -f docker-compose.dev.yml down   ║"
echo  "║  Logs:     docker compose -f docker-compose.dev.yml logs -f ║"
echo  "╚══════════════════════════════════════════════════════════════╝"
