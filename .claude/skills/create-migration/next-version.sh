#!/usr/bin/env bash
# Computes the next Flyway version for ClinicaSaaS and prints the target file path.
# Scans BOTH migration locations so V{N} never collides (Flyway loads both).
# Usage: next-version.sh <slug> [--dev]
# Prints: the absolute path of the migration file to CREATE (does not create it).
# Exits non-zero if no slug is given or the computed file already exists.

set -euo pipefail

slug="${1:-}"
mode="${2:-}"

if [ -z "$slug" ]; then
  echo "ERROR: missing <slug>. Usage: next-version.sh <snake_case_description> [--dev]" >&2
  exit 1
fi

# Normalize slug to snake_case (lowercase, non-alnum -> _, collapse repeats, trim).
slug="$(printf '%s' "$slug" \
  | tr '[:upper:]' '[:lower:]' \
  | sed -E 's/[^a-z0-9]+/_/g; s/_+/_/g; s/^_//; s/_$//')"

repo_root="$(cd "$(dirname "$0")/../../.." && pwd)"
prod_dir="$repo_root/src/backend/src/main/resources/db/migration"
dev_dir="$repo_root/src/backend/src/main/resources/db/migration-dev"

# Find the highest V{N} across both locations.
max=0
for d in "$prod_dir" "$dev_dir"; do
  [ -d "$d" ] || continue
  while IFS= read -r f; do
    n="$(basename "$f" | sed -E 's/^V0*([0-9]+)__.*/\1/')"
    [[ "$n" =~ ^[0-9]+$ ]] || continue
    (( n > max )) && max=$n
  done < <(find "$d" -maxdepth 1 -name 'V[0-9]*__*.sql' 2>/dev/null)
done

next=$(( max + 1 ))
padded="$(printf 'V%03d' "$next")"

if [ "$mode" = "--dev" ]; then
  target_dir="$dev_dir"
else
  target_dir="$prod_dir"
fi

target="$target_dir/${padded}__${slug}.sql"

if [ -e "$target" ]; then
  echo "ERROR: $target already exists — refusing to overwrite." >&2
  exit 2
fi

echo "$target"
