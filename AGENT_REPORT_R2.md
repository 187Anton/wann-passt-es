# Abschlussbericht Runde 2 – Frontend-Neugestaltung

Auftrag: `TASK-R2.md`; Grundlage: `.agents/skills/wpe-design/SKILL.md`,
`AGENTS.md`, `TASK.md`. Geändert wurde ausschließlich die Oberfläche
(Templates, CSS, Browser-Szenario, Dokumentation). Controller, Fachlogik,
Datenmodell, URLs und Request-Parameter sind unverändert.

## Gestaltungskonzept in Kürze

Die Seite beantwortet eine Frage: *wann* ist draußen *wie lange* etwas möglich.
Deshalb steht das Ergebnis nicht als Liste unter dem Formular, sondern als
dunkles „Nachtfenster“ oben rechts (mobil: ganz oben): ein Tagesband, auf dem
jedes passende Zeitfenster als oranger Balken an seiner Stelle im 24-Stunden-Tag
sitzt; Position = Uhrzeit, Breite = Dauer. Drumherum eine weiße Seite mit
Haarlinien, Serifen-Überschriften und genau einer Akzentfarbe (gebranntes
Orange, `#c2410c`, Farbton 17°). Das Formular ist ruhiges Werkzeug daneben, das
Ergebnis ist die Hauptsache. Details, Palette, Schriftskala, ASCII-Skizzen und
die Selbstprüfung gegen den generischen Look stehen in `DESIGN.md`.

## Zuordnung zu den Akzeptanzkriterien

| ID | Status | Umsetzung / Nachweis |
| --- | --- | --- |
| R1 | erfüllt | `DESIGN.md` enthält Leitidee, benannte Palette mit alter/neuer Akzentfarbe (174° → 17°, Abstand 157°), Schriftrollen und sechsstufige Skala, Layoutbeschreibung mit ASCII-Skizzen für 360 px und 1280 px, das prägende Element (Tagesband) und eine Selbstprüfung mit sechs Punkten, die gegen den generischen Look gestrichen bzw. geändert wurden. |
| R2 | erfüllt | Überschriften/`legend` und die Fehlerzahl in `"Iowan Old Style", "Palatino Linotype", Palatino, Charter, "Bitstream Charter", Georgia, serif`; Text, Formulare, Zeiten in `system-ui, -apple-system, "Segoe UI", Roboto, sans-serif`. Keine weitere Familie (im Browser geprüft: `h1` → „Iowan Old Style“, `.slot__time` → „system-ui“). `font-variant-numeric: tabular-nums` auf Zeiten, Datumsangaben, Dauern, Achse, Metazeilen, Zahlenfeldern. Größen-Skala mit genau sechs Stufen (`--size-1` … `--size-6`). |
| R3 | erfüllt | Seitenhintergrund `#ffffff` (`--paper`), kein Cremeton. Die dunkle Fläche ist ausschließlich das Ergebnisband (`#17120f`). |
| R4 | erfüllt | Genau eine Akzentfarbe `#c2410c` (Farbton 17°, außerhalb 90°–200°) mit Abstufungen (`#8f2d08` für Links/Text, `#f0864e` für Balken auf dunklem Grund, `#f7e9e0` als Wäsche). Bisherige Akzentfarbe `#0f5f57` (Farbton 174°), Abstand **157°** (≥ 40°). Beide Werte in `DESIGN.md`. |
| R5 | erfüllt | Kein Karten-Kit (Haarlinien und Typografie statt Kacheln), genau zwei Eckenradien (`3px`, `12px`; Ladeanzeige als 2-px-Linie statt Kreis), keine Versal-Labels (Eyebrow, `text-transform: uppercase` entfernt), keine Mittelpunkt-Reihungen (auch die Metazeilen nutzen Kommas oder eigene Zeilen), keine Pfeile an Buttons, keine Emojis, keine Farbverläufe (`grep gradient` leer). Symbole nur als Inline-SVG (Sonnenmarke, Standortnadel). |
| R6 | erfüllt | Bei 1280 × 800 liegt das erste Zeitfenster bei y = 343–470 px, der Leerzustand bei y = 320–475 px, jeweils vollständig im Fenster (gemessen im Browser). |
| R7 | erfüllt | 360 px und 1280 px: `document.documentElement.scrollWidth − clientWidth` = 0 auf allen sieben geprüften Seiten; keine Elemente außerhalb des Viewports; keine abgeschnittenen Labels, Buttons oder Karten (Skriptprüfung über `scrollWidth > clientWidth`). Zusätzlich geprüft bei 380, 480, 640, 768, 900, 1024 und 1440 px. |
| R8 | erfüllt | Rechnerisch geprüft (WCAG-Formel) und im Browser über 272 Textknoten nachgemessen, siehe Kontrasttabelle unten. |
| R9 | erfüllt | Prüfprotokoll unten: sieben Seiten × zwei Breiten, Vorgehen, gefundene und behobene Probleme. |
| R10 | erfüllt | `./mvnw test`: 80 Tests, 0 Fehler, 3 übersprungen (die standardmäßig deaktivierten Demo-Klassen). Browser-Szenario separat ausgeführt: grün. `profile-flow.mjs` an das neue Markup angepasst und um Prüfungen für 1280 px, Bearbeiten-Seite, 404 und die Sichtbarkeit des Ergebnisblocks erweitert. |
| R11 | erfüllt | Diese Datei. |
| R12 | erfüllt | Vier Commits auf `agent/hermes-deepseek-r2`, nichts gepusht. |

