# Database localization implementation report

Verified 2026-11-16.

## Findings and implementation

Local MariaDB 12.2.2 is reachable on port 3306. Read-only metadata inspection
found server, database, all three tables and all seven text columns already using
utf8mb4 with utf8mb4_uca1400_ai_ci. Docker CLI was unavailable; port 13307 was
closed. Compose targets MariaDB 11.4. The original schema and Compose inherited
server defaults. New installations now specify utf8mb4_unicode_ci, compatible
with both versions. Existing utf8mb4 collations are preserved by migration.

JDBC configuration is in Main.openDatabaseConnection (DB_URL/DB_USER/DB_PASSWORD).
Connector/J 3.5.6 successfully negotiated Unicode without URL changes.
eventRepository uses prepared setString/getString and SQL DATE/TIME values.
AddEvent and AddEventDialog now preserve entered titles/locations, including
surrounding spaces; blank titles remain invalid. Length validation counts Unicode
code points, matching VARCHAR character limits including supplementary characters.
The existing I18n bindings already localize calendar dates/times and dialog time
choices. No schema translation tables are required; see the plan for future
shared reference data. Current ER source is Documents/ER-diagram.md.

## Verification

- Separate database test_sprint6_localization was created with Latin-1 tables.
  Seeded user/event/reminder rows, ran migration, and inspected SHOW CREATE TABLE
  and row values. All tables became utf8mb4_unicode_ci; IDs, values, primary and
  unique keys, and three ON DELETE CASCADE foreign keys remained intact.
  Test database is retained for inspection. The application database was not migrated.
- Opt-in integration test uses actual AddEvent and eventRepository against local
  MariaDB, inserts its own user, saves and retrieves title
  `Meeting · Ääkköset · اجتماع · 授業` and location
  `  Helsinki · قاعة · 教室 🏫  `, and rolls back all inserted rows.
  Auto-increment counters can advance.
- Headless JavaFX builds the real TimetableView, waits for asynchronous loading,
  switches EN → FI → AR → JA, and asserts unchanged title/location labels plus
  localized calendar date and event time labels. This passed.
- Initial full test run: 68 tests, five UI errors caused by saved Arabic preference
  against English-label expectations. UI test fixture now saves/restores the
  preference and starts in English.

## Reproduce / manual checks

Back up first (CLI commands below use password prompts). Restore a dump to a
separate clone before migration; replace database names as appropriate. In the
MariaDB client:

```sql
USE test_sprint6_localization;
SOURCE Database/check-localization.sql;
SOURCE Database/migrations/006_utf8mb4.sql;
SOURCE Database/check-localization.sql;
SHOW CREATE TABLE users;
SHOW CREATE TABLE timetable_events;
SHOW CREATE TABLE reminders;
```

For a real restored clone, compare rows/counts and constraints before/after,
including unique username/email collisions under the target collation. DDL cannot
be rolled back; stop writes and retain a tested backup. Never disable constraints
or delete a Docker volume. No automatic production migration is performed.

```powershell
mariadb-dump -h 127.0.0.1 -u <admin> -p --single-transaction student_timetable > backup.sql
$env:DB_LOCALIZATION_TEST = 'true'
mvn test
mvn javafx:run
```

For desktop visual verification: sign in with a development account; add the
exact title/location above in the current week, restart and reopen it, switch
EN/FI/AR/JA, and check unchanged text, Arabic shaping/RTL, Japanese glyphs and
locale dates/times in the calendar and edit dialog. Headless assertions do not
verify desktop fonts, glyph appearance, clipping or bidirectional rendering.
Docker startup and migration on MariaDB 11.4 remain unverified locally.

Final full-suite rerun with DB_LOCALIZATION_TEST=true: **69 tests, 0 failures,
0 errors**, including the live MariaDB and headless JavaFX locale-switch test.
