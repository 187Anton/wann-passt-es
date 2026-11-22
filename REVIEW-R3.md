# Review Runde 3 · „Wann passt es?“

## Geprüfter Stand

- **Ziel-Branch** `agent/claude-opus-r2b`, Commit `ea4dd1d4b1fc3d1d97a9106135846b78de292103`. **Datum** 22.11.2026. Prüfumfang war der Diff ab `agentic-baseline` gemäß `TASK-R3.md` sowie die Abnahme nach `TASK.md`, `TASK-R2B.md` und `.interface-design/system.md`.
- **Tests** im temporären, abgelösten Worktree mit Java 21. `./mvnw test` meldete 123 Tests, davon 120 bestanden, 0 fehlgeschlagen, 0 Fehler, 3 standardmäßig übersprungen. `./mvnw -Ddemo.browser=true -Dtest=ProfileBrowserTests test` bestand separat mit lokalem Chromium (1 Test, 0 Fehler, 0 übersprungen). `git diff --check` blieb ohne Befund.
- **Browser und HTTP** über Playwright-MCP mit Chromium bei 360 × 800 und 1280 × 800. Die Anwendung lief auf Port 8091 aus `.port` mit synthetischen Orts- und Wetterdaten und In-Memory-H2. Aufgerufen, vermessen und gesichtet wurden Planung mit mehrtägigen Ergebnissen, ohne Treffer und mit ungültigem Zeitraum, Ortssuche mit Treffern, Profilübersicht, Profilbearbeitung und 404. Zusätzlich geprüft wurden Wetterdetails per Tastatur, der Profilablauf ohne JavaScript, Ortswechsel mit und ohne JavaScript, parallele Profilanlage und ein Java-Probelauf zur Zeitumstellung.
- **Validierung und Ausgabe** per HTTP, Testlauf und Codeprüfung. Unter anderem lieferte eine Mindestdauer von `0` die deutschsprachige 400-Seite. HTML-Sonderzeichen einer Ortssuchanfrage wurden als Text ausgegeben. Die geprüften Templates verwenden `th:text`, kein `th:utext` und kein Inline-JavaScript. Inline-Stile in den Tagesbändern erhalten berechnete Prozentwerte. Der HTTP-Test für unerwartete Fehler bestätigte die gestaltete 500-Seite ohne interne Details.
- **Layout und Kontrast.** Bei 1280 px begannen das weiße 320-px-Suchpanel und die ebenfalls weiße Ergebnisfläche beide bei y = 97 px. Die erste Tageszeile begann bei y = 285 px, der Leerzustand bei y = 209 px. Die hellen Tagesbänder und die Überschrift führen den Blick zum Ergebnis. Bei 360 px folgte das Formular auf die Ergebnisse. Alle sichtbaren Eingabefelder der geprüften Ansichten hatten verknüpfte Labels. Nachrechnung anhand der gerenderten CSS-Werte ergab unter anderem 5,16:1 für Sekundärtext auf der Auswahlfläche, 6,69:1 für weißen Text auf dem Hauptbutton, 3,34:1 für Feldrahmen auf Feldhintergrund und 5,77:1 für den Fokusrahmen auf der Auswahlfläche.

## Gesamturteil

**nicht merge-fähig.** Gleichzeitige Speichervorgänge verletzen die geforderte Eindeutigkeit von Profilnamen und hinterlassen dauerhaft doppelte Datensätze. Die Tagesbänder, die Fehlerseiten und die bestehende Testabdeckung sind eine gute Grundlage, doch der Ortswechsel ohne JavaScript, der mobile Tastaturfokus und die Ergebnisanzeige zur Zeitumstellung brauchen Korrekturen.

## Befunde

### Hoch · Paralleles Speichern erzeugt doppelte Profilnamen (A3, A4, A6)

