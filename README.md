# Student Timetable

Student Timetable is a Java desktop application for managing a student's weekly schedule and basic profile information. It is intended for students who want to view their timetable in one place, add personal events, and receive reminders for upcoming events. Visitors can open the timetable without signing in; registered users can save and manage their own events and profile.

The project goal is to provide a straightforward, locally runnable timetable with persistent MariaDB storage, account registration and login, and a JavaFX graphical interface.

## Features

- Register and sign in with an account.
- View a weekly calendar and add events with a title, date, start and end times, location, and color.
- Edit profile details including username, email, and major.
- Use the visitor view without signing in.
- Enable or disable in-app reminders for upcoming events.
- Persist user, timetable, and reminder data in MariaDB.

## Technology stack and dependencies

| Component | Version / details |
| --- | --- |
| Java runtime and compiler target | JDK 21 |
| Build | Apache Maven (project uses Maven; Maven Wrapper is not included) |
| UI | JavaFX Controls 21.0.6 |
| Database | MariaDB; Compose image `mariadb:11.4` |
| JDBC driver | MariaDB Java Client 3.5.6 |
| Password hashing | Favre BCrypt 0.10.2 |
| Unit and UI test framework | JUnit Jupiter 6.1.3 |
| Headless JavaFX test support | TestFX OpenJFX Monocle 21.0.2 |
| Coverage | JaCoCo Maven Plugin 0.8.15 |
| Container support | Docker and Docker Compose; Dockerfile uses Maven 3.9 with Eclipse Temurin 21 |
| CI configuration | Jenkins pipeline in `Jenkinsfile` |

The JavaFX styles are in `src/main/resources/styles.css`; the bundled typeface files are in `src/main/resources/Fonts/`. The interface currently uses English text. There are no localized resource bundles in the repository.

## Architecture and development methodology

The application uses a layered design with a JavaFX presentation layer and a small set of service and persistence classes. `Main` is the JavaFX entry point and connects the views to services and repositories. UI views collect user actions and render application state; services implement application operations; repositories issue JDBC queries; model classes represent users, profiles, and events.

| Layer | Location | Responsibility |
| --- | --- | --- |
| Presentation | `src/main/java/com/example/timetable/ui/` | Landing, login, registration, timetable, settings, header, and add-event UI |
| Application services | `src/main/java/com/example/timetable/service/` | Registration, event creation, password hashing helpers, and event reminders |
| Persistence | `src/main/java/com/example/timetable/repository/` | MariaDB access for accounts, profiles, login, and timetable events |
| Domain models | `src/main/java/com/example/timetable/model/` | User, profile, and timetable event data |
| Application bootstrap | `src/main/java/com/example/timetable/Main.java` | JavaFX startup, dependency wiring, database connection, navigation, and reminder timer |

Persistence is implemented with JDBC and SQL rather than an ORM. Database connection settings are read from `DB_URL`, `DB_USER`, and `DB_PASSWORD`; defaults are provided for local development. Passwords are stored as BCrypt hashes.

The database schema is initialized by [`Database/create.sql`](Database/create.sql). It defines `users`, `timetable_events`, and `reminders`. Events belong to a user; user deletion cascades to that user's events and reminders, and event deletion cascades to its reminders. The ER diagram and use-case diagram are available in [`Documents/`](Documents/README.md): [ER diagram](Documents/ER-diagram.png) and [use-case diagram](Documents/projekti-usecase.png). Project vision and sprint review documents are also included there.

Development artifacts show an iterative, sprint-based team process, with sprint review reports and a product vision document under `Documents/`. The Jenkins pipeline checks out the main branch, builds, runs tests, and publishes test and JaCoCo reports. It currently uses Windows `bat` steps, so the configured Jenkins agent needs to support those commands.

## Functional testing and verification

Tests are located in `src/test/java/` and use JUnit Jupiter. The suite covers model behavior, password utility behavior, registration and login, repository operations, event creation, reminders, application startup, and JavaFX UI flows. JavaFX UI tests use TestFX Monocle support.

Run the verification steps from the repository root:

```bash
mvn clean test
```

Maven's Surefire test reports are written under `target/surefire-reports/`. JaCoCo is configured to create an HTML coverage report at `target/site/jacoco/index.html` during the test phase. Repository tests that access MariaDB require a running database with the expected schema and credentials; configure `DB_URL`, `DB_USER`, and `DB_PASSWORD` for that test database before running them. The Docker Compose database can be used for this purpose.

## Setup and execution

### Requirements

- JDK 21
- Maven 3.9 or compatible
- MariaDB 11.4 or compatible for local database use
- Docker Engine and the Compose plugin for the container setup
- A graphical desktop session for normal JavaFX execution

### Local setup

1. Start MariaDB and create the schema. For example, with a local MariaDB client:

   ```bash
   mariadb -u root -p < Database/create.sql
   ```

2. Ensure the database account matches the application's defaults (`student` / `student`), or provide your own values through environment variables. The default JDBC URL is `jdbc:mariadb://localhost:3306/student_timetable`.

   ```bash
   export DB_URL='jdbc:mariadb://localhost:3306/student_timetable'
   export DB_USER='student'
   export DB_PASSWORD='student'
   ```

   In PowerShell, use `$env:DB_URL`, `$env:DB_USER`, and `$env:DB_PASSWORD` instead of `export`.

3. Download dependencies and launch the JavaFX application:

   ```bash
   mvn clean javafx:run
   ```

To build without launching the UI, run `mvn package`. On first launch the database must be reachable. The application reads these connection variables:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:mariadb://localhost:3306/student_timetable` |
| `DB_USER` | `student` |
| `DB_PASSWORD` | `student` |

### Docker Compose

The Compose configuration starts MariaDB and the application. MariaDB is exposed on host port `3307` and initialized from `Database/create.sql` on first creation of its data volume. The app connects to the database over the Compose network. JavaFX also needs a working display connection; the Compose file currently mounts the host X11 socket and an Xauthority file, so it is intended for a Linux host with X11 configured.

Set `XAUTH_FILE` to the path of a valid Xauthority file if needed, then start the services:

```bash
export XAUTH_FILE="$HOME/.Xauthority"
docker compose up --build
```

Set `DB_PASSWORD` (and optionally `DB_ROOT_PASSWORD`) in the environment before startup to override the local defaults. Stop with `Ctrl+C`; to start in the background use `docker compose up --build -d`, inspect logs with `docker compose logs -f application`, and stop containers with `docker compose down`. Database contents persist in the `database-data` volume. To delete the stored database as well, run `docker compose down -v`.

### Useful commands

```bash
mvn clean test       # run tests and generate JaCoCo coverage
mvn package          # compile and package the application
mvn javafx:run       # launch the application
docker compose up --build
```

## Repository map

```text
Database/                  Database initialization and maintenance SQL
Documents/                 Diagrams, vision statement, and sprint reviews
src/main/java/.../model/   Domain data classes
src/main/java/.../repository/ JDBC repositories
src/main/java/.../service/ Application services
src/main/java/.../ui/      JavaFX screens and dialogs
src/main/resources/        CSS and bundled fonts
src/test/java/             Unit, repository, and UI tests
```
