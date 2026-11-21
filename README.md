# Wann passt es?

Lokale Webanwendung, die für einen Ort, eine Outdoor-Aktivität, einen Zeitraum von bis zu sieben Tagen, eine Mindestdauer und persönliche Wetterwünsche passende Zeitfenster berechnet. Wiederverwendbare Suchprofile lassen sich speichern, verwenden, bearbeiten und löschen.

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
| `ProfileBrowserTests` | `-Ddemo.browser=true` | Benötigt Node.js und Playwright (Chromium); führt `src/test/browser/profile-flow.mjs` aus (Speichern, Verwenden, Bearbeiten, Löschen). Eine vorhandene Playwright-Installation lässt sich über die Umgebungsvariable `PLAYWRIGHT_MODULE` angeben. |
| `ProfilePerformanceTests` | `-Ddemo.performance=true` | Grenzwert über `-Ddemo.maxP95Millis` (Standard 1000) |

## Funktionen

Alle Seiten nutzen ein gemeinsames Layout mit Navigation (Planung, Ort suchen, Suchprofile) und eigenem CSS/JavaScript unter `src/main/resources/static/`. Alle Kernfunktionen funktionieren auch ohne JavaScript; das Skript verbessert nur die Bedienung (Lösch-Rückfrage, Ladeanzeige, Übernahme noch nicht abgeschickter Eingaben beim Ortswechsel, Enddatum folgt dem Startdatum).

- **Planung** (`/`): Zeitraum mit Start- und Enddatum (jeweils heute bis heute + 6 Tage in der Zeitzone des Orts), Aktivität, Mindestdauer sowie Wünsche zu Temperatur, Wind und Niederschlag. Ergebnis sind alle zusammenhängenden passenden Zeitfenster im Zeitraum, chronologisch nach Tagen gruppiert, mit Uhrzeit, Dauer, Kennzahlen und einer Zeitleiste über den ganzen Zeitraum. Fenster über Mitternacht bleiben ein Fenster.
- **Ortssuche** (`/locations`): Suche ab zwei Zeichen, bis zu fünf Treffer, deutsche Ortsnamen. Beim Ortswechsel bleiben Aktivität, Mindestdauer, Wetterwünsche und Zeitraum (bzw. beim Bearbeiten eines Profils die Profilangaben) erhalten.
- **Suchprofile** (`/profiles`): aktuelle Auswahl unter einem eindeutigen Namen speichern, verwenden, bearbeiten (`/profiles/{id}/edit`) und löschen. Gespeichert wird auch der Ortsname.
- **Fehlerseiten**: Ungültige Parameter (z. B. unbekannte Auswahlwerte, Text statt Zahl, Mindestdauer kleiner 1, ungültiges Datum) führen zu einer deutschsprachigen Fehlerseite mit Status 400, unbekannte Adressen und Profile zu einer 404-Seite, unerwartete Fehler zu einer Seite ohne technische Details.

### Suchzeitraum

| Parameter | Verhalten |
| --- | --- |
| keiner | nur heute |
| `startDate` und `endDate` | gesamter Zeitraum (beide Tage eingeschlossen) |
| nur `startDate` | genau dieser Tag |
| nur `endDate` | von heute bis zum Enddatum |
| `searchDate` (bisheriger Parameter) | genau dieser Tag; `startDate`/`endDate` haben Vorrang |

Ein Enddatum vor dem Startdatum oder Daten außerhalb der sieben Vorhersagetage werden mit einer Meldung (Status 400) abgelehnt; dann wird keine Wetterabfrage ausgeführt. Vergangene Stunden werden ignoriert. Fenster werden am Ende des Zeitraums (Mitternacht nach dem Enddatum) abgeschnitten.

### Suchprofile

- Profilnamen haben nach dem Trimmen 1–100 Zeichen und sind eindeutig; der Vergleich ignoriert Groß-/Kleinschreibung und Leerzeichen am Anfang und Ende. Beim Bearbeiten darf ein Profil seinen eigenen Namen behalten.
- Ältere Profile ohne gespeicherten Ortsnamen zeigen „Unbekannter Ort (ID …)“ und bleiben nutzbar. Beim Bearbeiten wird der Name, wenn möglich, über die Ortssuche-API nachträglich ermittelt und mit dem nächsten Speichern übernommen.
- Die Spalte `location_name` wird beim Start per `ddl-auto=update` ergänzt; bestehende Profile bleiben erhalten.

