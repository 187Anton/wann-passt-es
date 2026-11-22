# REVIEW-R3 – Review von `agent/hermes-deepseek-r2b`

## 1. Kopf

| | |
| --- | --- |
| Geprüfter Branch | `agent/hermes-deepseek-r2b` |
| Commit | `e5d4893f0b9d10229f86c7c7ebb4f0a7cd7fa107` („Document the round 2b design implementation and checks“) |
| Prüfumfang | `git diff agentic-baseline..agent/hermes-deepseek-r2b -- . ':!.agents' ':!.claude' ':!.interface-design' ':!TASK-R2B.md'` (34 Dateien, +4854/−381) |
| Datum | 22.11.2026 |
| Reviewer | Claude Opus 5.5 (Branch `agent/claude-opus-r3`) |

**Tatsächlich ausgeführt**

- Temporärer Worktree `/tmp/r3-review-wpe-claude` (detached auf `e5d4893`), danach wieder entfernt. Am Ziel-Branch wurde nichts verändert.
- `./mvnw test`: **BUILD SUCCESS, 89 Tests, 0 Fehlschläge, 0 Fehler, 3 übersprungen** (die per System-Property abgeschalteten Demo-Klassen `ProfileBrowserTests` und `ProfilePerformanceTests`). Das Browser-Szenario `profile-flow.mjs` habe ich **nicht** ausgeführt.
- Anwendung mit `./mvnw spring-boot:run` auf Port 8092 (aus `.port`) gestartet, mit Live-Daten von Open-Meteo.
- HTTP-Prüfungen per `curl`: ungültiger Enum-Wert, Text statt Zahl, Mindestdauer 0/−5/Überlauf, ungültiges Datum, Ende vor Start, Daten außerhalb des Bereichs, `searchDate`, nur `endDate`, `locationId=0`, unbekannte URL, unbekanntes Profil (Bearbeiten/Löschen), falsche HTTP-Methode, doppelter Profilname mit anderer Schreibweise und Leerzeichen, Profilname mit `<script>` (Escaping in Liste und `data-confirm`).
- Playwright-MCP: Planung über 7 Tage (1280 × 800), Leerzustand (360 × 800, ganze Seite), ungültiger Zeitraum (1280 × 800), Profil bearbeiten → Ortswechsel, Abbrechen der Lösch-Rückfrage; `scrollWidth`-Messung bei 360 px für Planung (Treffer, leer, Fehler), Ortssuche, Profile und 404.
- `jshell` gegen die kompilierten Klassen des Ziel-Branches (`target/classes` + Test-Classpath), um zwei Logikbefunde nachzustellen (Befunde 2 und 3).
- Kontraste der Palette rechnerisch nach WCAG nachgerechnet (Python).

## 2. Gesamturteil

**merge-fähig nach kleinen Änderungen**

Der Branch ist sauber strukturiert, die Suite ist grün, Fehlerseiten, Escaping und Designsystem sind ordentlich umgesetzt, und das Layout hielt bei 360 und 1280 px allen Prüfungen stand. Vor dem Merge sollten aber vier nachgestellte Fehler mittlerer Schwere behoben werden: Ein Enddatum ohne Startdatum wird stillschweigend verworfen (auch ein ungültiges, entgegen B3), ein fehlender `weather_code` löscht ganze Stunden, die Wetterdetails zeigen Niederschlag außerhalb des Fensters, und beim Ortswechsel im Profil-Editor gehen ungespeicherte Änderungen verloren. Jede Korrektur ist klein und lokal.

## 3. Befunde

### Befund 1 – mittel: Enddatum ohne Startdatum wird ignoriert, auch ungültige Werte ohne Meldung

- **Ort:** `src/main/java/de/anton/wannpasstes/MainpageController.java:82–85`
- **Problem:** `requestedEnd` wird nur aus `endDate` gebildet, wenn auch `startDate` gesetzt ist, sonst aus `searchDate`. Lässt man in der Oberfläche „Von“ leer und wählt nur „Bis“, durchsucht die Anwendung still nur heute und setzt „Bis“ im Formular auf heute zurück. Ein Enddatum außerhalb des erlaubten Bereichs (z. B. `endDate=2020-01-01`) führt dann ebenfalls zu keiner Meldung. Das widerspricht B3 („Daten außerhalb des erlaubten Bereichs führen zu einer verständlichen Meldung“), und die Eingabe geht verloren, ohne dass die Nutzerin es merkt.
- **Nachweis:** *verifiziert*: `curl "http://localhost:8092/?endDate=2026-11-25&…"` liefert als Zeitraum „22.11.2026“ und `value="2026-11-22"` im Feld „Bis“. `curl "http://localhost:8092/?endDate=2020-01-01"` liefert HTTP 200 ohne `alert--error`. In `MainpageRangeTests` gibt es keinen Test für diesen Fall.
- **Vorschlag:** `requestedEnd = endDate != null ? endDate : (startDate != null ? startDate : searchDate)`. Fehlt dann der Start, gilt „heute bis Ende“, und `validateRange` greift auch hier. Dazu zwei Tests: nur `endDate` gültig bzw. ungültig.

