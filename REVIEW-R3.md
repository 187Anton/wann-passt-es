# Review Runde 3 – Ziel-Branch `agent/codex-r2b`

## 1. Kopf

- **Geprüfter Branch:** `agent/codex-r2b`
- **Commit:** `1fca13b` („Document round 2b design verification“), `1fca13bd9d13d37bf191fd2298d94acfed82b77d`
- **Datum:** 22.11.2026
- **Prüfumfang:** `git diff agentic-baseline..agent/codex-r2b -- . ':!.agents' ':!.claude' ':!.interface-design' ':!TASK-R2B.md'` (38 Dateien, +2291/−482), zusätzlich `AGENT_REPORT.md` und `AGENT_REPORT_R2B.md` gelesen und die Aussagen stichprobenartig selbst nachgeprüft.
- **Worktree:** temporär unter `/tmp/r3-review-wpe-hermes` (detached). Am Ziel-Branch wurde nichts geändert; `git status` im Review-Worktree blieb leer, der Worktree wurde nach der Prüfung entfernt.
- **Ausgeführt:**
  - `./mvnw test` im Review-Worktree: **66 Tests, 63 bestanden, 0 fehlgeschlagen, 0 Fehler, 3 übersprungen** (`ProfileBrowserTests` 1, `ProfilePerformanceTests` 2), **BUILD SUCCESS** (Maven 3.10.0, Java 23, Projektziel Java 21). Vor den Browserprüfungen und danach keine Änderung am Arbeitsbaum.
  - Anwendung im Worktree gestartet: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8093` (Port aus `.port`), echte Open-Meteo-Antworten, H2-Dateidatenbank.
  - Playwright-MCP, Headless Chromium 157, bei **360 × 800** und **1280 × 800**: Planung mit Ergebnissen (2 und 4 Tage, Fenster über Mitternacht), Planung ohne Treffer, umgekehrter/ungültiger Zeitraum (400), Ortssuche mit Treffern, Profilübersicht leer und gefüllt, Profil bearbeiten inklusive Ortssuche, fehlendes Profil, 404, 405, unbekannter Enum-Wert (400), Alt-Parameter `searchDate`, kompletter Ablauf ohne JavaScript (eigener Kontext mit deaktiviertem JavaScript), Persistenz nach Neustart der Anwendung, A2-Platzhalter (dafür `location_name` per H2-Shell auf `NULL` gesetzt).
  - Messungen im Browser: `scrollWidth`/`clientWidth` aller Seiten bei beiden Breiten, Spaltengeometrie, Schriftgrößen/-gewichte, berechnete Farben, Tab-Reihenfolge und `:focus-visible`-Outline, Kontrastrechnung mit eigenem Skript (unabhängig vom Bericht).
- **Nicht ausgeführt:** der optionale Demo-Test `ProfileBrowserTests` (`-Ddemo.browser=true`), weil das npm-Paket `playwright` im Worktree nicht auflösbar ist (`ERR_MODULE_NOT_FOUND`). Denselben Ablauf (Speichern, Bearbeiten, Verwenden, Löschen, 404, mit und ohne JavaScript, 360 px) habe ich über das MCP selbst durchgespielt.
- **Hinweis:** `origin/agent/codex-r2b` existiert und zeigt auf denselben Commit. Ob der Agent selbst gepusht hat, lässt sich nicht feststellen; da alle Agenten-Branches auf `origin` liegen, deutet das eher auf das Experiment-Setup als auf einen Verstoß gegen „nicht pushen“ hin.

## 2. Gesamturteil

**Merge-fähig nach kleinen Änderungen.** Die fachliche Umsetzung von Runde 1 ist vollständig, die Runde-2b-Gestaltung hält sich eng an `system.md`, die Tests sind grün und decken die kniffligen Fälle (Mitternacht, Zeitumstellung, Altprofile, unvollständige API-Daten) gut ab. Vor der Übernahme muss aber ein Tastaturfehler behoben werden, der auf Desktop-Breiten das gesamte Planungsformular aus der Fokusreihenfolge entfernt; die übrigen Befunde sind kleine Politur.

## 3. Befunde

### Befund 1 – hoch – Auf Desktop ist das gesamte Planungsformular per Tastatur nicht erreichbar

- **Datei/Seite:** `src/main/resources/static/css/app.css:379` (`reading-flow: grid-rows` auf `.planner`) zusammen mit `src/main/resources/templates/mainpage.html:44-45` (`tabindex="-1"` am `<form id="planning-form">`); Seite `GET /` bei 1280 px.
- **Was falsch ist:** In Chromium 157 überspringt die Vorwärts-Tab-Navigation das Formular samt aller Bedienelemente, sobald `reading-flow` aktiv ist und das Formular selbst `tabindex="-1"` trägt. Ein Tastaturnutzer erreicht auf Desktop keine Datumsfelder, keine Aktivität, keine Mindestdauer, keine Wetterwünsche, keinen Such- und keinen Speichern-Knopf; nur Navigation und Fußzeile sind erreichbar. Damit ist D3 („Bedienung per Tastatur ist möglich“) und in der Folge Z11 verletzt. Auf 360 px tritt der Fehler nicht auf, weil `reading-flow` erst ab 60 rem greift.
- **Nachweis:** *verifiziert.* Frisches Laden von `http://localhost:8093/` bei 1280 × 800, 30 × `Tab`: `Zum Inhalt → Wann passt es? → Planung → Ortssuche → Suchprofile → Open-Meteo → GeoNames → Body` und wieder von vorn; kein Formularfeld. Mit Ergebnissen zusätzlich das `<summary>` des Zeitfensters, danach ebenfalls die Fußzeile. Entfernt man im DOM nur `tabindex` vom Formular, lautet die Reihenfolge `… → Ort ändern → Startdatum → Enddatum → Aktivität → …`; entfernt man nur `reading-flow`, wird das Formular ebenfalls erreichbar, dann aber nach der Ergebnisfläche. Bei 360 px ist die Reihenfolge ohne Änderung korrekt. Zusatzbeobachtung: Die Tastaturaktivierung von „Suche anpassen“ fokussiert das Formular und zeichnet eine 3-px-Kontur um die gesamte Formularfläche, was wie ein Fehlerzustand wirkt.
- **Vorschlag:** `tabindex="-1"` vom Formular nehmen und stattdessen auf die Überschrift `#planning-title` legen; den Link „Suche anpassen“ auf `#planning-title` zeigen lassen. Diese Variante habe ich im Browser geprüft: Fokusreihenfolge entspricht dann der visuellen Reihenfolge (Formular links zuerst), und der Fokus landet weiterhin sichtbar im Formular. `src/test/browser/profile-flow.mjs:45` muss dafür die erwartete ID anpassen. Alternativ `reading-flow` streichen; dann bleibt die DOM-Reihenfolge (Ergebnisse vor Formular) und damit eine Abweichung zur visuellen Reihenfolge. In beiden Fällen sollte das Browserszenario die Tab-Reihenfolge bei 1280 px prüfen – bisher wird nur einmal getabbt und der Sprunglink geprüft, deshalb ist der Fehler nicht aufgefallen.