### Kontrastwerte (rechnerisch, WCAG 2.x)

| Paar | Verhältnis | Anforderung |
| --- | --- | --- |
| Tinte `#1a1512` auf Weiß | 18,10 : 1 | 4,5 : 1 |
| Sekundärtext `#5b5249` auf Weiß | 7,64 : 1 | 4,5 : 1 |
| Link/Tief `#8f2d08` auf Weiß | 8,25 : 1 | 4,5 : 1 |
| Weiß auf Akzent `#c2410c` (Primary-Button) | 5,18 : 1 | 4,5 : 1 |
| Feldrahmen `#8a7f73` auf Weiß | 3,92 : 1 | 3 : 1 (Bedienelement) |
| Fokusring `#c2410c` auf Weiß | 5,18 : 1 | 3 : 1 |
| Helltext `#f6f0e9` auf Nacht `#17120f` | 16,43 : 1 | 4,5 : 1 |
| Sekundärtext `#c7bcb0` auf Nacht | 9,96 : 1 | 4,5 : 1 |
| Balken `#f0864e` auf Nacht | 7,28 : 1 | 3 : 1 (Grafik) |
| Fehler `#a3201a` auf Weiß | 7,56 : 1 | 4,5 : 1 |
| Erfolg `#2e6b3d` auf Weiß | 6,39 : 1 | 4,5 : 1 |

Zusätzlich im Browser über alle Seiten: 272 Textknoten geprüft (Textfarbe gegen
tatsächlichen Hintergrund, nach Vererbung und Transparenz), **kein** Wert unter
4,5 : 1 (bzw. 3 : 1 bei großer Schrift); der kleinste Wert ist der
Primary-Button mit 5,18 : 1. Die große Fehlerzahl (`clamp` 72–128 px) nutzt
`#c2410c` auf Weiß, 5,18 : 1 (Anforderung 3 : 1).

## Prüfprotokoll der visuellen Kontrolle

**Werkzeug.** Der Playwright-MCP-Server dieser Sitzung ließ sich nicht starten
(`Chromium distribution 'chrome' is not found at /Applications/Google Chrome.app`),
weil dort der Chrome-Kanal konfiguriert ist, im System aber nur die
Chromium-Builds von Playwright liegen. Geprüft wurde deshalb mit derselben
Engine über die Playwright-Node-API und das installierte Chromium
(`chromium_headless_shell-1254`), also mit echtem Browser-Rendering, echter
Schrift- und Layoutberechnung. Das Browser-Szenario des Projekts nutzt genau
diese Engine.