### Befund 2 – mittel: Fehlender `weather_code` verwirft Stunden, obwohl er nicht ausgewertet wird

- **Ort:** `src/main/java/de/anton/wannpasstes/WeatherClient.java:72`
- **Problem:** Eine Stunde wird übersprungen, sobald `weatherCode == null` ist. Laut TASK.md und README wird `weatherCode` aber gar nicht ausgewertet. Fehlt die Liste `weather_code` oder enthält sie `null`, verschwinden deshalb Stunden mit vollständigen Temperatur-, Wind- und Niederschlagswerten, im Extremfall alle. Die Oberfläche meldet dann „Keine passenden Zeitfenster … Das ist kein Fehler – dein Suchfenster ist nur streng.“ Das ist bei fehlenden Daten irreführend, und C3 („unbrauchbare Stunden überspringen“) wird zu weit ausgelegt.
- **Nachweis:** *verifiziert*: Per `jshell` mit `MockRestServiceServer` drei vollständige Stunden ohne `weather_code`-Liste an `WeatherClient.fetchForecast` übergeben, Ergebnis: `hours without weather_code list = 0`. Die Tests in `WeatherClientRobustnessTests` liefern immer eine vollständige `weather_code`-Liste mit.
- **Vorschlag:** `weatherCode` nicht als Pflichtwert behandeln (fehlend → z. B. `-1` oder `WeatherHour.weatherCode` als `Integer`). Zusätzlich im Controller unterscheiden, ob gar keine verwertbaren Stunden im Zeitraum lagen, und dann einen Hinweis zeigen statt des Leerzustands. Test: Antwort ohne `weather_code` ergibt alle Stunden.

### Befund 3 – mittel: Wetterdetails zeigen den Niederschlag der Stunde vor dem Fenster

- **Ort:** `src/main/java/de/anton/wannpasstes/PlanningTimeline.java:186–207` (`detailsOf`, Bedingung in Zeile 201)
- **Problem:** Die Details bilden das Maximum über alle Stundenwerte von `window.start()` bis einschließlich `window.end()`. Der Niederschlagswert eines Messpunkts beschreibt aber die *vorausgegangene* Stunde (README, `TimeWindowService`). Der Wert am Startpunkt gehört also nicht zum Fenster. Ein Fenster mit „Trocken“ kann so „bis 2,0 mm/h“ anzeigen. Das widerspricht dem Wunsch der Nutzerin und untergräbt das Vertrauen in das Ergebnis.
- **Nachweis:** *verifiziert*: Per `jshell` Stunden 10–13 Uhr (10:00 mit 2,0 mm, sonst 0,0) und `PrecipitationPreference.DRY` eingesetzt. Ergebnis: `TimeWindow[10:00, 13:00]` mit `WindowDetails[…, precipitation=bis 2,0 mm/h]`. Der Test `PlanningTimelineTests.summarisesTheWeatherWithinTheWindow` deckt das nicht ab, weil der Startpunkt dort 0,0 mm hat.
- **Vorschlag:** Für Niederschlag nur Messpunkte mit `time > window.start()` berücksichtigen (Temperatur und Wind weiter inklusive Start). Einen Regressionstest mit Regen am Startpunkt ergänzen.

### Befund 4 – mittel: Ortswechsel im Profil-Editor verwirft ungespeicherte Änderungen

