# Opiskelijan lukujärjestys

JavaFX-työpöytäsovellus opiskelijan viikoittaisen lukujärjestyksen ja käyttäjäprofiilin hallintaan. Sovellus käyttää MariaDB-tietokantaa käyttäjien ja tapahtumien tallentamiseen. Käyttöliittymän kielet ovat englanti, suomi, arabia (RTL) ja japani. Kieltä voi vaihtaa kirjautumisnäkymissä, yläpalkissa ja asetuksissa. Katso [lokalisoinnin dokumentaatio](Documents/LOCALIZATION.md) ja [käännettävien tekstien luettelo](Documents/LOCALIZATION-STRINGS.md).

## Toiminnot

- Käyttäjätilin luominen ja kirjautuminen
- Viikon tapahtumien tarkastelu kalenterinäkymässä
- Tapahtuman lisääminen kirjautuneelle käyttäjälle
- Käyttäjäprofiilin käyttäjänimen, sähköpostin ja pääaineen muokkaaminen
- Vierailijanäkymän käyttö ilman kirjautumista

## Tekniikat

- Java 21 ja JavaFX
- MariaDB
- Maven
- JUnit 5 -yksikkötestit
- Docker Compose -käynnistysvaihtoehto

## Vaatimukset

Paikalliseen käynnistykseen tarvitaan JDK 21, Maven ja MariaDB. Docker-käynnistykseen tarvitaan Docker ja Docker Compose. Graafisen käyttöliittymän näyttämiseen Docker-kontista tarvitaan lisäksi toimiva X11-palvelin.

## Käynnistä paikallisesti

1. Käynnistä MariaDB ja luo tietokantarakenne suorittamalla projektin juuressa `Database/create.sql`.
2. Käynnistä sovellus:

   ```bash
   mvn javafx:run
   ```

Oletuksena sovellus yhdistää osoitteeseen `jdbc:mariadb://localhost:3306/student_timetable` tunnuksilla `student` / `student`. Yhteyden voi määrittää ympäristömuuttujilla:

| Muuttuja | Oletusarvo |
| --- | --- |
| `DB_URL` | `jdbc:mariadb://localhost:3306/student_timetable` |
| `DB_USER` | `student` |
| `DB_PASSWORD` | `student` |

Vaihda oletustunnukset, jos tietokantasi käyttää muita arvoja.

### IntelliJ IDEA

Avaa projekti Maven-projektina, odota riippuvuuksien latautumista ja käynnistä Maven-tehtävä `javafx:run`.

## Testit

Suorita yksikkötestit projektin juurihakemistossa:

```bash
mvn test
```

Testikattavuusraportti muodostuu polkuun `target/site/jacoco/index.html`.

## Käynnistä Dockerilla Windowsissa

Projektin `docker-compose.yml` käynnistää JavaFX-sovelluksen ja MariaDB:n. Tietokanta alustetaan ensimmäisellä käynnistyskerralla `Database/create.sql`-tiedoston avulla, ja tiedot säilyvät Docker-volyymissa.

Graafisen näkymän välittämiseen tarvitaan Docker Desktopin lisäksi Xming tai muu X11-palvelin. Määritä Xming kuuntelemaan X11-yhteyksiä ja salli yhteydet Docker Desktopista Windowsin palomuurissa. Älä avaa X11-palvelinta julkiseen verkkoon.

Luo projektin juureen `.env`-tiedosto esimerkin pohjalta:

```powershell
Copy-Item .env.example .env
```

Käynnistä Xming ja sitten sovellus sekä tietokanta:

```powershell
docker compose up --build
```

Pysäytä palvelut painamalla `Ctrl+C`. Tietokannan volyymi säilyy palveluiden pysäyttämisen jälkeen. Sen poistaminen poistaa myös tietokantaan tallennetut tiedot.

## Docker Hub -kuvan käyttäminen

Docker Hub -kuva sisältää JavaFX-sovelluksen; tietokanta käynnistetään tämän repositorion Compose-määrityksellä. Kloonaa repo, käynnistä Xming (tai muu X11-palvelin), luo `.env` yllä olevien ohjeiden mukaan ja suorita projektin juuressa:

```powershell
docker compose pull application
docker compose up -d --no-build
docker compose logs -f application
```

Uusimman kuvan hakeminen myöhemmin:

```powershell
docker compose pull application
docker compose up -d --no-build
```

Kuvan ylläpitäjä voi rakentaa ja julkaista päivityksen Docker Hubiin:

```powershell
docker build -t rthless/ohjelmistotuotantoprojekti-1-r2:latest .
docker push rthless/ohjelmistotuotantoprojekti-1-r2:latest
```

JavaFX-ikkunan näyttämiseen tarvitaan toimiva X11-yhteys. Älä avaa Xmingiä julkiseen verkkoon.


## Projektin rakenne

- `src/main/java/.../ui` sisältää JavaFX-näkymät ja tapahtumien lisäysikkunan.
- `src/main/java/.../service` sisältää sovelluksen toimintalogiikan.
- `src/main/java/.../repository` sisältää tietokantakyselyt.
- `src/main/java/.../model` sisältää käyttäjä- ja tapahtumatietojen mallit.
- `src/test/java` sisältää yksikkö- ja käyttöliittymätestit.
- `Database/create.sql` luo tietokannan taulut.

## Database localization (Sprint 6)

New installations explicitly use `utf8mb4`. Existing volumes are not reinitialized:
back up and test [migration 006](Database/migrations/006_utf8mb4.sql) on a restored
clone first. Never delete the volume to migrate. See the
[plan](docs/database-localization-plan.md),
[implementation and verification report](docs/database-localization-report.md),
and [current ER diagram](Documents/ER-diagram.md).

Event titles and locations are saved as entered, including whitespace. Dates and
times stay SQL DATE/TIME and display using the selected UI locale. MariaDB
Connector/J negotiates Unicode; no MySQL-only `useUnicode` URL flags are needed.

Opt-in real database test (creates temporary rows and rolls them back):

```powershell
$env:DB_LOCALIZATION_TEST = 'true'
mvn '-Dtest=UserInterfaceTest#multilingualDatabaseRoundTripAndLocaleSwitch' test
```

Uses the same DB_URL, DB_USER and DB_PASSWORD defaults as the application. Run only
against a development database: rollback removes rows but auto-increment IDs advance.