**Vorgehen je Seite und Breite (360 × 800 und 1280 × 800).**

1. Seite laden, Screenshot des gesamten Dokuments ablegen (Ablage:
   `/Users/anton/.hermes/cache/scratch/shots/`, nicht im Repository).
2. Messen: horizontaler Überlauf des Dokuments, Elemente außerhalb des
   Viewports, `scrollWidth > clientWidth` für Links, Buttons, Felder,
   Auswahllisten, Überschriften, Absätze, Datendefinitionen und Legenden.
3. Geometrie: Position und Höhe von Kopfzeile, Seitenkopf, Formularspalte,
   Ergebnisband, erstem Zeitfenster und Leerzustand.
4. Stile: berechnete Schriftfamilie/-größe der Überschrift, der Uhrzeit und des
   Fließtexts; Fokusring jedes Bedienelements per `focus()`.
5. Screenshot als Helligkeitskarte (100 × n Zeichen) ausgewertet, um die
   Komposition (dunkle Fläche rechts oben, Textblöcke links, Balkenlagen)
   räumlich zu kontrollieren.

Geprüfte Seiten: Planung mit Ergebnissen (mehrere Tage, sechs Fenster),
Planung ohne Treffer, Planung mit ungültigem Zeitraum, Ortssuche mit Treffern,
Profilübersicht mit vier Profilen (darunter ein 76 Zeichen langer Name und ein
langer Ortsname), Profil bearbeiten, 404 sowie zusätzlich 400 (ungültiger
Enum-Wert) und 405.

**Gefundene und behobene Probleme.**

1. **Tagesachse war falsch skaliert.** `justify-content: space-between` setzte
   die Beschriftungen 0/6/12/18/24 auf ca. 0 / 39 / 58 / 79 / 98 % der Breite,
   die Balken dagegen auf 0 / 25 / 50 / 75 / 100 %. Die Achse log also
   gegenüber den Balken. Behoben durch absolute Positionierung mit
   `translateX(-50%)` an 0/25/50/75/100 %; nachgemessen: 0 / 25 / 50 %.
2. **Leerzustand-Überschrift war zu klein und in der falschen Schrift.**
   `.panel-empty p` (Spezifität 0,1,1) überschrieb `.panel-empty__title`
   (0,1,0): 15 px statt 22 px Serife. Gleicher Fehler bei `.empty__title` auf
   Profil- und Ortssuche-Seite. Behoben über `p:not(...)`-Selektoren; im
   Browser nachgemessen: 22 px, Serife, korrekte Farbe.
3. **Dritter Eckenradius.** Der Ladezustand des Submit-Buttons war ein Kreis
   (`border-radius: 50%`) und damit ein dritter Radius im Projekt. Ersetzt
   durch eine 2-px-Fortschrittslinie am Buttonfuß (Animation, bei
   `prefers-reduced-motion` statisch).
4. **Fehlermeldung zeigte einen ungültigen Zeitraum als gültig.** Bei
   „Ende vor Start“ stand im Bandkopf zusätzlich „26.11.2026 – 22.11.2026“.
   Die Metazeile erscheint jetzt nur ohne Fehler; die Meldung selbst nennt
   beide Daten.
5. **Sehr breite Bedienelemente auf den einspaltigen Seiten.** Auf der
   Bearbeiten-Seite waren Auswahllisten und Zahlenfeld 780 px breit. Jetzt:
   Auswahllisten max. 24 rem, Zahlenfeld 10 rem, Namensfeld bewusst volle
   Breite (sonst würden lange Namen abgeschnitten).
6. **Abgeschnittener Profilname im Feld.** Bei 1280 px war der Name in einem
   384 px breiten Feld abgeschnitten; das Namensfeld ist jetzt volle Breite,
   zusätzlich `text-overflow: ellipsis` als sichtbares Signal bei 360 px.