- **Stelle** `SearchProfileController.java:81–117`, `SearchProfileEditController.java:82–114`, `SearchProfileRepository.java:14–25`, `SearchProfile.java:17–31`.
- **Problem** Die Namensabfrage und das Speichern erfolgen getrennt. Die Datenbank lässt denselben normalisierten Namen mehrfach zu. So können etwa zwei Tabs denselben Namen erfolgreich speichern. `SearchProfileUniquenessJpaTests.java:27–37` prüft den Vergleich vorhandener Namen, aber keine konkurrierenden Schreibvorgänge.
- **Nachweis – verifiziert.** Zwölf parallele `POST /profiles` mit abwechselnd `  R3 Parallel  ` und `r3 parallel` erhielten alle `302` zur Übersicht. Dort erschienen zwölf Einträge. Eine anschließende Einzelanfrage mit `R3 PARALLEL` wurde über den regulären Duplikatpfad abgewiesen.
- **Vorschlag** Einen eindeutig indizierten, einheitlich normalisierten Namensschlüssel einführen. Bei der Migration bestehende Namenskonflikte unter Erhalt aller Profile auflösen und Verletzungen der Eindeutigkeit in die vorhandene Formularmeldung übersetzen. Einen Test mit konkurrierenden Speichervorgängen ergänzen.

### Mittel · Ortswechsel verwirft aktuelle Formulareingaben ohne JavaScript (C4, D5, Z11)

- **Stelle** `mainpage.html:205–230`, `profile-edit.html:28–55`, `app.js:8–26`.
- **Problem** Ortssuche und Planung liegen in getrennten Formularen. Nur JavaScript kopiert die aktuell eingegebenen Werte in die versteckten Felder. Die Oberfläche verspricht, dass die Angaben erhalten bleiben. Die MVC-Tests prüfen bereits übermittelte Werte (`LocationSearchWebMvcTests.java:47–80` und `MainpageWebMvcTests.java:267–291`).
- **Nachweis – verifiziert.** Enddatum vom 23.11. auf den 24.11. geändert, Radfahren, 125 Minuten und mäßigen Wind gewählt. Anschließend ohne vorherige Planung einen anderen Ort gesucht und ausgewählt. Mit JavaScript wurden alle vier Änderungen übernommen. Ohne JavaScript erschienen wieder der 23.11., Spazierengehen, 60 Minuten und schwacher Wind. Ein zuvor abgeschickter Zeitraum blieb auch ohne Skript erhalten.
- **Vorschlag** Die Ortssuche über das Planungsformular mit `formaction` absenden und die Ortsauswahl aus den tatsächlich übermittelten Feldern aufbauen. Die Profilbearbeitung analog anbinden und beide Wege ohne JavaScript testen.

### Mittel · Die klebende Suchleiste verdeckt den Tastaturfokus (D3, Z11)

- **Stelle** Planung bei 360 × 800, `app.css:456–470` und `app.css:627–635`.
- **Problem** Die opake Leiste „Zeitfenster suchen“ liegt über der fokussierten Temperatur-Option. Sie verdeckt einen Teil des Optionsfelds und seines Fokusrahmens.
- **Nachweis – verifiziert.** Bei 360 × 800 von „Aktivität“ zweimal `Tab` gedrückt. Der Fokus lag auf der Temperatur-Option „Egal“. Die Optionszeile lag bei y = 714–754 px, die darüberliegende Leiste begann bei y = 733 px. Der untere Teil des Optionsfelds und der Zeile waren in Chromium verdeckt.
- **Vorschlag** Die Leiste auf schmalen Bildschirmen in den normalen Formularfluss setzen oder beim Fokussieren ausreichend Scrollabstand für die Leiste reservieren. Mit Tab und Pfeiltasten nachprüfen.

### Mittel · Lange Ortsnamen sprengen das Layout der Trefferliste (D2, Z8)

- **Stelle** Ortssuche, `locations.html:50–60`, `app.css:83` und `app.css:1063–1115`.
- **Problem** Ein langer Ortsname ohne geeignete Umbruchstelle vergrößert die Mindestbreite des Grids. Dadurch ragen auch andere Elemente der Seite aus dem Viewport.
- **Nachweis – verifiziert.** Die Ortsdaten enthielten den synthetischen Treffer `Testort ` gefolgt von 90 `W`. In Chromium lag `document.documentElement.scrollWidth` bei 1.502 px für einen 360-px-Viewport und bei 1.774 px für einen 1.280-px-Viewport. Nach Entfernen dieses einen Treffers im Browser entsprachen die Breiten wieder dem Viewport.
- **Vorschlag** Die Grid-Spalten auf `minmax(0, 1fr)` begrenzen und `overflow-wrap: anywhere` für Treffertexte verwenden. Einen langen Treffer in den Layouttest aufnehmen.

