# Abschlussbericht Runde 2b – Frontend nach Designsystem „Himmel & Schiefer“

## 1. Umsetzung des Designsystems in Kürze

- **Tokens** (`static/css/app.css`, `:root`): alle zwölf Farben aus `system.md` als Variablen mit sprechenden Namen (`--sky`, `--cloud`, `--haze`, `--ink`, `--slate`, `--harbor`, `--clearing`, `--window`, `--meadow`, `--amber`, `--brick`, `--rule`), Abstandsskala `--s1…--s7` (4–48 px), Radien `--r-control` (8 px) und `--r-surface` (12 px), ein einziger dezenter Flächenschatten `--lift`. Im übrigen CSS kommt kein Hex-Wert mehr vor.
- **Schrift:** nur der Systemschrift-Stack aus `system.md`; ausschließlich 16/24, 14/20, 22/28, 32/40; Gewichte 400/500/600/700; Uhrzeiten, Daten und Dauern mit `tabular-nums`.
- **Planungsseite:** Ab 960 px zwei gleich helle weiße Flächen auf gleicher Höhe – links Suche (320 px: Ort, Zeitraum, Aktivität, Mindestdauer, Wetterwünsche, Profil speichern), rechts Ergebnis. Unter 960 px gilt die DOM-Reihenfolge: Zusammenfassung (Aktivität, Ort, Zeitraum, Mindestdauer, Link „Suche anpassen“) → Ergebnisse → Formular.
- **Ergebnisse (prägendes Element):** eine Zeile pro Tag, gemeinsame Skala 0–24 Uhr mit Marken bei 0/6/12/18/24, Fenster als `#B9D0EA` mit 1-px-Kontur in Akzentfarbe. Darunter jedes Fenster als Text „17:00–24:00 · 7 Std.“. Fenster über Mitternacht erscheinen in jeder berührten Tageszeile, die Segmente laufen ohne Kontur und Rundung bündig in den Nachbartag, beschriftet sind sie einmal am Starttag („So 18:00 – Mo 05:00 · 11 Std. · über Mitternacht“), in Folgetagen als „Fortsetzung“. Wetterdetails stecken in einem nativen `<details>` („Wetter“): Temperatur-, Wind- und Niederschlagsbereich im Fenster sowie „✓ Alle Wetterwünsche erfüllt“ in Erfolgsfarbe. Vergangene Stunden des heutigen Tags sind dezent abgedunkelt (Sekundärton mit 12 % Deckkraft) und in der Legende erklärt.
- **Zustände:** Fehler (ungültiger Zeitraum, Wetterdienst, Profilname) als weiße Hinweisfläche mit roter Kontur und rotem Titel; „keine verwertbaren Daten“ in Warnfarbe; „keine passenden Zeitfenster“ ruhig in Textfarbe mit leerem Tagesband und drei konkreten Vorschlägen, die als Links direkt eine angepasste Suche starten.
- **Formular:** native Elemente; Wetterwünsche als Optionsfelder mit sichtbarem Grenzwert („Höchstens mäßiger Wind · bis 30 km/h“), gewählte Option auf `--clearing`. Der Button „Zeitfenster suchen“ bleibt beim Scrollen durch das lange Formular am unteren Rand erreichbar (`position: sticky`).
- **Suchprofile:** ruhige Liste auf einer Fläche, je Profil Name, Ort und Kenndaten, eine gefüllte Hauptaktion („Profil verwenden“), Bearbeiten und Löschen als zurückhaltende Textbuttons.
- **Kopf/Fuß:** Kopfzeile mit Marke (Mini-Tagesband als Logo, auch als Favicon) und Navigation Planung / Ortssuche / Suchprofile, aktuelle Seite in Akzent auf `--clearing`; schlichte Fußzeile mit Quellenangaben.

## 2. Zuordnung zu Z1–Z12