### Befund 2 – niedrig – Trennlinien unterschreiten die 3:1-Vorgabe des Designsystems

- **Datei/Seite:** `src/main/resources/static/css/app.css:15` (`--divider: color-mix(in srgb, var(--line) 30%, transparent)`), verwendet u. a. in `:63` (Kopfzeile), `:103` (Fußzeile), `:117` (Panelrahmen), `:194`, `:256`, `:273`, `:325`, `:349`.
- **Was falsch ist:** `system.md` verlangt für „Linien und Feldrahmen“ einen Ton, der gegen Weiß mindestens 3:1 erreicht. Die Feldrahmen (`--line` = `#748497`) erfüllen das mit 3,83:1, die Panels, Tageszeilen und Kopf-/Fußzeilen verwenden aber eine 30-%-Mischung. Gemessen ergibt das `#D5DAE0` und damit nur 1,41:1 gegen Weiß (1,29:1 gegen den Seitenhintergrund). Für rein dekorative Trenner ist das kein WCAG-Verstoß, es weicht aber sichtbar von der Systemvorgabe ab, und der Kontrastbericht führt nur die Feldrahmen auf.
- **Nachweis:** *verifiziert.* Berechneter Rahmen von `.results` im Browser: `color(srgb 0.454902 0.517647 0.592157 / 0.3)`; Kontrastrechnung für die Mischung über Weiß: 1,41:1, über `#F3F6FA`: 1,29:1. Zum Vergleich: eine 90-%-Mischung erreicht 3,25:1, `--line` selbst 3,83:1.
- **Vorschlag:** Für strukturierende Linien (Panelrahmen, Tageszeilen) `--line` direkt verwenden oder die Mischung so weit erhöhen, dass 3:1 erreicht wird; die 30-%-Mischung nur für rein dekorative Trenner behalten und die Abweichung im Bericht mit Wert dokumentieren.