### Mittel · Die doppelte Stunde bei der Zeitumstellung erscheint als Nullbreite (B4, Z4)

- **Stelle** `ResultView.java:167–175`, `ResultView.java:196–216` und `ResultView.java:282–285`. Das CSS setzt für jedes Segment eine Mindestbreite von 4 px (`app.css:819–826`).
- **Problem** Beim Zurückstellen der Uhr können Anfang und Ende dieselbe Ortsuhrzeit haben und trotzdem eine Stunde auseinanderliegen. Die Umrechnung in Minuten seit Mitternacht verliert den Offset. Das Band zeigt deshalb nur die CSS-Mindestbreite, während der Text `02:00–02:00 · 1 Std.` meldet.
- **Nachweis – verifiziert.** `TimeWindowService` und `ResultView.of` mit zwei passenden Stundenpunkten am 25.10.2026 in `Europe/Berlin` aufgerufen (`02:00+02:00` und `02:00+01:00`, Mindestdauer 60 Minuten). Ergebnis des Java-Probelaufs war `02:00–02:00 | 1 Std. | … widthPercent=0.000 …`.
- **Vorschlag** Die erste und zweite Ausprägung der wiederholten Stunde im Band unterscheiden und die Endpunkte im Text mit Sommer- bzw. Normalzeit kennzeichnen. Beide Zeitumstellungen mit festen Zeitstempeln testen.

### Niedrig · Das Präsentationsmodell hält zwei Gruppierungen parallel vor

- **Stelle** `ResultView.java:115–147`, `MainpageController.java:143–144` und `MainpageController.java:193–198`.
- **Problem** Das Template verwendet `days()` und `result.periodLabel()`. Daneben liefert der Controller noch einen separat formatierten `periodLabel`, und `ResultView` baut zusätzlich `groups()` auf. Im Zielstand verwenden nur die Tests `groups()`. Damit hält der Produktionscode zwei Datumsformate und zwei Repräsentationen derselben Fenster vor.
- **Nachweis – verifiziert.** `git grep -n -E 'periodLabel|\.groups\(' HEAD -- src` zeigt die Template-Nutzung und die parallelen Modellpfade.
- **Vorschlag** Die Tageszeilen als einzige Gruppierung verwenden und die Fenstertests auf deren öffentliche Struktur und das gerenderte HTML ausrichten. Das ungenutzte Controller-Attribut entfernen.

## Stärken

- `SearchPeriod` bündelt die Vorhersagegrenzen und die Kompatibilität mit `searchDate`. Tests sichern ab, dass ein ungültiger Zeitraum keine Wetterabfrage auslöst.
- Die Zeitfensterberechnung zählt ein Fenster über Mitternacht einmal und wendet die Mindestdauer auf das Gesamtfenster an. Im Browser war der Übergang in beiden Tagesbändern sichtbar. Die aufklappbaren Wetterwerte halten den ersten Blick auf das Ergebnis frei.
- Das gemeinsame Layout mit Systemschriften, Farbvariablen, nativen Formularfeldern und Quellenfuß ist konsistent. Leerzustand und Fehlerzustand sind auch sprachlich klar erkennbar.
- Die Tests mit Datei-H2 und Neustart belegen, dass ein Altprofil ohne gespeicherten Ortsnamen erhalten bleibt und Änderungen nach einem Neustart geladen werden. Die HTTP-Tests prüfen die tatsächliche Ausgabe der Fehlerseiten.

## Übernahmeempfehlung

Das gemeinsame Layout, die Tagesbänder mit nativen Wetterdetails, die Zeitraumvalidierung, die fehlertolerante Verarbeitung der Wetterdaten und die deutschsprachigen Fehlerseiten als Grundlage übernehmen. Vor der Übernahme des Gesamtstands die Datenbank-Eindeutigkeit absichern, die aktuelle Auswahl beim Ortswechsel serverseitig übernehmen und Fokus, Umbrüche sowie Zeitumstellung korrigieren. Die bestehenden Integrations- und Browsertests um die reproduzierten Randfälle erweitern.