| ID | Status | Umsetzung / Beleg |
| --- | --- | --- |
| Z1 | erfüllt | Alle Farben als Variablen, kein Hex außerhalb von `:root` (per `grep` geprüft), keine Verläufe. Abgeleitete Werte nur aus Palettenfarben: Schatten mit Haupttext-RGB bei 6 % Deckkraft, Vergangenheitsmarkierung `color-mix` aus `--slate`. Einzige gefüllte gesättigte Fläche ist der Hauptbutton; weiße Schrift nur dort. Die alten Zusatzfarben (Gelb/Grün/Lila in Logo, Zeitleiste und Badges) sind entfernt. |
| Z2 | erfüllt | Nur 14/20, 16/24, 22/28, 32/40 und Gewichte 400–700 (per `grep` gezählt). Abstände nur aus der Skala. Radien 8/12 px; an den Mitternachtskanten der Bandsegmente bewusst 0 (bündiger Übergang in den Nachbartag, siehe Entscheidungen). |
| Z3 | erfüllt | Grid `320px minmax(0,1fr)` ab 960 px, beide Spalten `--cloud`, `align-items: start`. Schmal: Zusammenfassung → Ergebnisse → Formular (Screenshots 360 px). |
| Z4 | erfüllt | Tageszeilen mit gemeinsamer Skala; Text mit Beginn, Ende, Dauer in Std./Min.; Mitternachtsfenster in beiden Zeilen, einmal beschriftet. Tests: `ResultViewTests` (u. a. `labelsMultiDayWindowEndingAtMidnightWithBothDays`), `MainpageWebMvcTests.rendersDayRowsWeatherDetailsAndWishesAsRadioButtons`. |
| Z5 | erfüllt | `<details>` je Fenster mit Wetterbereich; vergangene Stunden heute dezent markiert. Test `summarisesWeatherWithinTheWindow`. |
| Z6 | erfüllt | Leerzustand ruhig (Textfarbe, kein Rot) mit drei Vorschlägen; Rot nur bei Zeitraum-, Wetterdienst-, Profil- und Feldfehlern. |
| Z7 | erfüllt | 1280 × 800: erste Tageszeile beginnt bei ca. 285 px, Leerzustand bei ca. 209 px (per Playwright gemessen). |
| Z8 | erfüllt | Automatische Prüfung auf allen 7 Seiten × 2 Breiten: `scrollWidth == innerWidth`, kein Element außerhalb des Viewports, kein abgeschnittener Text (Overflow/Ellipsis). Zusätzlich Sichtprüfung der Screenshots. |
| Z9 | erfüllt | Siehe Kontrasttabelle unten (WCAG-Formel, per Skript berechnet). |
| Z10 | erfüllt | Playwright-MCP, Prüfprotokoll unten; Gleichgewicht beantwortet. |
| Z11 | erfüllt | `./mvnw test`: 123 Tests, 0 Fehler, 3 übersprungen (Demo-Tests). `profile-flow.mjs` angepasst und mit `-Ddemo.browser=true` ausgeführt: bestanden. Kriterien aus `TASK.md` siehe Abschnitt 4. |
| Z12 | erfüllt | Dieser Bericht; vier Commits auf `agent/claude-opus-r2b`, nichts gepusht. |

## 3. Eigene Entscheidungen und Annahmen

- **Controller minimal angepasst:** `MainpageController` reicht die bereits geladenen Stundenwerte an `ResultView.of(...)` weiter. Ohne sie lassen sich die von Z5 verlangten Wetterdetails nicht anzeigen. Fachlogik, Wetterregeln, URLs, Methoden und Parameternamen sind unverändert.
- **`ResultView` umgebaut (Präsentationsmodell):** Die Zeitleiste über den ganzen Zeitraum (`timeline`) ist durch `days` (Tageszeilen mit Segmenten, Fenstern, Fortsetzungen, Vergangenheitsanteil) ersetzt; neu sind `rangeLabel`, `periodLabel` und `WeatherRange`. `groups()` und die bisherigen Fensterfelder bleiben erhalten. Der Wetterbereich folgt der Bewertungslogik: Temperatur und Wind an allen Stundenpunkten von Beginn bis Ende, Niederschlag an den Punkten nach dem Beginn (der Wert beschreibt die vorausgehende Stunde). Bereiche werden „12 bis 17 °C“ geschrieben, damit negative Temperaturen nicht mit einem Bindestrich verwechselt werden.
- **Geänderte Tests (bewusst geändertes Verhalten):** In `ResultViewTests` prüften zwei Tests die entfernte Zeitleiste über den Zeitraum; sie prüfen jetzt die Tageszeilen (Positionen auf der 0–24-Skala, vergangene Stunden, Fortsetzung). Neu: drei Tests in `ResultViewTests`, ein Markup-Test in `MainpageWebMvcTests`. `profile-flow.mjs` wählt und prüft jetzt zusätzlich Optionsfelder.
- **Beschriftung über Mitternacht:** Wie im Beispiel aus `system.md` zeigen mehrtägige Fenster das tatsächliche Enddatum („Sa 19:00 – Mo 00:00“), Fenster innerhalb eines Tags bis Mitternacht weiterhin „24:00“.
- **Positionierung im Band nach Wanduhrzeit** (Minuten seit lokaler Mitternacht ÷ 1440), damit Fenster genau auf den Stundenmarken liegen; an Zeitumstellungstagen ist das eine Näherung.
- **DOM-Reihenfolge = schmale Reihenfolge.** Auf dem Desktop erreicht die Tastatur deshalb zuerst die Ergebnisse (wenige fokussierbare „Wetter“-Schalter), dann das Formular. Ich halte das für vertretbar, weil die Zusammenfassung zuerst kommt und die Ergebnisse der Zweck der Seite sind.
- **Seitenüberschrift der Planung** ist die Zusammenfassung „Spazieren gehen in Berlin“ (h1). Mobil 22/28, ab 600 px 32/40, weil lange Ortsnamen sonst bei 360 px umbrechen müssten.
- **Hover des Hauptbuttons** nur per Unterstreichung, weil `system.md` keine dunklere Akzentvariante vorsieht und weiße Schrift nur auf dem Akzent stehen soll.
- **Löschen** ist ein zurückhaltender Textbutton, nicht rot: Rot ist laut `system.md` echten Fehlern vorbehalten; die Rückfrage per JavaScript bleibt.
- **Aktivität** bleibt ein `<select>` (vier gleichrangige Bezeichnungen, keine Grenzwerte); nur die Wetterwünsche sind Optionsfelder, wie in `system.md` gewünscht.
- **Linienfarbe** `--rule: #71849A` (zwischen `#EAF0F6` und `#53657A`, 3,84:1 auf Weiß). Für ruhige Trennlinien innerhalb von Flächen (Tageszeilen, Listen) nutze ich `--haze`, weil eine 3:1-Linie dort zu hart wirkt; diese Linien tragen keine Bedienfunktion.
- **Rückfragen des Skills** (Richtung bestätigen, Muster speichern) habe ich gemäß `TASK-R2B.md` nicht gestellt; `system.md` blieb unverändert.