### Befund 3 – niedrig – Beschriftung eines Fensters am Zeitraumende nennt ein Datum außerhalb des gewählten Zeitraums

- **Datei/Seite:** `src/main/java/de/anton/wannpasstes/PlanningTimeline.java:106-110` (Endbeschriftung) und `src/main/java/de/anton/wannpasstes/MainpageController.java:75-79` (die Grenzstunde `rangeEnd` wird bewusst mitgeladen); Seite `GET /?startDate=2026-11-22&endDate=2026-11-23`.
- **Was falsch ist:** Die Beschriftung lautet „So. 22.11. 19:00 – Di. 24.11. 00:00 · 29 Std.“, obwohl der Zeitraum am 23.11. endet. Bei `endDate=2026-11-25` steht entsprechend „Do. 26.11. 00:00 · 77 Std.“. Das Fenster selbst liegt korrekt im Zeitraum – 24.11. 00:00 ist das exklusive Ende des 23.11. –, aber die Beschriftung liest sich wie ein Ergebnis über den gewählten Zeitraum hinaus und widerspricht der sonst verwendeten „24:00“-Konvention am Tagesende. Für die Mindestdauer ist der Randfall unproblematisch, weil das Fenster genau an der Grenze endet.
- **Nachweis:** *verifiziert.* Im Browser bei 360 und 1280 px beobachtet (2-Tage- und 4-Tage-Zeitraum, `minDurationMinutes=90` bzw. `120`, `windPreference=MODERATE`); die Bänder enden am letzten Tag bei 1440, die Beschriftung nennt den Folgetag.
- **Vorschlag:** Wenn `window.end()` exakt dem Zeitraumende entspricht, die Beschriftung als „letzter gewählter Tag + 24:00“ ausgeben (z. B. „Mo. 23.11. 24:00“), statt den Folgetag mit 00:00 zu nennen.

### Befund 4 – niedrig – Ortsschaltflächen mehrerer Treffer haben denselben zugänglichen Namen

- **Datei/Seite:** `src/main/resources/templates/locations.html:37-38`; Seite `/locations?query=Berlin`.
- **Was falsch ist:** Alle Treffer erhalten `aria-label="Ort übernehmen: <Name>"`. Bei fünf Berlin-Treffern (Land Berlin, New Hampshire, New Jersey, Wisconsin, Maryland) heißt jede Schaltfläche gleich „Ort übernehmen: Berlin“. Wer per Screenreader durch Schaltflächen springt, kann die Ziele nicht unterscheiden, obwohl Region und Land im selben Listeneintrag sichtbar sind.
- **Nachweis:** *verifiziert.* Playwright meldet beim Klick auf `.location-result button` eine strict-mode-Verletzung mit fünf identischen Namen „Ort übernehmen: Berlin“; die sichtbaren Texte unterscheiden sich nur in Region und Land.
- **Vorschlag:** Region und ggf. Land in das `aria-label` aufnehmen (z. B. „Ort übernehmen: Berlin, New Hampshire“), oder das Label auf den sichtbaren Text reduzieren und die Unterscheidung über die Listenstruktur belassen.

### Befund 5 – niedrig – Der A2-Platzhalter für Altprofile ist nicht automatisiert abgedeckt

