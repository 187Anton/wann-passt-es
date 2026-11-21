# Wann passt es?

Lokale Webanwendung, die für einen Ort, eine Outdoor-Aktivität, einen Tag, eine Mindestdauer und persönliche Wetterwünsche passende Zeitfenster berechnet. Wiederverwendbare Suchprofile lassen sich speichern, verwenden und löschen.

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

## Funktionen

- **Ortssuche** (`/locations`): Suche ab zwei Zeichen, bis zu fünf Treffer, deutsche Ortsnamen.
- **Planung** (`/`): Datum (heute bis heute + 6 Tage), Aktivität, Mindestdauer sowie Wünsche zu Temperatur, Wind und Niederschlag. Ergebnis sind zusammenhängende passende Zeitfenster in Ortszeit.
- **Suchprofile** (`/profiles`): aktuelle Auswahl unter einem Namen speichern, gespeichertes Profil verwenden, Profil löschen.

### Wetterwünsche (vorläufige Produktparameter)

Grenzen sind eingeschlossen; `ANY` bedeutet keine Grenze für das jeweilige Kriterium.

| Auswahl | Bedeutung |
| --- | --- |
| Temperatur `COOL` / `MILD` / `WARM` | 5–15 °C / 15–25 °C / 20–30 °C |
| Wind `WEAK` / `MODERATE` | max. 15 km/h / max. 30 km/h |
| Niederschlag `DRY` / `LIGHT_DRIZZLE` / `LIGHT_RAIN` | max. 0,0 / 0,2 / 1,0 mm pro Stunde |

Die Aktivität (Spazieren, Laufen, Radfahren, Wandern) ist eine Bezeichnung und setzt keine automatischen Wetterregeln. Der von Open-Meteo gelieferte `weatherCode` wird geladen, aber noch nicht ausgewertet.

### Zeitfensterberechnung

`TimeWindowService` betrachtet benachbarte Stundenwerte im Abstand von genau einer Stunde. Temperatur und Wind müssen an beiden Intervall-Endpunkten passen; der Niederschlag wird vom Endpunkt genommen, weil dieser Wert die vorausgegangene Stunde beschreibt. Passende Intervalle werden zu Zeitfenstern zusammengefasst. Das ist eine stündliche Näherung, keine Garantie für die Bedingungen zwischen den Messpunkten.

## Technik

- Java 21, Spring Boot 4.1 (siehe `pom.xml`), Maven-Wrapper
- Spring MVC mit Thymeleaf, Bean Validation, Spring Data JPA/Hibernate
- H2 als Datei-Datenbank (`./data/`, per `.gitignore` ausgeschlossen); Schema über `ddl-auto=update`
- Externe APIs über `RestClient` mit 5 s Verbindungs- und 10 s Lese-Timeout
- Package: `de.anton.wannpasstes`

## Bekannte Grenzen

- Erste lokale Version, **nicht für eine öffentliche Bereitstellung gedacht**: keine Anmeldung, keine Benutzertrennung, keine explizite CSRF-Absicherung; leeres Datenbankpasswort nur für lokale Entwicklung.
- Suchzeitraum ist ein einzelner Tag innerhalb der sieben Vorhersagetage.
- Am letzten Vorhersagetag kann der Messpunkt um Mitternacht fehlen, dann wird das letzte Stundenintervall nicht bewertet.
- Unvollständige oder inkonsistente API-Daten werden noch nicht robust abgefangen; ungültige Parameter führen nicht durchgängig zu einer freundlichen Fehlerseite.
- Beim Ortswechsel über die Ortssuche gehen gewählte Wetterwünsche und Datum verloren.
- Profile zeigen die Orts-ID statt eines Ortsnamens, Profilnamen sind nicht eindeutig, Profile lassen sich nicht bearbeiten.
- Keine Wettergarantie und keine Sicherheitsaussage für Outdoor-Aktivitäten.

## Lizenz

MIT, siehe [LICENSE](LICENSE).