## 4. Prüfungen

### Automatisierte Tests (ausgeführt)

- `./mvnw test` nach dem letzten Commit: **123 Tests, 0 Fehlschläge, 0 Fehler, 3 übersprungen** (`ProfilePerformanceTests` ×2, `ProfileBrowserTests` – standardmäßig deaktiviert).
- `PLAYWRIGHT_MODULE=<lokale Playwright-Installation> ./mvnw test -Ddemo.browser=true -Dtest=ProfileBrowserTests`: **1 Test, bestanden** (Speichern mit Optionsfeld, Verwenden, Bearbeiten, Löschen abbrechen/bestätigen, Neuladen).
- Die bestehenden Tests zu `TASK.md` (A1–A8, B1–B7, C1–C5, D-Kriterien mit Markupbezug) laufen unverändert grün. Labels, IDs der Ortssuche-Hidden-Felder, Fehlertexte und Statuscodes sind erhalten.

### Visuelle Prüfung mit dem Playwright-MCP (Chromium, live gestartete Anwendung auf Port aus `.port`, echte Open-Meteo-Daten vom 22.11.2026, synthetisches Testprofil)

Geprüft bei 1280 × 800 und 360 × 800: Planung mit Ergebnissen über sieben Tage (mehrere Fenster, mehrere über Mitternacht), Planung ohne Treffer, Planung mit ungültigem Zeitraum (Ende vor Start, HTTP 400), Ortssuche mit Treffern, Profilübersicht, Profil bearbeiten, 404. Zusätzlich: aufgeklapptes Wetter-`<details>`, echtes Scrollen mit klebender Suchleiste, Ortswechsel mit vorher geänderten Optionsfeldern (Werte wurden per JavaScript übernommen), Tastaturfokus auf Optionsfeldern (Pfeiltaste, 2-px-Fokusring in Akzent). Überlauf und abgeschnittener Text wurden per Skript auf allen 14 Kombinationen gemessen (alle ohne Befund).

Gefunden und behoben:

1. Markierung vergangener Stunden zu dunkel (22 % Deckkraft) – sprang als graue Fläche heraus → auf 12 % reduziert.
2. Thymeleaf-Fehler: BEM-Klassen mit `__` in `th:classappend` lösen Präprozessierung aus → Modifikatoren `from-before`/`into-next`.
3. Ortssuche und Profilliste mit doppeltem Innenabstand (Flächen-Padding aus dem Media-Query überschrieb `padding: 0`) → Spezifität korrigiert, Trefferlinks mit eigenem Radius.
4. Trennlinien der Trefferliste krümmten sich an den abgerundeten Links → Linie an das Listenelement verlegt.
5. Profildetails stapelten sich bei 1280 px einzeln, weil die Aktionen die Breite beanspruchten → Raster „Name | Aktionen“ / „Details über volle Breite“.
6. Bei 360 px stand „Löschen“ allein in einer zweiten Zeile → Hauptaktion mobil in voller Breite, Nebenaktionen gemeinsam darunter.
7. Kennzahlenzeile brauchte bei 360 px drei Zeilen → dreispaltig (Wert über Label).
8. Leerer Balken im Leerzustand ohne Bedeutung → leeres Tagesband mit Stundenmarken.
9. Pfeile der Vorschlagsliste klebten am Text → Einzug 24 px.

