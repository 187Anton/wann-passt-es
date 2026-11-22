# Wann passt es?

Lokale Webanwendung, die für einen Ort, eine Outdoor-Aktivität, einen Zeitraum, eine Mindestdauer und persönliche Wetterwünsche passende Zeitfenster berechnet. Wiederverwendbare Suchprofile lassen sich speichern, verwenden, bearbeiten und löschen.

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
| `ProfileBrowserTests` | `-Ddemo.browser=true` | Benötigt Node.js und Playwright (Chromium); prüft `src/test/browser/profile-flow.mjs` auf Desktop und bei 360 px mit und ohne JavaScript |
| `ProfilePerformanceTests` | `-Ddemo.performance=true` | Grenzwert über `-Ddemo.maxP95Millis` (Standard 1000) |

## Funktionen

- **Ortssuche** (`/locations`) findet ab zwei Zeichen bis zu fünf Orte mit deutschen Bezeichnungen. Beim Ortswechsel bleiben Zeitraum, Aktivität, Mindestdauer und Wetterwünsche erhalten.
- **Planung** (`/`) durchsucht standardmäßig heute. Start- und Enddatum liegen jeweils zwischen heute und heute + 6 Tagen am gewählten Ort. Ohne Enddatum gilt das Startdatum auch als Ende. Die Ergebnisse stehen chronologisch in Ortszeit. Bestehende Links mit `searchDate` suchen weiterhin genau einen Tag, wenn keine neuen Datumsparameter angegeben sind.
- **Tageszeilen** zeigen passende Zeitfenster auf einer gemeinsamen Skala von 0 bis 24 Uhr. Ein Fenster über Mitternacht bleibt zusammenhängend beschriftet. Die Wetterwerte stehen in den aufklappbaren Details zum Fenster.
- **Suchprofile** (`/profiles`) speichern Orts-ID, Ortsname, Aktivität, Mindestdauer und die drei Wetterwünsche. Du kannst Profile verwenden, bearbeiten oder löschen. Profilnamen sind unabhängig von Groß- und Kleinschreibung sowie äußeren Leerzeichen eindeutig. Datumsangaben gehören zur aktuellen Planung.
- **Ältere Profile** bleiben verfügbar. Beim Start ergänzt die Anwendung bei doppelten Altnamen freie Nummern wie `(2)`. Fehlt der Ortsname, erscheint die Orts-ID als Platzhalter. Beim Bearbeiten kann der Ortsname nachgetragen werden.

### Wetterwünsche (vorläufige Produktparameter)

Grenzen sind eingeschlossen. Die Auswahl `ANY` bedeutet keine Grenze für das jeweilige Kriterium.

| Auswahl | Bedeutung |
| --- | --- |
| Temperatur `COOL` / `MILD` / `WARM` | 5–15 °C / 15–25 °C / 20–30 °C |
| Wind `WEAK` / `MODERATE` | max. 15 km/h / max. 30 km/h |
| Niederschlag `DRY` / `LIGHT_DRIZZLE` / `LIGHT_RAIN` | max. 0,0 / 0,2 / 1,0 mm pro Stunde |

Die Aktivität (Spazieren, Laufen, Radfahren, Wandern) ist eine Bezeichnung und setzt keine automatischen Wetterregeln. Der von Open-Meteo gelieferte `weatherCode` wird geladen, aber noch nicht ausgewertet.

### Zeitfensterberechnung

`TimeWindowService` betrachtet benachbarte Stundenwerte im Abstand von genau einer Stunde. Temperatur und Wind müssen an beiden Endpunkten passen. Der Niederschlag wird vom Endpunkt genommen, weil dieser Wert die vorausgegangene Stunde beschreibt. Passende Intervalle werden auch über Mitternacht zu einem Zeitfenster zusammengefasst. Die Mindestdauer gilt für das gesamte Fenster. Vergangene Stunden und unbrauchbare Messpunkte werden übersprungen. Fehlende Stunden unterbrechen das Fenster. Das ist eine stündliche Näherung, keine Garantie für die Bedingungen zwischen den Messpunkten.

## Technik

- Java 21, Spring Boot 4.1 (siehe `pom.xml`), Maven-Wrapper
- Spring MVC mit Thymeleaf, Bean Validation, Spring Data JPA/Hibernate
- H2 als Datei-Datenbank (`./data/`, per `.gitignore` ausgeschlossen); Schema über `ddl-auto=update`
- Externe APIs über `RestClient` mit 5 s Verbindungs- und 10 s Lese-Timeout
- Package: `de.anton.wannpasstes`

## Bekannte Grenzen

- Erste lokale Version, **nicht für eine öffentliche Bereitstellung gedacht**: keine Anmeldung, keine Benutzertrennung, keine explizite CSRF-Absicherung; leeres Datenbankpasswort nur für lokale Entwicklung.
- Am letzten Vorhersagetag kann der Messpunkt um Mitternacht fehlen, dann wird das letzte Stundenintervall nicht bewertet.
- Bei lückenhaften Vorhersagen können weniger oder kürzere Zeitfenster erscheinen. Wenn keine Stunde verwertbar ist, zeigt die Planung eine Fehlermeldung.
- Für neue Profile und Ortswechsel beim Bearbeiten muss die Orts-API den Namen zum gewählten Ort liefern können.
- Keine Wettergarantie und keine Sicherheitsaussage für Outdoor-Aktivitäten.

## Lizenz

MIT, siehe [LICENSE](LICENSE).
