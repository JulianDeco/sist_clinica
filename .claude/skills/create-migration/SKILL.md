---
name: create-migration
description: Scaffold a new Flyway migration for Kuris with the correct V{N} number, naming convention, and a tenant-aware template. User-invoked via /create-migration.
disable-model-invocation: true
---

# create-migration

Scaffolds a new Flyway migration following Kuris database standards
(`docs/standards/03-database-standards.md`). Flyway is the **only** sanctioned
schema-change mechanism — never `ddl-auto=create/update`, never manual SQL.

## When to use
Invoked by the user as `/create-migration <description>` (e.g.
`/create-migration add_phone_to_patients`). Optionally target the dev-only
location with `--dev` for seed/fixture migrations.

## Rules this skill enforces
- **Immutability**: never edit an applied migration. This skill only ever creates
  a NEW file. (A PreToolUse hook also blocks edits to existing `V*.sql`.)
- **Numbering**: the next `V{N}` is `max(existing) + 1`, zero-padded to 3 digits
  (`V012`, `V013`, …), computed across BOTH `db/migration/` and `db/migration-dev/`
  so numbers never collide (Flyway loads both locations — see `application.yml`).
- **Naming**: `V{N}__snake_case_description.sql`.
- **Location**: production schema → `src/backend/src/main/resources/db/migration/`.
  Dev-only seed/fixture → `src/backend/src/main/resources/db/migration-dev/` (`--dev`).
- **Tenant-scoped tables** must include the standard columns
  (`tenant_id UUID NOT NULL REFERENCES tenants(id)`, audit columns, soft-delete),
  and indexes named `idx_{table}_{columns}`, uniques `uq_{table}_{columns}`.

## Procedure
1. Read the `<description>` argument; slugify to `snake_case`. If missing, ask for one.
2. Run the helper to compute the next number and the target path:
   ```bash
   bash .claude/skills/create-migration/next-version.sh "<slug>" [--dev]
   ```
   It prints the full path to create (and fails loudly if the file would collide).
3. Create the file at that path using the template below. Fill the WHY header — every
   migration states the business/technical reason (see existing V001–V011 for the house style).
4. Do NOT run Flyway automatically. Tell the user to apply it with the backend's normal
   startup or `mvn -pl src/backend flyway:migrate` once reviewed.
5. Remind: if this is a tenant-scoped table, run `multitenant-isolation-reviewer` over the
   repositories that will query it.

## Template (production migration)
```sql
-- V{N} — <short title> (T-XXX, spec §X)
-- WHY: <why this change exists — business rule or technical driver>

-- Example for a NEW tenant-scoped table (adapt or delete):
CREATE TABLE IF NOT EXISTS <table_name> (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    -- ... business columns ...
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_<table_name>_tenant_id ON <table_name>(tenant_id);
-- Composite index for the most common tenant-scoped lookup:
-- CREATE INDEX IF NOT EXISTS idx_<table_name>_tenant_<col> ON <table_name>(tenant_id, <col>);
```

## Notes
- The helper script never writes the migration; it only computes the safe path.
- For ALTER migrations, drop the CREATE template and write the `ALTER TABLE` directly,
  keeping the WHY header.
