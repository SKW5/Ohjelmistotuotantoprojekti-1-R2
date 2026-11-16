-- Select the target database before running. Back up and stop writes first.
-- DDL commits implicitly. Keep existing utf8mb4 comparison rules.
SET @ddl = (SELECT CONCAT('ALTER DATABASE `', REPLACE(DATABASE(), '`', '``'), '` CHARACTER SET utf8mb4 COLLATE ', IF(DEFAULT_CHARACTER_SET_NAME='utf8mb4', DEFAULT_COLLATION_NAME, 'utf8mb4_unicode_ci')) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME=DATABASE());
PREPARE localization_ddl FROM @ddl;
EXECUTE localization_ddl;
DEALLOCATE PREPARE localization_ddl;
SET @ddl = (SELECT CONCAT('ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE ', IF(TABLE_COLLATION LIKE 'utf8mb4_%', TABLE_COLLATION, 'utf8mb4_unicode_ci')) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users');
PREPARE localization_ddl FROM @ddl;
EXECUTE localization_ddl;
DEALLOCATE PREPARE localization_ddl;
SET @ddl = (SELECT CONCAT('ALTER TABLE timetable_events CONVERT TO CHARACTER SET utf8mb4 COLLATE ', IF(TABLE_COLLATION LIKE 'utf8mb4_%', TABLE_COLLATION, 'utf8mb4_unicode_ci')) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='timetable_events');
PREPARE localization_ddl FROM @ddl;
EXECUTE localization_ddl;
DEALLOCATE PREPARE localization_ddl;
SET @ddl = (SELECT CONCAT('ALTER TABLE reminders CONVERT TO CHARACTER SET utf8mb4 COLLATE ', IF(TABLE_COLLATION LIKE 'utf8mb4_%', TABLE_COLLATION, 'utf8mb4_unicode_ci')) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='reminders');
PREPARE localization_ddl FROM @ddl;
EXECUTE localization_ddl;
DEALLOCATE PREPARE localization_ddl;
