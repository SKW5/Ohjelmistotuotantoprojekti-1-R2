# Acceptance Test Plan – Student Timetable

| | |
| --- | --- |
| Project | Student Timetable (Ohjelmistotuotantoprojekti 1-R2) |
| Document | Acceptance Test Plan (Sprint 6) |
| Version / date | 1.0 / 17 November 2026 |
| Authors | [Team member names] |
| Status | Planned and designed only. The tests are not executed in Sprint 6. |

## 1. Purpose and scope

This plan defines when the Student Timetable application is accepted by the Product Owner. It lists the requirements, measurable acceptance criteria and example acceptance tests (functional, usability, performance and reliability), and maps every test to a requirement.

**In scope:** registration and login, visitor view, weekly timetable, event management, reminders, profile settings, localization (English, Finnish, Arabic with right-to-left layout, Japanese) and Unicode data storage in MariaDB.

**Sources:** Vision Statement, `README.md`, sprint review reports and `Documents/LOCALIZATION.md`. Requirement IDs below are derived from these documents.

## 2. Requirements

| ID | Requirement | Source |
| --- | --- | --- |
| REQ-01 | A user can register and log in with a username, email and password. | README |
| REQ-02 | A visitor can open the timetable without signing in. | README |
| REQ-03 | The user sees a weekly calendar with days and hours. | Vision Statement |
| REQ-04 | The user can add events with title, date, start and end time, location and colour. | Vision Statement |
| REQ-05 | The user can edit and delete events. | Vision Statement |
| REQ-06 | The user can turn reminders on or off and receives a reminder before an event. | Vision Statement |
| REQ-07 | The user can edit profile details (username, email, major). | README |
| REQ-08 | Data is stored in the database and passwords are stored as hashes. | README |
| REQ-09 | The user can switch the interface between English, Finnish, Arabic and Japanese, and the choice is remembered. | Sprint 5 |
| REQ-10 | The Arabic interface is mirrored right-to-left. | Sprint 5 |
| REQ-11 | Dates, times and numbers follow the selected language. | Sprint 5 |
| REQ-12 | Text in any language can be stored without corruption (UTF-8). | Sprint 6 |
| REQ-13 | The application is responsive and handles failures without crashing. | Sprint 6 |
| REQ-14 | The application is easy to learn without instructions. | Vision Statement |

### 2.1 User stories (Trello Product Backlog)

The acceptance tests are traced to the team's user stories. The technical requirements above are the detailed form of these stories.

| ID | User story | Requirements |
| --- | --- | --- |
| US-01 | As a student I want to use the application in my native language. | REQ-09, REQ-10, REQ-11, REQ-12 |
| US-02 | As a student I want to add my personal and school events so that I can schedule everything in one place. | REQ-04, REQ-08 |
| US-03 | As a student I want to see specific days' schedules so that I can plan ahead. | REQ-03 |
| US-04 | As a student I want to view my timetable so that I know when and where I need to be. | REQ-02, REQ-03 |
| US-05 | As a student I want to receive reminders for upcoming events and classes so that I remember them. | REQ-06 |
| US-06 | As a student I want to edit my events so that I can change them when plans change. | REQ-05 |

REQ-01, REQ-07, REQ-13 and REQ-14 (accounts, profile, quality) support these stories and are not separate user stories.

## 3. Acceptance criteria

Targets for REQ-13 and REQ-14 are proposals and should be confirmed with the Product Owner.

| ID | Criterion | Req |
| --- | --- | --- |
| AC-01 | Registration with valid data works and login with the same credentials succeeds. Empty fields, duplicate username or duplicate email are rejected with a message and no row is saved. Wrong credentials never log in. | REQ-01 |
| AC-02 | The visitor view opens without login and does not allow saving events. | REQ-02 |
| AC-03 | The weekly grid shows seven days and every saved event of the shown week in the correct day and time slot. | REQ-03 |
| AC-04 | An event is saved only if the title is non-empty (max 100 characters), date and times are set, the end time is after the start time and the location is at most 100 characters. Invalid input is rejected with a message. | REQ-04 |
| AC-05 | Edited values and deletions are visible immediately and still correct after restarting the application. | REQ-05, REQ-08 |
| AC-06 | With reminders on, one reminder is shown for an event starting within 15 minutes, and not shown twice. With reminders off, none is shown. | REQ-06 |
| AC-07 | Changed username, email and major are saved and visible after logging in again. | REQ-07 |
| AC-08 | No password is stored in plain text, and all data is present after restart. | REQ-08 |
| AC-09 | All visible text changes immediately when the language is changed, typed form input is kept, and the language is restored after restart. | REQ-09 |
| AC-10 | In Arabic all views and dialogs are right-to-left and the calendar columns are mirrored. English restores left-to-right. | REQ-10 |
| AC-11 | Weekday names, dates, times and digits follow the selected language. Stored date and time values do not change. | REQ-11 |
| AC-12 | The title `Meeting · Ääkköset · اجتماع · 授業` is saved and read back identically. | REQ-12 |
| AC-13 | Login, saving an event and loading a week take 1 s or less with a local database. If the database is unavailable, an error message is shown and the application does not crash. | REQ-13 |
| AC-14 | At least 4 of 5 new users complete the core tasks (register, add an event, edit an event) without help. | REQ-14 |

