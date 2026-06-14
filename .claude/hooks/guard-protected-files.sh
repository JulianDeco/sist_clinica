#!/usr/bin/env bash
# PreToolUse guard for ClinicaSaaS.
# Blocks edits to secrets (.env) and to ALREADY-APPLIED Flyway migrations.
# Rationale (docs/standards/03-database-standards.md): an applied migration is
# immutable — schema changes only ever go in a NEW V{N}__*.sql file. Editing a
# shipped migration silently diverges environments and breaks `flyway validate`.
#
# Reads the hook payload on stdin (JSON). Exit 2 = block with message on stderr.
# Any other non-fatal path exits 0 (allow).

set -euo pipefail

payload="$(cat)"

# Extract the target file path from common Edit/Write tool inputs.
file_path="$(printf '%s' "$payload" | python3 -c '
import sys, json
try:
    data = json.load(sys.stdin)
except Exception:
    print(""); sys.exit(0)
ti = data.get("tool_input", {}) or {}
print(ti.get("file_path") or ti.get("path") or "")
' 2>/dev/null || true)"

[ -z "$file_path" ] && exit 0

base="$(basename "$file_path")"

# 1) Secrets: block .env and env variants (but allow .env.example).
case "$base" in
  .env|.env.*|*.env)
    if [ "$base" != ".env.example" ]; then
      echo "BLOCKED: '$file_path' holds environment secrets. Edit .env.example instead, or change it outside Claude. (guard-protected-files.sh)" >&2
      exit 2
    fi
    ;;
esac

# 2) Applied Flyway migrations: block edits to existing files under db/migration/**.
#    Creating a NEW V{N}__*.sql is fine (the file won't exist yet, so this guard
#    only fires on edits to files already on disk).
if printf '%s' "$file_path" | grep -qE 'db/migration(-dev)?/V[0-9]+__.*\.sql$'; then
  if [ -f "$file_path" ]; then
    echo "BLOCKED: '$file_path' is an applied Flyway migration and is immutable. Create a NEW V{N}__description.sql for schema changes (docs/standards/03-database-standards.md §4). (guard-protected-files.sh)" >&2
    exit 2
  fi
fi

exit 0