Hinweis zu den Screenshots: Ganzseiten-Aufnahmen zeigen die klebende Suchleiste an ihrer Viewport-Position über dem Formular; beim echten Scrollen liegt sie korrekt (geprüft). Die Datumsfelder erscheinen im MCP-Chromium im US-Format, weil dessen Browsersprache Englisch ist; das ist eine Browsereinstellung.

### Kontraste (WCAG-2-Formel, per Node-Skript berechnet)

| Vordergrund | Hintergrund | Verhältnis | Anforderung |
| --- | --- | --- | --- |
| Haupttext `#243247` | Weiß / `#F3F6FA` / `#EAF0F6` / `#E7EFF8` | 12,95 / 11,94 / 11,28 / 11,16 | Text ≥ 4,5 ✓ |
| Sekundärtext `#53657A` | Weiß / `#F3F6FA` / `#EAF0F6` / `#E7EFF8` | 5,98 / 5,52 / 5,21 / 5,16 | Text ≥ 4,5 ✓ |
| Weiß | Akzent `#2F5E92` (Hauptbutton) | 6,69 | Text ≥ 4,5 ✓ |
| Akzent `#2F5E92` (aktive Navigation, Fokusring, Fensterkontur) | Weiß / `#F3F6FA` / `#EAF0F6` / `#E7EFF8` | 6,69 / 6,18 / 5,83 / 5,77 | Text ≥ 4,5, Fokus ≥ 3 ✓ |
| Fensterkontur `#2F5E92` | Fensterfläche `#B9D0EA` | 4,23 | Grafik ≥ 3 ✓ |
| Linie/Feldrahmen `#71849A` | Weiß / `#F3F6FA` / `#EAF0F6` | 3,84 / 3,54 / 3,34 | Rahmen ≥ 3 ✓ |
| Erfolg `#2D6A4F` | Weiß / `#F3F6FA` / `#E7EFF8` | 6,39 / 5,90 / 5,51 | Text ≥ 4,5 ✓ |
| Warnung `#895B13` | Weiß / `#F3F6FA` | 5,88 / 5,43 | Text ≥ 4,5 ✓ |
| Fehler `#A33A3A` | Weiß / `#F3F6FA` / `#EAF0F6` | 6,51 / 6,01 / 5,67 | Text ≥ 4,5, Rahmen ≥ 3 ✓ |

Optionsfelder sind native Radios mit `accent-color` in Akzentfarbe; ihr Fokus wird zusätzlich als 2-px-Akzentrahmen um die ganze Zeile gezeigt (≥ 5,77:1).

### Prüffrage „Gleichgewicht“

Mit zusammengekniffenen Augen liest sich die Planungsseite als zwei gleich helle, gleich hoch beginnende Flächen auf hellem Grund. Das Auge landet zuerst bei der großen Überschrift rechts und direkt darunter bei den hellblauen Intervallen der Tagesbänder – das Ergebnis ist schnell gefunden. Einzige gesättigte Fläche ist der Button „Zeitfenster suchen“; er ist klein genug, um nicht zu dominieren. Die anfangs zu dunkle Vergangenheitsmarkierung war das einzige Element, das zu laut ins Auge fiel; sie ist abgeschwächt. In der Profilliste wiederholt sich der gefüllte Button je Profil; das bleibt ruhig, weil Name und Details die Zeile tragen, wäre aber bei sehr vielen Profilen der erste Kandidat für eine Abschwächung.

## 5. Verbleibende Schwächen und nächste Schritte

- Tab-Reihenfolge auf dem Desktop: Ergebnisse vor Formular (siehe Entscheidungen). Alternative wäre eine zweite, nur mobil sichtbare Zusammenfassung oder CSS-`order` mit umgekehrter DOM-Reihenfolge.
- Bei vielen Tagen mit einem mehrtägigen Fenster wiederholt sich die Zeile „Fortsetzung“; eine noch knappere Form (nur Band, Text nur am Starttag) wäre denkbar.
- Die klebende Suchleiste kann auf kleinen Bildschirmen beim Scrollen durch die Optionsfelder eine Zeile verdecken.
- `color-mix()` für die Vergangenheitsmarkierung braucht einen aktuellen Browser; ältere Browser zeigen dann keine Markierung (die Hinweiszeile unter den Ergebnissen erklärt das Verhalten trotzdem).
- Die visuelle Prüfung beruht auf Live-Wetterdaten eines Tags; ein Vorschaumodus mit festen synthetischen Daten würde Screenshots reproduzierbar machen.
- Kein automatisierter visueller Regressionstest und keine automatisierte Barrierefreiheitsprüfung (z. B. axe) im Build; beides wäre ein sinnvoller nächster Schritt.
