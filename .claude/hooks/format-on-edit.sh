#!/usr/bin/env bash
# PostToolUse formatter for ClinicaSaaS.
# After an Edit/Write, formats the touched file:
#   *.java                      -> mvn spotless:apply (backend) — if Spotless is configured
#   *.ts / *.html / *.scss      -> pnpm prettier --write (frontend) — if Prettier is available
#
# Best-effort and non-blocking: if the formatter isn't installed/configured yet,
# it exits 0 silently. Wire up Spotless (pom.xml) and Prettier (frontend) to activate.

set -uo pipefail

payload="$(cat)"
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
[ -f "$file_path" ] || exit 0

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"

case "$file_path" in
  *.java)
    backend="$repo_root/src/backend"
    if grep -q "spotless-maven-plugin" "$backend/pom.xml" 2>/dev/null; then
      ( cd "$backend" && mvn -q spotless:apply -DspotlessFiles="$file_path" >/dev/null 2>&1 ) || true
    fi
    ;;
  *.ts|*.html|*.scss|*.css)
    frontend="$repo_root/src/frontend"
    if [ -f "$frontend/package.json" ] && command -v pnpm >/dev/null 2>&1; then
      ( cd "$frontend" && pnpm exec prettier --write "$file_path" >/dev/null 2>&1 ) || true
    fi
    ;;
esac

exit 0