### Wetterwünsche (vorläufige Produktparameter)

Grenzen sind eingeschlossen; `ANY` bedeutet keine Grenze für das jeweilige Kriterium.

| Auswahl | Bedeutung |
| --- | --- |
| Temperatur `COOL` / `MILD` / `WARM` | 5–15 °C / 15–25 °C / 20–30 °C |
| Wind `WEAK` / `MODERATE` | max. 15 km/h / max. 30 km/h |
| Niederschlag `DRY` / `LIGHT_DRIZZLE` / `LIGHT_RAIN` | max. 0,0 / 0,2 / 1,0 mm pro Stunde |

Die Aktivität (Spazieren, Laufen, Radfahren, Wandern) ist eine Bezeichnung und setzt keine automatischen Wetterregeln. Der von Open-Meteo gelieferte `weatherCode` wird geladen, aber noch nicht ausgewertet.

### Zeitfensterberechnung

`TimeWindowService` betrachtet benachbarte Stundenwerte im Abstand von genau einer Stunde. Temperatur und Wind müssen an beiden Intervall-Endpunkten passen; der Niederschlag wird vom Endpunkt genommen, weil dieser Wert die vorausgegangene Stunde beschreibt. Passende Intervalle werden zu Zeitfenstern zusammengefasst. Bei mehreren Tagen werden die Stundenwerte als durchgehende Reihe ausgewertet; die Mindestdauer gilt für das zusammenhängende Fenster. Das ist eine stündliche Näherung, keine Garantie für die Bedingungen zwischen den Messpunkten.

Unvollständige Wetterdaten (unterschiedlich lange oder fehlende Listen, `null`-Werte) führen nicht zum Absturz: Stunden ohne Zeit, Temperatur, Wind oder Niederschlag werden übersprungen und unterbrechen damit ein Zeitfenster. Liegen im Zeitraum gar keine verwertbaren Daten vor, erscheint ein Hinweis.

## Technik

- Java 21, Spring Boot 4.1 (siehe `pom.xml`), Maven-Wrapper
- Spring MVC mit Thymeleaf, Bean Validation, Spring Data JPA/Hibernate
- H2 als Datei-Datenbank (`./data/`, per `.gitignore` ausgeschlossen); Schema über `ddl-auto=update`
- Externe APIs über `RestClient` mit 5 s Verbindungs- und 10 s Lese-Timeout
- Frontend: Thymeleaf-Fragmente (`templates/fragments/layout.html`), eigenes CSS (`static/css/app.css`) und optionales Vanilla-JavaScript (`static/js/app.js`); keine externen Bibliotheken, Fonts oder CDNs
- Package: `de.anton.wannpasstes`

## Bekannte Grenzen

- Lokale Version, **nicht für eine öffentliche Bereitstellung gedacht**: keine Anmeldung, keine Benutzertrennung, keine explizite CSRF-Absicherung; leeres Datenbankpasswort nur für lokale Entwicklung.
- Suchzeitraum nur innerhalb der sieben Vorhersagetage. Am letzten Vorhersagetag kann der Messpunkt um Mitternacht fehlen, dann wird das letzte Stundenintervall nicht bewertet.
- Ohne JavaScript übernimmt der Ortswechsel den zuletzt abgeschickten (Planung) bzw. gespeicherten oder zuletzt übernommenen (Profil bearbeiten) Stand; noch nicht abgeschickte Änderungen in anderen Feldern gehen dann verloren. Mit JavaScript werden auch diese übernommen.
- Die Eindeutigkeit von Profilnamen wird in der Anwendung geprüft, nicht per Datenbank-Constraint (keine Absicherung gegen gleichzeitige Anfragen). Bereits vorhandene doppelte Namen aus der ersten Version bleiben bestehen, bis eines der Profile umbenannt wird.
- Der Ortsname eines Profils wird mit dem Formular übermittelt und nicht serverseitig gegen die Orts-ID geprüft.
- Profile speichern keinen Zeitraum; „Profil verwenden“ durchsucht wie bisher den heutigen Tag.
- Keine Wettergarantie und keine Sicherheitsaussage für Outdoor-Aktivitäten.

## Lizenz

MIT, siehe [LICENSE](LICENSE).
