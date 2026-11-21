# Wann passt es?

Lokale Webanwendung, die für einen Ort, eine Outdoor-Aktivität, einen Zeitraum, eine
Mindestdauer und persönliche Wetterwünsche passende Zeitfenster berechnet.
Wiederverwendbare Suchprofile lassen sich speichern, verwenden, bearbeiten und löschen.

Wetter- und Ortsdaten stammen von [Open-Meteo](https://open-meteo.com/) (Ortsdaten basierend auf GeoNames). Für die Nutzung ist daher eine Internetverbindung nötig.

## Schnellstart

Voraussetzung: Java 21 (JDK). Maven muss nicht installiert sein, das Projekt bringt den Maven-Wrapper mit.

```bash
./mvnw spring-boot:run
```

Danach im Browser [http://localhost:8080](http://localhost:8080) öffnen. Alternativ `WannPasstEsApplication` in IntelliJ starten.

Die Anwendung bitte aus dem Projektverzeichnis starten: Die lokale Datenbank liegt relativ zum Arbeitsverzeichnis unter `./data/`.

## Tests

```bash
./mvnw test
```

Die Tests verwenden eine eigene In-Memory-H2-Datenbank und simulierte API-Antworten, nicht die Live-API.

Zwei Demo-Testklassen sind standardmäßig deaktiviert und werden nur über System-Properties eingeschaltet:

| Testklasse | Aktivierung | Hinweis |
| --- | --- | --- |
| `ProfileBrowserTests` | `-Ddemo.browser=true` | Benötigt Node.js und Playwright (Chromium); führt `src/test/browser/profile-flow.mjs` aus |
| `ProfilePerformanceTests` | `-Ddemo.performance=true` | Grenzwert über `-Ddemo.maxP95Millis` (Standard 1000) |

Das Browser-Szenario prüft Speichern, Bearbeiten, Verwenden und Löschen eines Profils, denselben Ablauf ohne JavaScript sowie bei 360 px und 1280 px Breite, dass keine Seite horizontal scrollt und das Ergebnis im 800 px hohen Fenster sichtbar ist. Liegt Playwright nicht im Projektverzeichnis, kann der Pfad über `PLAYWRIGHT_MODULE` gesetzt werden.

## Funktionen

- **Ortssuche** (`/locations`): Suche ab zwei Zeichen, bis zu fünf Treffer, deutsche Ortsnamen. Die Suche lässt sich aus der Planung oder aus einem Profil starten; der gewählte Ort übernimmt Zeitraum, Aktivität, Mindestdauer und Wetterwünsche.
- **Planung** (`/`): Zeitraum von heute bis heute + 6 Tage (in der Zeitzone des Ortes), Aktivität, Mindestdauer sowie Wünsche zu Temperatur, Wind und Niederschlag. Ergebnis sind zusammenhängende passende Zeitfenster in Ortszeit, chronologisch sortiert und jeweils mit Datum, Uhrzeit und Dauer. Ohne Datumsangabe wird der heutige Tag geprüft.
- **Suchprofile** (`/profiles`): aktuelle Auswahl unter einem eindeutigen Namen speichern, gespeichertes Profil verwenden, bearbeiten (Name, Ort, Aktivität, Mindestdauer, alle Wetterwünsche) und löschen. Die Übersicht zeigt den Ortsnamen; bei älteren Profilen ohne gespeicherten Namen die Orts-ID.
- **Fehlerseiten**: ungültige Parameter, unbekannte Adressen und unerwartete Fehler enden auf einer gestalteten deutschsprachigen Seite mit Weg zurück zur Planung (HTTP 400, 404, 405 bzw. 500).

### Wetterwünsche (vorläufige Produktparameter)

Grenzen sind eingeschlossen; `ANY` bedeutet keine Grenze für das jeweilige Kriterium.

| Auswahl | Bedeutung |
| --- | --- |
| Temperatur `COOL` / `MILD` / `WARM` | 5–15 °C / 15–25 °C / 20–30 °C |
| Wind `WEAK` / `MODERATE` | max. 15 km/h / max. 30 km/h |
| Niederschlag `DRY` / `LIGHT_DRIZZLE` / `LIGHT_RAIN` | max. 0,0 / 0,2 / 1,0 mm pro Stunde |

Die Aktivität (Spazieren, Laufen, Radfahren, Wandern) ist eine Bezeichnung und setzt keine automatischen Wetterregeln. Der von Open-Meteo gelieferte `weatherCode` wird geladen, aber noch nicht ausgewertet.

### Zeitfensterberechnung

`TimeWindowService` betrachtet benachbarte Stundenwerte im Abstand von genau einer Stunde. Temperatur und Wind müssen an beiden Intervall-Endpunkten passen; der Niederschlag wird vom Endpunkt genommen, weil dieser Wert die vorausgegangene Stunde beschreibt. Passende Intervalle werden zu Zeitfenstern zusammengefasst.

Der gesamte gewählte Zeitraum wird als eine durchgehende Stundenreihe bewertet. Ein Fenster, das ohne Lücke über Mitternacht reicht, bleibt deshalb ein Fenster; die Mindestdauer bezieht sich auf dieses zusammenhängende Fenster. Vergangene Stunden werden ignoriert. Das ist eine stündliche Näherung, keine Garantie für die Bedingungen zwischen den Messpunkten.

### URL-Parameter

| Seite | Parameter |
| --- | --- |
| `GET /` | `locationId`, `startDate`, `endDate` (ISO, `jjjj-mm-tt`), `activity`, `minDurationMinutes`, `temperaturePreference`, `windPreference`, `precipitationPreference` |
| `GET /` (alt) | `searchDate` durchsucht weiterhin genau diesen einen Tag, wenn kein `startDate` angegeben ist |
| `GET /locations` | `query` sowie optional `profileId` und die Planungsparameter, die beim Ortwechsel erhalten bleiben |
| `GET/POST /profiles` | Übersicht bzw. Speichern der aktuellen Auswahl |
| `GET/POST /profiles/{id}/edit` | Profil bearbeiten |
| `POST /profiles/delete` | Profil löschen |

## Gestaltung

Das Erscheinungsbild ist in `DESIGN.md` festgehalten: weiße Seite, eine
Akzentfarbe (gebranntes Orange), Serifen-Überschriften, Gliederung über
Typografie und Haarlinien. Die Planungsseite ist ab 900 px zweispaltig: links
das Formular, rechts das dunkle Ergebnisband. Dort sitzt jedes passende
Zeitfenster als Balken an seiner Stelle im 24-Stunden-Tag, seine Breite ist die
Dauer. Auf schmalen Bildschirmen steht das Band über dem Formular. Das übrige
Layout ist einspaltig und funktioniert ab 360 px ohne horizontales Scrollen.

## Technik

- Java 21, Spring Boot 4.1 (siehe `pom.xml`), Maven-Wrapper
- Spring MVC mit Thymeleaf, Bean Validation, Spring Data JPA/Hibernate
- H2 als Datei-Datenbank (`./data/`, per `.gitignore` ausgeschlossen); Schema über `ddl-auto=update`
- Externe APIs über `RestClient` mit 5 s Verbindungs- und 10 s Lese-Timeout
- Frontend mit gemeinsamem Thymeleaf-Layout, eigenem CSS und einem kleinen Vanilla-JavaScript (`static/`), ohne externe Bibliotheken, CDNs oder Build-Schritt; alle Kernfunktionen funktionieren auch ohne JavaScript
- Oberfläche und Datumsformatierung auf Deutsch (`spring.web.locale=de`)
- Package: `de.anton.wannpasstes`

## Bekannte Grenzen

- Erste lokale Version, **nicht für eine öffentliche Bereitstellung gedacht**: keine Anmeldung, keine Benutzertrennung, keine CSRF-Absicherung; leeres Datenbankpasswort nur für lokale Entwicklung.
- Suchzeitraum ist auf die sieben Vorhersagetage beschränkt; mehr als ein Zeitraum gleichzeitig ist nicht möglich.
- Am letzten Vorhersagetag kann der Messpunkt um Mitternacht fehlen, dann wird das letzte Stundenintervall nicht bewertet.
- Profilnamen werden in der Anwendung eindeutig geprüft (ohne Beachtung von Groß-/Kleinschreibung und Leerzeichen am Rand), aber nicht durch eine Datenbank-Constraint abgesichert. Grund: vorhandene Datenbanken können bereits doppelte Namen enthalten und dürfen den Start nicht verhindern.
- Der Ortsname wird beim Speichern eines Profils mitgeschrieben und danach nicht aktualisiert. Fehlt er (ältere Profile), zeigt die Übersicht die Orts-ID.
- Unvollständige Wetterdaten werden übersprungen; fehlt ein Messpunkt, kann dadurch ein Zeitfenster kürzer ausfallen.
- Keine Wettergarantie und keine Sicherheitsaussage für Outdoor-Aktivitäten.

## Lizenz

MIT, siehe [LICENSE](LICENSE).