7. **Reihenfolge für Screenreader.** Die Dauer stand im DOM zwischen Datum und
   Uhrzeit („Datum, 120 Minuten, 18:00–19:00“). DOM-Reihenfolge jetzt Datum →
   Uhrzeit → Dauer, die Dauer wird per CSS-Raster oben rechts platziert.
   Zusätzlich liest die Uhrzeit dank verstecktem „bis“ als „18:00 bis 19:00“.
8. **Doppelte Abstände bei Meldungen** (`.alert`-Rand plus Rasterabstand) und
   fehlendes `position: relative` am Button nach dem Umbau der Ladeanzeige –
   beide korrigiert.
9. **Ausrichtung der Bandkopf-Metazeile.** Ab 480 px rechtsbündig, damit die
   Zeilen mit der Innenkante des Bands abschließen.

**Nachkontrolle.** Nach jeder Korrektur wurden Messung und Helligkeitskarte
wiederholt. Endstand: 0 px Überlauf, 0 abgeschnittene Elemente, 0 Elemente
außerhalb des Viewports auf allen Seiten bei 360 und 1280 px; erstes
Zeitfenster bei 1280 × 800 vollständig sichtbar (343–470 px).

## Ausgeführte Prüfungen

| Prüfung | Ergebnis |
| --- | --- |
| `./mvnw test` | 80 Tests, 0 Fehlschläge, 0 Fehler, 3 übersprungen (BUILD SUCCESS) |
| `./mvnw test -Dtest=ProfileBrowserTests -Ddemo.browser=true` (mit `PLAYWRIGHT_MODULE` und `PLAYWRIGHT_CHROMIUM_EXECUTABLE`) | 1 Test, grün: Speichern, Bearbeiten, Verwenden, Löschen mit und ohne JavaScript, kein horizontales Scrollen bei 360 und 1280 px, Leerzustand im 800-px-Fenster, 404 mit Rückweg |
| Layoutmessung 360 × 800 / 1280 × 800, sieben Seiten | 0 px horizontaler Überlauf, 0 abgeschnittene Elemente, 0 Elemente außerhalb |
| Layoutmessung 380/480/640/768/900/1024/1440 px | 0 px Überlauf, 0 abgeschnittene Elemente |
| Kontrast (rechnerisch + 272 Textknoten im Browser) | alle ≥ 4,5 : 1 bzw. ≥ 3 : 1 |
| Fokusprüfung (alle Links, Buttons, Felder, Auswahllisten) | überall `3px solid rgb(194, 65, 12)`, Offset 2 px |
| Struktur/A11y-Stichprobe (doppelte IDs, unbeschriftete Felder, Überschriftenfolge, `lang`) | keine Befunde |
| Start auf Port aus `.port` (8093) | Anwendung lief, alle Seiten per HTTP erreichbar |

Balkengeometrie stichprobenartig nachgemessen (1280 px, Bandbreite 624 px):
18:00 → 75,0 %, 21:00–24:00 → 87,5 %/12,5 %, 02:00–17:00 → 8,3 %/62,5 % – die
Balken sitzen also maßstabsgetreu auf der Achse.

## Eigene Entscheidungen und Annahmen

- **Ergebnis vor Formular.** Auf schmalen Bildschirmen steht das Band über dem
  Formular (CSS-`order`), ab 900 px zweispaltig rechts. Das erfüllt „Ergebnisse
  zuerst“ ohne die Tab-Reihenfolge zu verbiegen: die DOM-Reihenfolge bleibt
  Ergebnis → Formular, die optische Reihenfolge ebenso.
- **Achse pro Zeile, Beschriftung einmal.** Jedes Zeitfenster hat eine eigene
  0–24-Uhr-Linie; die Beschriftung steht einmal unter allen Zeilen. So bleibt
  die Balkenlage auch bei vielen Fenstern lesbar, ohne 24 Zahlen pro Zeile.