- **Ort:** `src/main/resources/templates/profile-edit.html:59`, `src/main/java/de/anton/wannpasstes/LocationSearchController.java:88–93`, `README.md:40`
- **Problem:** „Anderen Ort wählen“ ist ein einfacher Link auf `/locations?profileId=…`, und der Rücksprung trägt nur `locationId` und `locationName`. Wer erst Name, Aktivität oder Mindestdauer ändert und dann den Ort wechselt, findet wieder die gespeicherten Werte vor, mit und ohne JavaScript. Das README sagt dagegen, auch der aus einem Profil gewählte Ort „übernimmt Zeitraum, Aktivität, Mindestdauer und Wetterwünsche“. C4 betrifft zwar ausdrücklich die Planung, dieselbe Erwartung entsteht aber hier (vgl. A6 „Eingaben gehen nicht verloren“).
- **Nachweis:** *verifiziert* (Playwright): `/profiles/1/edit?locationId=2911298&locationName=Hamburg` aufgerufen, so wie der Treffer-Link der Ortssuche für `profileId=1` lautet. Ergebnis: Name „Abendrunde“, Aktivität `CYCLING`, Dauer 90, also die gespeicherten und nicht die im Formular geänderten Werte. Der Link auf der Seite ist `href="/locations?profileId=1"` ohne weitere Parameter.
- **Vorschlag:** Den Ortswechsel als Submit-Button des Edit-Formulars umsetzen (`formaction="/locations" formmethod="get"`), der alle Felder samt `profileId` mitschickt. `LocationSearchController.returnUrlFor` und `GET /profiles/{id}/edit` sollten diese Felder durchreichen bzw. übernehmen. Alternativ das README korrigieren.

### Befund 5 – niedrig: Abgebrochene Lösch-Rückfrage hinterlässt dauerhaften Ladezustand

- **Ort:** `src/main/resources/static/js/app.js:15` und `:27`
- **Problem:** Der Busy-Listener prüft `event.defaultPrevented` nicht. Bricht man die Rückfrage „wirklich löschen?“ ab, bekommt der Button trotzdem `is-busy` (Spinner) und `aria-busy="true"` und behält beides, obwohl nichts passiert. Sichtbare Zustände sollen laut TASK.md D eindeutig sein. Dasselbe tritt nach „Zurück“ aus dem bfcache auf.
- **Nachweis:** *verifiziert* (Playwright auf `/profiles`): `window.confirm = () => false`, dann Klick auf „Löschen“. Danach `className` mit `… is-busy`, `aria-busy` gleich `"true"`, URL unverändert.
- **Vorschlag:** Im Busy-Listener zuerst `if (event.defaultPrevented) return;` prüfen. Das funktioniert, weil der Confirm-Listener bereits vorher registriert ist. Zusätzlich bei `pageshow` mit `event.persisted` den Zustand zurücksetzen.

### Befund 6 – niedrig: Tage ohne Treffer sind für Screenreader leer

- **Ort:** `src/main/resources/templates/mainpage.html:139` (`.track` mit `aria-hidden="true"`) und `:163` (`window-lines` nur bei Segmenten)
- **Problem:** Das Tagesband ist korrekt vor Hilfstechnik verborgen. Eine Textzeile gibt es aber nur, wenn der Tag Segmente hat. Bei einem mehrtägigen Zeitraum hören Screenreader-Nutzer für Tage ohne Fenster nur „Dienstag 24.11.“ ohne Aussage. Sehende erkennen dagegen das leere Band.
- **Nachweis:** *Vermutung* (aus dem Template gelesen, nicht mit Screenreader geprüft).
- **Vorschlag:** `th:unless` mit einer dezenten Zeile „Kein passendes Zeitfenster“ (sichtbar in Sekundärtext oder `visually-hidden`).

### Befund 7 – niedrig: Ort steht nicht im Suchbereich, Profilname landet in der Planungs-URL

- **Ort:** `src/main/resources/templates/mainpage.html:16` (Ort als Chip im Seitenkopf), `:213` und `:294` (Profilname-Feld im GET-Formular)
- **Problem:** (a) `system.md` sieht den Ort im linken Suchbereich vor („Ort, Zeitraum, Aktivität …“). Hier steht er als Chip über beiden Spalten, und der Suchbereich beginnt mit „Zeitraum“. Das ist eine kleine, aber sichtbare Abweichung vom Zielbild. (b) Weil das Feld „Profilname“ im selben GET-Formular liegt, hängt „Zeitfenster finden“ auch `profileName=…` (und `locationName=…`) an die URL. Das ist harmlos, aber URL-Rauschen, und der Profilname landet im Verlauf.
- **Nachweis:** *verifiziert* für (a) per Screenshot bei 1280 × 800. (b) aus dem Template gelesen (*Vermutung* bezüglich des Verlaufs).
- **Vorschlag:** Eine kompakte Ortszeile mit „Ort ändern“ als ersten Block in das Suchpanel verschieben. Das Speichern als eigenes POST-Formular mit versteckten Feldern umsetzen, oder `profileName` per `form`-Attribut nur dem Speichern-Button zuordnen.

