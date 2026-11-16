# Database localization plan

Scope: keep user text in its original language, support all Unicode characters,
and format DATE/TIME values only at the UI boundary. Languages: en-GB, fi-FI,
ar-EG, ja-JP. Do not translate event titles, locations, usernames or free-text majors.

1. Audit server, schema, table and column encodings with `Database/check-localization.sql`.
2. Explicit utf8mb4 / utf8mb4_unicode_ci defaults for new installations and Compose.
3. Back up existing databases; restore to a separate database and run
   `Database/migrations/006_utf8mb4.sql` with that database selected. Stop writes
   during migration. DDL commits implicitly; restore the backup if recovery is needed.
4. Compare row values/counts, SHOW CREATE TABLE, primary/unique/foreign keys and
   cascade behavior before production. Converting legacy collations may cause
   unique username/email collisions: resolve on the clone, never drop unique keys.
   Existing utf8mb4 table/database collations are retained. Review separately any
   customized column collations: CONVERT applies the table collation to text columns.
5. Verify service/repository round trip and EN → FI → AR → JA UI binding updates.

There are no shared course, building or major reference tables. `major` and
`location` are user text. Color codes are stable identifiers translated by resource
bundles. If shared courses/buildings are introduced, give each a stable ID and a
translation table `(reference_id, locale, name)` with a composite primary key and
foreign key to the parent; select the requested locale with English fallback.
Do not duplicate whole events per language. No translation tables are needed now.