## 4. Test environment

Windows reference machine, JDK 21, Maven, MariaDB 11.4 (local or Docker Compose), a test database created from `Database/create.sql`, and fonts that cover Arabic and Japanese. Test users: `student1` (with events) and `student2` (empty). The automated suite (`mvn clean test`) is used for regression.

## 5. Example test cases

### 5.1 Functional tests

| ID | Req | Steps | Expected result |
| --- | --- | --- | --- |
| FT-01 | REQ-01 | Register `newuser`, log out, log in with the same credentials. | Account is created and login opens the timetable. |
| FT-02 | REQ-01 | Register with an existing username, an existing email and empty fields. Log in with a wrong password. | All attempts fail with a message. No duplicate row is created. |
| FT-03 | REQ-02 | Open the visitor view and try to add an event. | Timetable is shown. Saving is not possible. |
| FT-04 | REQ-03, REQ-04 | Add an event (title `Lecture`, today, 10:00–11:30, location `Room A`, green). Then try an empty title, end before start and a 101-character title. | The valid event appears in the correct slot in green. All invalid attempts are rejected and nothing is saved. |
| FT-05 | REQ-05, REQ-08 | Edit the event time, delete another event, restart the application and log in. | The edit is kept and the deleted event is gone. |
| FT-06 | REQ-06 | Create an event starting in 10 minutes. Test with reminders on and off. | One reminder with reminders on, none with reminders off. |
| FT-07 | REQ-07 | Change username, email and major in Settings, log out and log in. | New values are shown. A duplicate username is rejected. |

### 5.2 Localization tests

| ID | Req | Steps | Expected result |
| --- | --- | --- | --- |
| LT-01 | REQ-09 | Type an email on the registration view and switch EN → FI → AR → JA. Restart the application. | All text changes at once, the typed email stays, and the last language is restored. |
| LT-02 | REQ-10 | Select Arabic and open the header, timetable, event dialog and an alert. Switch back to English. | All are right-to-left with mirrored columns. English restores left-to-right. |
| LT-03 | REQ-11 | Compare dates, weekdays and times in all four languages and check the database values. | Display follows each language. Stored values are unchanged. |
| LT-04 | REQ-12 | Save an event with the title `Meeting · Ääkköset · اجتماع · 授業`. Read it in the UI and with `SELECT`. | The text is identical in both. |

### 5.3 Usability tests

Five students without prior knowledge of the application, think-aloud method.

| ID | Req | Task | Expected result |
| --- | --- | --- | --- |
| UT-01 | REQ-14 | Register, add an event for tomorrow with a colour, then change its time. | At least 4 of 5 succeed without help in 5 minutes. |
| UT-02 | REQ-09, REQ-14 | Change the interface to Finnish and back to a language you understand. | All 5 recover using the language names shown in their own scripts. |

### 5.4 Performance and reliability tests

| ID | Req | Steps | Expected result |
| --- | --- | --- | --- |
| PT-01 | REQ-13 | Repeat login, saving an event and loading a week 20 times each and record the times. | Each operation takes 1 s or less. |
| RT-01 | REQ-13 | Stop the database, try to save an event, restart the database and try again. | An error message is shown and the application does not crash. Saving works again afterwards. |
| RT-02 | all | Run `mvn clean test`. | 0 failures and 0 errors. |

## 6. Coverage matrix

| User story | Requirement | Criteria | Tests |
| --- | --- | --- | --- |
| US-01 Native language | REQ-09 Language switching | AC-09 | LT-01, UT-02 |
| US-01 Native language | REQ-10 RTL layout | AC-10 | LT-02 |
| US-01 Native language | REQ-11 Locale formats | AC-11 | LT-03 |
| US-01 Native language | REQ-12 Unicode data | AC-12 | LT-04 |
| US-02 Add events | REQ-04 Add event | AC-04 | FT-04, UT-01 |
| US-02 Add events | REQ-08 Persistence and security | AC-05, AC-08 | FT-05, RT-02 |
| US-03 Specific days | REQ-03 Weekly calendar | AC-03 | FT-04, FT-05 |
| US-04 View timetable | REQ-02 Visitor view | AC-02 | FT-03 |
| US-04 View timetable | REQ-03 Weekly calendar | AC-03 | FT-04 |
| US-05 Reminders | REQ-06 Reminders | AC-06 | FT-06 |
| US-06 Edit events | REQ-05 Edit and delete | AC-05 | FT-05, UT-01 |
| Supporting | REQ-01 Registration and login | AC-01 | FT-01, FT-02, UT-01 |
| Supporting | REQ-07 Profile | AC-07 | FT-07 |
| Supporting | REQ-13 Performance and reliability | AC-13 | PT-01, RT-01, RT-02 |
| Supporting | REQ-14 Usability | AC-14 | UT-01, UT-02 |