- **Datei/Seite:** `src/main/resources/templates/profiles.html:19`, `src/main/resources/templates/profile-edit.html:22`; Testbaum `src/test/java/de/anton/wannpasstes`.
- **Was falsch ist:** A2 verlangt, dass Altprofile ohne Ortsnamen weiterhin angezeigt und benutzbar bleiben. Die Templates zeigen dafür „Ort <ID>“; kein Test setzt jedoch ein Profil mit `locationName == null` und prüft diese Ausgabe. `ProfileDatabaseUpgradeTests` prüft nur die Entity und den Start, `SearchProfileWebMvcTests` prüft nur die HTML-Escaping-Ausgabe eines Profils. Die Funktion ist vorhanden, ein späterer Regress würde aber unbemerkt bleiben.
- **Nachweis:** *verifiziert für die Lücke* (Suche nach `locationName`/`Ort` in `src/test/java` liefert nur eine Assertion auf `model.get("locationName")`; kein Platzhalter-Test). Die Funktion selbst habe ich manuell geprüft: `location_name` per H2-Shell auf `NULL` gesetzt, danach zeigt `/profiles` und `/profiles/3/edit` „Ort 2950159“; beim Speichern wird der Name über die Orts-API nachgetragen.
- **Vorschlag:** Einen MVC-Test ergänzen, der ein Profil ohne Ortsnamen rendert und den Platzhalter in Übersicht und Bearbeiten prüft.

## 4. Stärken

1. **Saubere Trennung von Fachlogik und Darstellung:** `PlanningTimeline` ist ein reines View-Model; der Controller bleibt schlank, die Zeitfensterlogik ist unangetastet. Die Zeitumstellung ist sowohl für die fehlende Frühlingsstunde als auch für die doppelte Herbststunde durch Unit-Tests abgedeckt – das ist der aufwendigste Teil der Gestaltung und er ist belastbar gelöst.
2. **Robuste API-Verarbeitung:** `WeatherClient` prüft Listenlängen pro Index, verwirft `null`/nicht-endliche Werte, entdoppelt Zeitstempel und bricht erst ab, wenn keine Stunde verwertbar ist. `WeatherClientDataTests` deckt genau diese Fälle ab, statt nur den Normalfall zu testen.
3. **Profilarbeit vollständig und dauerhaft gelöst:** `nameKey` mit Unique-Constraint, Nachprüfung im Service plus `23505`-Fallback, pessimistisches Lock, deterministische Umbenennung von Altprofilen beim Start und ein Test, der eine echte Alt-Datenbank anlegt, die Anwendung zweimal startet und die Persistenz prüft. Das erfüllt A1–A8 über die Testebene hinaus.
4. **Durchgängiges, abhängigkeitsfreies Frontend:** Ein Layout-Fragment, native Formularelemente, `<details>` als zweite Ebene, ein Skript von 22 Zeilen und ein vollständig funktionierender Ablauf ohne JavaScript. Fokusringe, 44-px-Ziele, Sprunglink und die ruhige Leerzustandsgestaltung sind sauber umgesetzt; die gemessenen Kontraste decken sich mit dem Bericht.
5. **Fehlerbehandlung ohne interne Details:** `WebErrorHandler` plus Boot-Fehlertemplate liefern deutsche Seiten für 400/404/405/500, und die HTTP-Tests prüfen ausdrücklich, dass weder Exception-Text noch „Whitelabel“ im HTML landet.

## 5. Übernahmeempfehlung

**Übernehmen:** die gesamte fachliche Umsetzung aus Runde 1 (Profilmodell/Service/Controller, Mehrtages-Zeitraum, Fehlerbehandlung, API-Robustheit), das gemeinsame Layout, `PlanningTimeline` samt Tests, das schlanke CSS-Grundgerüst und den Browserablauf. Die Arbeit ist deutlich über dem Durchschnitt dieser Runde: Alt-Datenbanken, Zeitumstellung und unvollständige API-Antworten sind nicht nur bedacht, sondern getestet.

**Vor der Übernahme ändern:** Befund 1 (Tastaturreihenfolge auf Desktop) ist ein echter Rückschritt gegenüber `TASK.md` D3 und mit einer Zeile plus Ankerziel zu beheben; dabei das Browserszenario um eine Tab-Prüfung bei 1280 px ergänzen. Befund 3 (Mitternachtsbeschriftung am Zeitraumende) und Befund 2 (Trennlinien) sind kleine Korrekturen, Befund 4 und 5 sind Politur bzw. Testlücke.

**Nicht übernehmen:** die 30-%-Mischung für strukturierende Linien in der jetzigen Form und `reading-flow` ohne Tastaturtest – die Eigenschaft ist neu und nur in Chromium wirksam, sodass sie als alleiniger Mechanismus für Reihenfolge zu riskant ist.