- **Nur Template-Logik.** Balkenposition und -breite werden in Thymeleaf aus
  Startzeit und Dauer berechnet (Permille, `calc()`), der Controller bleibt
  unangetastet. Ein Fenster über Mitternacht wird am Tagesende abgeschnitten
  und zusätzlich als „bis …, 02:00 Uhr“ ausgeschrieben.
- **Akzent Orange.** Wetter, Sonne, Abendlicht – und mit 157° Abstand weit weg
  vom alten Petrol. Grün bleibt ausschließlich der Erfolgsmeldung vorbehalten.
- **Zwei Radien, ein Schatten.** `3px` für Bedienelemente, `12px` für das
  Ergebnisband; Schatten nur dort, weil es über der Seite liegt.
- **Schriftgrößen-Skala** mit sechs Stufen; die Uhrzeit (30 px) ist die größte
  Zahl auf der Seite, die Seitenüberschrift (32–44 px) die größte Überschrift.
- **Fehlerseite ohne Karte.** Die Statuszahl in Serife ist das eine mutige
  Element, linksbündig statt zentriert.
- **Kein JavaScript nötig.** `app.js` bleibt reine Verbesserung
  (Löschbestätigung, Ladezustand, Enddatum-Minimum).

## Bekannte Lücken und Risiken

- **Mehrtägige Fenster** werden auf der Achse des Starttages gezeigt und am
  Tagesende abgeschnitten, mit Textzeile „bis …“. Eine echte mehrteilige
  Darstellung bräuchte eine Gruppierung im Controller (bewusst nicht angefasst).
  Bei den Live-Testdaten tritt das auf, weil die Wetterlage ganze Tage am Stück
  passt; die Anzeige bleibt korrekt, wirkt aber bei 37-Stunden-Fenstern
  sperrig (Uhrzeit „11:00 – 00:00“ plus „bis …“-Zeile).
- **Sehr lange Profilnamen** werden in einzeiligen Eingabefeldern bei 360 px
  mit Auslassungspunkten gekürzt – das ist Systemverhalten von `input`, kein
  Layoutfehler; der volle Name steht in der Übersicht.
- **Keine Bildbetrachtung mit menschlichem Auge.** Die Prüfung stützt sich auf
  Browser-Rendering, DOM-Geometrie, berechnete Stile, Kontrastrechnung und
  Helligkeitskarten der Screenshots. Feine ästhetische Fragen (z. B. ob die
  Serife auf einem bestimmten System zu schwer wirkt) sind damit nur
  eingeschränkt beurteilbar; die Screenshots liegen unter
  `/Users/anton/.hermes/cache/scratch/shots/` zur Sichtprüfung bereit.
- **Rechte Spalte bleibt unten leer**, weil das Formular höher ist als das
  Ergebnisband. Bewusst nicht durch ein Sticky-Band gelöst: bei vielen Fenstern
  wäre der untere Teil dann nicht erreichbar.
- **Kein Dark Mode** und keine `prefers-color-scheme`-Variante; die dunkle
  Fläche ist Gestaltungselement, nicht Modus.
- Der Testbetrieb nutzte die Live-API von Open-Meteo (nur für die manuelle
  Sichtprüfung, nicht für `./mvnw test`); die Ergebnisse hängen daher vom
  Wetter am Prüftag ab. Für Fenster mit mehreren Treffern wurde mit
  `temperaturePreference=ANY`, `windPreference=ANY` bzw.
  `precipitationPreference=DRY` gearbeitet.

## Nächste Schritte (Vorschlag)

1. Mehrtägige Fenster im Band auf mehrere Tageszeilen aufteilen (dafür eine
   Gruppierung im Controller oder eine kleine View-Hilfsklasse).
2. Fehler bei ungültigem Zeitraum zusätzlich am Feld markieren
   (`aria-invalid`, Fehlertext am Datumsfeld).
3. Ergebnisband bei sehr vielen Treffern einklappen („weitere anzeigen“) –
   ohne JavaScript als reine Sprungmarke.
4. Prüfung der Serifendarstellung auf Windows/Linux (Georgia-Fallback) und
   Feinjustierung der Zeilenabstände dort.
