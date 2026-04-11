#!/bin/bash
# agents.sh — Lanzar agentes DEVELOPER + REVIEWER en paralelo
# Uso: ./agents.sh

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SESSION="clinica-agents"
DEV_PROMPT_FILE="$PROJECT_DIR/.claude/agents/developer-prompt.md"
REV_PROMPT_FILE="$PROJECT_DIR/.claude/agents/reviewer-prompt.md"

# ─── 1. Verificar dependencias ────────────────────────────────────────────────
echo "→ Verificando dependencias..."

if ! command -v tmux &>/dev/null; then
    echo "  tmux no está instalado. Instalando..."
    sudo apt-get update -qq && sudo apt-get install -y tmux
    echo "  ✓ tmux instalado"
else
    echo "  ✓ tmux $(tmux -V | cut -d' ' -f2)"
fi

if ! command -v claude &>/dev/null; then
    echo "  ✗ Error: 'claude' no encontrado en PATH"
    echo "    Instalá Claude Code con: npm install -g @anthropic-ai/claude-code"
    exit 1
fi
echo "  ✓ claude disponible"

if ! command -v gh &>/dev/null; then
    echo "  ⚠ Advertencia: 'gh' (GitHub CLI) no encontrado — el reviewer no podrá crear reviews"
    echo "    Instalá con: sudo apt install gh  y luego: gh auth login"
fi

# ─── 2. Limpiar sesión anterior si existe ─────────────────────────────────────
tmux kill-session -t "$SESSION" 2>/dev/null && echo "→ Sesión anterior cerrada" || true

# ─── 3. Crear sesión tmux con dos paneles ─────────────────────────────────────
echo "→ Creando sesión tmux '$SESSION'..."

# Panel izquierdo = DEVELOPER
tmux new-session -d -s "$SESSION" -c "$PROJECT_DIR" -x "$(tput cols)" -y "$(tput lines)"

# Panel derecho = REVIEWER
tmux split-window -h -t "$SESSION" -c "$PROJECT_DIR"

# Títulos de paneles (requiere tmux ≥ 2.3)
tmux set-option -t "$SESSION" pane-border-status top 2>/dev/null || true
tmux select-pane -t "$SESSION:0.0" -T "DEVELOPER" 2>/dev/null || true
tmux select-pane -t "$SESSION:0.1" -T "REVIEWER"  2>/dev/null || true

# ─── 4. Iniciar claude en cada panel ──────────────────────────────────────────
echo "→ Iniciando Claude en panel DEVELOPER..."
tmux send-keys -t "$SESSION:0.0" "claude" Enter

echo "→ Iniciando Claude en panel REVIEWER..."
tmux send-keys -t "$SESSION:0.1" "claude" Enter

# ─── 5. Esperar que Claude inicialice ─────────────────────────────────────────
echo "→ Esperando inicialización de Claude (5 segundos)..."
sleep 5

# ─── 6. Enviar prompts de rol ─────────────────────────────────────────────────
# Convierte el archivo markdown a una sola línea para tmux send-keys
DEV_MSG="Leé el archivo .claude/agents/developer-prompt.md y seguí exactamente esas instrucciones como tu rol en este proyecto."
REV_MSG="Leé el archivo .claude/agents/reviewer-prompt.md y seguí exactamente esas instrucciones como tu rol en este proyecto."

echo "→ Asignando rol DEVELOPER..."
tmux send-keys -t "$SESSION:0.0" "$DEV_MSG" Enter

sleep 1

echo "→ Asignando rol REVIEWER..."
tmux send-keys -t "$SESSION:0.1" "$REV_MSG" Enter

# ─── 7. Mostrar instrucciones y conectar ──────────────────────────────────────
echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║           AGENTES INICIADOS — Sistema Médico FHIR           ║"
echo "╠══════════════════════════════════════════════════════════════╣"
echo "║  Panel IZQUIERDO │ DEVELOPER → implementa + commit + PR     ║"
echo "║  Panel DERECHO   │ REVIEWER  → testea + aprueba/rechaza     ║"
echo "╠══════════════════════════════════════════════════════════════╣"
echo "║  Atajos tmux:                                                ║"
echo "║    Ctrl+B → flechas    Moverse entre paneles                 ║"
echo "║    Ctrl+B → Z          Zoom en panel actual                  ║"
echo "║    Ctrl+B → D          Desconectarse (agentes siguen corriendo) ║"
echo "║    tmux attach -t $SESSION   Reconectarse                   ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

tmux attach-session -t "$SESSION"
