# Database schema (Sprint 6)

All new tables use `utf8mb4 / utf8mb4_unicode_ci`. Migrated utf8mb4 tables retain
existing collations. Dates/times remain typed SQL values; no localized strings or
translation columns are stored. This is the maintained ER diagram; the old PNG is
an earlier snapshot.

```mermaid
erDiagram
    users ||--o{ timetable_events : owns
    users ||--o{ reminders : receives
    timetable_events ||--o{ reminders : triggers
    users {
        INT user_id PK
        VARCHAR_50 username UK
        VARCHAR_100 email UK
        VARCHAR_255 password_hash
        VARCHAR_100 major "nullable free text"
    }
    timetable_events {
        INT event_id PK
        INT user_id FK
        VARCHAR_100 title
        DATE event_date
        TIME start_time
        TIME end_time
        VARCHAR_100 location "nullable free text"
        VARCHAR_20 color "default blue"
    }
    reminders {
        INT reminder_id PK
        INT user_id FK
        INT event_id FK
        INT reminder_time
    }
```

All three foreign keys use ON DELETE CASCADE. Text lengths, nullability, keys and
relationships are unchanged by localization.
