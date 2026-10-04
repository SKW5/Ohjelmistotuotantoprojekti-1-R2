# Student Timetable JavaFX

Yksinkertainen frontend.

## Käynnistys

Tarvitset JDK 21:n ja Mavenin:

```bash
mvn javafx:run
```

### IntelliJ IDEA:ssa

1. Avaa kansio Maven-projektina.
2. Odota, että riippuvuudet latautuvat.
3. Käynnistä terminaalissa mvn javafx:run

**Login-toiminto on tällä hetkellä vain placeholder.**

## Docker ja Xming Windowsissa

Tarvitset Docker Desktopin Linux-kontteja varten ja Xmingin. Xmingin täytyy
kuunnella X11-yhteyksiä TCP-portissa 6000, ja Windowsin palomuurin pitää sallia
Docker Desktopin yhteydet. Älä avaa Xmingiä julkiseen verkkoon.

Kopioi esimerkkiasetukset `.env`-tiedostoksi PowerShellissä:

```powershell
Copy-Item .env.example .env
```

Käynnistä Xming ja sitten sovellus sekä MariaDB:

```powershell
docker compose up --build
```

Compose alustaa tietokannan `Database/create.sql`-tiedostosta ensimmäisellä
käynnistyskerralla ja säilyttää tietokantatiedot Docker-volyymissa. Pysäytä
palvelut painamalla `Ctrl+C`.

## Jenkins

Jenkins ajaa Maven-buildin ja testit headless-tilassa. Älä käynnistä JavaFX-
ikkunaa Jenkins-palvelusta; käytä Xmingiä paikalliseen Docker-käyttöön.