### Befund 8 – niedrig: Ergebnis-Template mit Tageszeilen wird von keinem automatisierten Test gerendert

- **Ort:** `src/test/java/de/anton/wannpasstes/MainpageRangeTests.java`, `PlanningTimelineTests.java`, `RequestErrorHandlingTests.java:76–88`
- **Problem:** `MainpageRangeTests` prüft nur das Model, `PlanningTimelineTests` nur die Datenklasse. Der einzige Test, der `/` vollständig rendert (`acceptsEmptyDateParameters`), liefert leere Wetterdaten und prüft nur den Status 200. Die SpEL-Ausdrücke für Tageszeilen, Segmente, „Fortsetzung“ und `<details>` laufen in der Suite also nie. Ein Tippfehler dort wäre erst zur Laufzeit als 500 sichtbar. Für ein Kriterium wie Z4 ist das eine merkliche Lücke.
- **Nachweis:** *verifiziert*: `grep` über `src/test` findet keine Assertion auf `track__window`, `window-line` oder `Fortsetzung`. Die Suite ist grün, ohne dieses Markup zu rendern.
- **Vorschlag:** Einen MockMvc- oder `RANDOM_PORT`-Test mit einem Fenster über Mitternacht ergänzen, der „Sa 19:00 – Mo 00:00“, „Fortsetzung“ und das `<details>`-Element im HTML prüft.

## 4. Stärken

1. **Saubere Trennung von Fachlogik und Darstellung.** `PlanningTimeline` positioniert und beschriftet nur. Die Prozentrechnung nutzt die echte Tageslänge (`dayMinutes`), funktioniert also auch an 23- und 25-Stunden-Tagen. Die 24:00-Konvention und der echte Endtag bei mehrtägigen Fenstern sind konsequent umgesetzt und getestet.
2. **Fehlerbehandlung.** `GlobalExceptionHandler` und `DateBindingAdvice` bilden 400/404/405/500 auf eine gestaltete, deutschsprachige Seite ohne Interna ab, und ein leeres Datumsfeld bedeutet „nicht angegeben“. Die Fälle habe ich per `curl` nachgestellt (alle wie erwartet). Ein Test stellt sicher, dass die Exception-Message nicht im 500er-Body erscheint.
3. **Zeitraum in einem Durchgang.** Der ganze Zeitraum wird als eine Stundenreihe bewertet (Fenster über Mitternacht bleiben ganz). Bei ungültigem Zeitraum wird nachweislich keine Wetterabfrage ausgeführt (`verify(weatherClient, never())`).
4. **Optionsfelder ohne Drift.** Das Fragment `fragments/preferences.html` liest die Grenzwerte direkt aus den Enums, sodass Beschriftung und Regel nicht auseinanderlaufen können.
5. **Disziplinierte Umsetzung des Designsystems.** Alle Farben sind Tokens, Tönungen entstehen nur per `color-mix`, Radien sind nur 8/12 px (plus 50 % für runde Marken). Nachgerechnete Kontraste: Sekundärtext auf eingelassener Fläche 5,21:1, Feldrahmen 3,70:1 auf Weiß bzw. 3,22:1 auf eingelassener Fläche, Akzent auf Auswahlfläche 5,77:1. Kein horizontales Scrollen bei 360 px auf allen geprüften Seiten. Der Leerzustand ist ruhig und enthält Vorschläge.

## 5. Übernahmeempfehlung

**Übernehmen:**

- `PlanningTimeline` samt Tests als Darstellungsschicht für die Tageszeilen (nach Korrektur von Befund 3).
- `GlobalExceptionHandler`, `DateBindingAdvice`, `error.html` und `RequestErrorHandlingTests`.
- `fragments/preferences.html` (Grenzwerte aus den Enums).
- Die CSS-Token-Struktur aus `app.css` (Palette, Tönungen per `color-mix`, Abstands- und Schriftskala).
- Die `valueAt`-Absicherung im `WeatherClient` (ohne die Pflichtprüfung auf `weatherCode`, Befund 2).
- `existsByNameIgnoreCase` / `existsByNameIgnoreCaseAndIdNot` für die Eindeutigkeit inklusive Ausnahme für das eigene Profil.

**Nicht ohne Änderung übernehmen:**

- Die Datumslogik in `MainpageController` (Befund 1).
- Den Ortswechsel im Profil-Editor (Befund 4) bzw. die zugehörige README-Aussage.
- Den Busy-Listener in `app.js` (Befund 5).
- Vor dem Merge einen Render-Test für die Ergebnisansicht ergänzen (Befund 8).
