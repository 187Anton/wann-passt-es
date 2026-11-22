# AGENT_REPORT_R2B – Frontend nach dem Designsystem „Himmel & Schiefer“

Runde 2b gestaltet ausschließlich das Frontend neu. Fachlogik, Datenmodell, Wetterregeln,
URLs, HTTP-Methoden und Request-Parameter bleiben unverändert. `pom.xml` wurde nicht
angefasst; es gibt keine neuen Abhängigkeiten, keine Frameworks, keine CDNs, keine
Webfonts und keine externen Assets. `AGENTS.md`, `CLAUDE.md`, `TASK.md`, `TASK-R2B.md`,
`.interface-design/system.md` und der Skill blieben unverändert.

## Gestaltungskonzept in wenigen Sätzen

Die Oberfläche ist ein heller, ruhiger Wetterplaner: eine Fläche in einem Farbton, feine
Linien statt Schattenberge, viel Weißraum und Systemschrift. Der einzige laute Akzent ist
die Hauptaktion; sonst erscheint Blau nur für Fokus, aktuelle Auswahl und die Zeitfenster.
Das prägende Element ist das Ergebnisband: eine Zeile pro Tag auf derselben 0–24-Uhr-Skala,
auf der die Zeitfenster als hellblaue Intervalle untereinander vergleichbar sind. Auf breiten
Bildschirmen steht die Suchspalte (320 px) links neben der Ergebnisfläche, beide als gleich
helle Flächen auf derselben Höhe; auf schmalen Bildschirmen folgen Zusammenfassung,
Ergebnisse und erst dann das Formular.

## Umsetzung und Zuordnung zu den Akzeptanzkriterien

| ID | Status | Umsetzung |
| --- | --- | --- |
| Z1 | erfüllt | Alle Vorgabenfarben sind CSS-Variablen (`--bg-page`, `--surface`, `--surface-inset`, `--text`, `--text-secondary`, `--accent`, `--accent-soft`, `--window`, `--success`, `--warning`, `--error`, `--line`). Tints für Hinweise entstehen ausschließlich per `color-mix()` aus diesen Farben, keine neuen Farbtöne, keine Verläufe, keine dunklen oder vollflächig gesättigten Ergebnisblöcke. |
| Z2 | erfüllt | Systemschrift-Stack wie vorgegeben; Größen 16/24, 14/20, 22/28, 32/40; Gewichte 400/500/600; Abstände ausschließlich 4/8/12/16/24/32/48; Radien 8 und 12 px. Ausnahme nur 50 % für runde Icon-Marken (kein Flächenradius). Uhrzeiten, Daten und Dauern mit `tabular-nums`. |
| Z3 | erfüllt | Ab 960 px zweispaltig: 320 px Suchspalte links, flexible Ergebnisfläche rechts, beide `#FFFFFF`, gleiche Oberkante (bei 1280 px beide y = 236). Darunter DOM- und Sichtreihenfolge: Zusammenfassung → Ergebnisse → Formular. |
| Z4 | erfüllt | Eine `<li>`-Zeile pro Tag im Zeitraum, gemeinsame Skala 0/6/12/18/24 Uhr. Jedes Fenster als Segment im Band plus Text mit Beginn, Ende und Dauer („17:00–20:30 · 3 Std. 30 Min.“). Fenster über Mitternacht erscheinen in beiden Tageszeilen; das Startsegment trägt das eine Label („Sa 19:00 – Mo 00:00 · 29 Std.“), Folgetage „Fortsetzung · bis Mo 00:00“. Ende exakt um Mitternacht heißt „24:00“. |
| Z5 | erfüllt | Wetterdetails je Fenster als natives `<details>` mit Temperaturbereich, maximalem Wind und Niederschlag. Vergangene Stunden des heutigen Tages sind dezent grau hinterlegt, mit „Jetzt“-Linie und Legende. |
| Z6 | erfüllt | Leerzustand als ruhige, eingelassene Fläche mit drei konkreten Vorschlägen (Mindestdauer, Wetterwünsche, Zeitraum), ohne Rot. Rot bleibt echten Fehlern vorbehalten (ungültiger Zeitraum, nicht ladbare Daten, fehlgeschlagene Aktion). |
| Z7 | erfüllt | Bei 1280 × 800: Ergebnisbereich beginnt bei y = 236, erste Tageszeile 341–489, Leerzustand 305–609 – jeweils vollständig ohne Scrollen sichtbar. |
| Z8 | erfüllt | Bei 360 und 1280 px auf allen geprüften Seiten kein horizontales Scrollen (`scrollWidth == clientWidth`). Lange Orts-/Profilnamen brechen um (`overflow-wrap`); einzige interne Scrollflächen sind native Textfelder mit langem Wert. |
| Z9 | erfüllt | Kontraste rechnerisch geprüft, siehe Tabelle unten: Text ≥ 4,5:1, große Schrift ≥ 3:1, Feldrahmen 3,22–3,70:1, Fokus 5,77–6,69:1. |
| Z10 | erfüllt | Alle sieben geforderten Seiten bei 360 × 800 und 1280 × 800 mit dem Playwright-MCP geöffnet, vermessen und als Screenshot geprüft; gefundene Punkte korrigiert und erneut geprüft. Prüfprotokoll und Antwort zur Gleichgewichtsfrage unten. |
| Z11 | erfüllt | `./mvnw test` grün (89 Tests, 0 Fehler, 3 übersprungen – die beiden Demo-Klassen). Zusätzlich `ProfileBrowserTests` mit `-Ddemo.browser=true` ausgeführt: bestanden. Alle Funktionen aus `TASK.md` weiterhin vorhanden. |
| Z12 | erfüllt | Dieser Bericht, vier nachvollziehbare Commits auf `agent/hermes-deepseek-r2b`, nichts gepusht. |

### Stand von `TASK.md`

Alle Pakete A–D und die allgemeine Abnahme bleiben erfüllt. Die Planung zeigt weiterhin
alle Zeitfenster des Zeitraums chronologisch, die Ortssuche übernimmt die Auswahl, Profile
lassen sich speichern, verwenden, bearbeiten und löschen (auch ohne JavaScript), ungültige
Eingaben enden auf gestalteten deutschen Fehlerseiten. Die bestehende Testsuite sichert das
ab; sie wurde bis auf die unten genannten UI-Anpassungen nicht verändert.

## Eigene Entscheidungen und Annahmen

1. **Präsentationsschicht statt Fachlogik.** Die Tageszeilen entstehen in der neuen Klasse
   `PlanningTimeline` (nur Formatierung und Positionierung). `TimeWindowService`,
   `TimeWindow`, `WeatherHour` und die Regeln blieben unverändert. `suitableWindows` bleibt
   als fachliches Ergebnis im Model, `resultDays` kommt für die Darstellung hinzu. Der
   Controller wurde nur erweitert, weil das Template diese Daten zwingend braucht.
2. **Wetterdetails aus den Stundenwerten im Fenster.** Temperatur minimum/maximum, maximaler
   Wind und maximaler Niederschlag werden aus den ohnehin geladenen Stundenwerten aggregiert
   (keine neue Abfrage, keine neue Regel). Fehlen Stunden, entfällt das Detail.
3. **24:00-Konvention.** Endet ein Fenster in der Folgenacht um 00:00, steht „17:00–24:00“.
   Überspannt es mehrere Tage, steht der echte Endtag („Sa 19:00 – Mo 00:00“), wie im
   Designsystem-Beispiel. Das Startsegment trägt das vollständige Label, Folgesegmente nur
   „Fortsetzung · bis …“, damit das eine Fenster klar bleibt und nicht siebenmal wiederholt wird.
4. **Reihenfolge im DOM.** Zusammenfassung → Ergebnisse → Formular, weil das die geforderte
   mobile Reihenfolge ist und die visuelle Reihenfolge auch für Tastatur und Screenreader
   konsistent bleibt. Auf Desktops ordnet das Grid die Suchspalte nach links; die Tabulatur
   beginnt daher bei den Ergebnissen. Das ist bewusst so, weil die Seite zuerst das Ergebnis
   zeigt und die Suche daneben eine Werkzeugspalte ist.
5. **Mobile Zusammenfassung statt doppelter Ortsangabe.** Unter 960 px ersetzt die
   Zusammenfassung die Ortszeile im Seitenkopf (die enthielt ohnehin nur Ort und
   „Ort ändern“); der Ort ist in der Zusammenfassung verlinkt, „Suche anpassen“ springt zum
   Formular. Das spart Höhe und vermeidet doppelte Informationen. Ab 960 px ist die
   Zusammenfassung ausgeblendet, weil das Formular sichtbar daneben steht.
6. **Wetterwünsche als Optionsfelder mit Grenzwerten.** Die Grenzen werden aus den Enums
   gelesen (`min/maxTemperatureCelsius`, `maxWindKmh`, `maxPrecipitationMm`), nicht im
   Template dupliziert. Label und Grenze stehen in einer Zeile und brechen bei Bedarf um;
   das hielt das Formular bei 1280 px von 1918 auf 1774 px Höhe.
7. **Semantische Farben nur als Text und feine Kontur.** Die Hinweis-Icons sind Kreise mit
   1-px-Kontur in der semantischen Farbe statt weißer Schrift auf satter Fläche. Damit bleibt
   „weiße Schrift nur auf dem Akzent“ eingehalten.
8. **Linienfarbe.** `#78869A` liegt zwischen eingelassener Fläche und Sekundärtext und erreicht
   gegen Weiß 3,70:1, gegen die eingelassene Fläche 3,22:1. Trennlinien sind bewusst sparsam;
   Struktur entsteht überwiegend über Abstand und Flächen.
9. **Icons und Favicon.** Marke und Favicon greifen das Ergebnisband als Motiv auf und nutzen
   nur Akzent, Zeitfensterfläche und eingelassene Fläche. Das Favicon ist eine lokale SVG-Datei,
   kein externer Dienst.
10. **Testanpassungen.** Im Browser-Szenario wurde `.profile-card` zu `.profile-row` (die
    Profilübersicht ist jetzt eine Liste) und „45 Minuten“ zu „45 Min.“ (kürzere Schreibweise
    in der Liste). Zusätzlich prüft das Szenario die 404-Seite bei 360 px sowie bei 1280 × 800,
    dass das erste Ergebnis bzw. der Leerzustand ohne Scrollen sichtbar ist und nichts
    horizontal scrollt. Die Java-Tests wurden nicht geändert.

## Prüfprotokoll der visuellen Prüfung

**Werkzeug.** Playwright-MCP (Chromium) gegen die laufende Anwendung auf Port 8093 (aus
`.port`). Pro Seite und Breite wurden DOM-Geometrie, Überläufe, sichtbare Textblöcke und
berechnete Stile erhoben und Screenshots (full page) erzeugt. Da im Agentenkontext kein
Bildbetrachter verfügbar ist, wurden die Screenshots zusätzlich pixelweise ausgewertet
(Flächenfarben, Textzeilen, Bandpositionen) und mit den Messwerten abgeglichen; die
Screenshots liegen außerhalb des Repositories unter `/Users/anton/r2b-*.png`.

Geprüfte Seiten (jeweils 360 × 800 und 1280 × 800):

| Seite | Zustand geprüft | Ergebnis |
| --- | --- | --- |
| Planung, mehrere Tage | 22.–28.11., Rom, 9 Fenster inkl. 62-h- und 70-h-Fenster über Mitternacht, mehrere Fenster an einem Tag | 360: kein Überlauf, Band 294 px, schmälstes Segment 12,25 px (1 h), erste Tageszeile 635–783. 1280: Spalten 320/764 px, gleiche Oberkante, erste Tageszeile 341–489. |
| Planung, keine Treffer | Berlin, Standardwünsche, 7 Tage | Ruhiger Leerzustand mit 3 Vorschlägen; 1280 vollständig sichtbar (305–609), 360 ab y = 575, kein Überlauf. |
| Planung, ungültiger Zeitraum | Enddatum vor Startdatum | Rote Fehlermeldung im Ergebnisbereich (y = 305/252), kein Leerzustand, keine Tageszeilen, kein API-Aufruf (durch Test abgesichert). |
| Ortssuche | `?query=Ber`, 5 Treffer | Ein Panel, Zeilen mit Name/Region/Land, Trefferanzahl, kein Überlauf; Suche + Button passen bei 360 px in eine Zeile. |
| Profilübersicht | 4 Profile, davon ein sehr langer Name | Liste mit Faktenzeile; 1280 Fakten in 5 Spalten (Zeilen 129–153 px), 360 Fakten zweispaltig, Aktionen umbrechen, nichts abgeschnitten. |
| Profil bearbeiten | Profil mit langem Namen, 11 Optionsfelder | Einspaltiges Formular, Optionsfelder 44–52 px hoch, vorausgewählte Werte korrekt, kein Überlauf. |
| 404 | unbekannte Adresse | Gestaltete Seite, HTTP 404, Statusmarke, zwei Wege zurück, zentriert. |
| Zusatz: Erfolgs-/Fehlermeldung | Profil gespeichert bzw. doppelter Name | Erfolgs- und Fehlermeldung über dem Grid, Auswahl und Profilname bleiben erhalten; Testprofil danach wieder gelöscht. |

**Gefundene Probleme und Korrekturen**

1. Mobile Zusammenfassung war 310 px hoch und zeigte den Ort doppelt (Seitenkopf + Karte).
   → Ortszeile im Seitenkopf unter 960 px ausgeblendet, Fakten bei 360 px zweispaltig:
   Zusammenfassung 222 px, erste Tageszeile von y = 851 auf 635 verschoben.
2. Profilzeilen auf dem Desktop waren durch zu schmale Fakten-Spalten 176–241 px hoch und
   brachen unschön um. → Fakten als eigene volle Zeile mit fünf Spalten à ~200 px; Zeilen
   jetzt 129–153 px.
3. Ein mehrtägiges Fenster wiederholte auf jeder Tageszeile das vollständige Label. →
   Startzeile vollständig, Folgetage „Fortsetzung · bis …“.
4. Hinweis-Icons nutzten weiße Schrift auf Erfolgs-/Fehlerfarbe. → Kontur-Icon in der
   semantischen Farbe, konform zu „weiße Schrift nur auf dem Akzent“.
5. Fehlerseite stand in der schmalen Seite links statt mittig. → zentriert.
6. Leerzustand war auf 360 px unnötig lang (572 px). → Text gestrafft (500 px).
7. „0 Zeitfenster“ stand zusätzlich über dem Leerzustand. → Zähler nur bei mindestens einem
   Fenster.
8. Aufklapp-Marke „Wetter im Fenster“ war nur 28 px hoch. → 36 px Zielhöhe.
9. Die „Jetzt“-Markierung nutzte die Akzentfarbe und konkurrierte mit den Fenstern. →
   Sekundärfarbe; Akzent bleibt für Aktion, Fokus, Auswahl und Fensterrahmen reserviert.
10. Im Ergebnis-Kopf klebte der Zähler für Screenreader am Punkt („·4 Zeitfenster“). →
    eigener Trenner mit `aria-hidden`, der Zähler bleibt als eigener Text lesbar.

**Gleichgewicht (Prüffrage aus `system.md`).** Im oberen und mittleren Bereich wirkt die
Seite als ruhiges Ganzes: zwei gleich helle Flächen, das Band als stärkstes, aber blasses
Element, Akzent nur an Hauptbutton, aktiver Navigation, gewählter Option und Fensterkontur.
Nichts springt grell heraus, das Ergebnis ist sofort zu finden. Schwachpunkt ist der untere
Seitenbereich: Das Formular ist mit 1774 px deutlich höher als die Ergebnisfläche
(1314 px bei sieben Tagen), sodass rechts neben dem unteren Formulardrittel Seitenhintergrund
steht. Das ist kein lautes Element, aber der einzige Punkt, an dem die Seite nicht ganz
ausbalanciert wirkt.

## Kontraste (rechnerisch geprüft)

| Vordergrund / Hintergrund | Verhältnis | Anforderung |
| --- | --- | --- |
| Text `#243247` / Weiß | 12,95:1 | ≥ 4,5 |
| Text `#243247` / Seitenhintergrund | 11,94:1 | ≥ 4,5 |
| Text `#243247` / eingelassene Fläche | 11,28:1 | ≥ 4,5 |
| Sekundärtext `#53657A` / Weiß | 5,98:1 | ≥ 4,5 |
| Sekundärtext / Seitenhintergrund | 5,52:1 | ≥ 4,5 |
| Sekundärtext / eingelassene Fläche | 5,21:1 | ≥ 4,5 |
| Akzent `#2F5E92` / Weiß | 6,69:1 | ≥ 4,5 (Text) / ≥ 3 (Fokus) |
| Akzent / Auswahlfläche `#E7EFF8` | 5,77:1 | ≥ 4,5 / ≥ 3 |
| Weiß / Akzent (Hauptbutton) | 6,69:1 | ≥ 4,5 |
| Erfolg `#2D6A4F` / 7-%-Tint | 5,80:1 | ≥ 4,5 |
| Fehler `#A33A3A` / 7-%-Tint | 5,86:1 | ≥ 4,5 |
| Warnung `#895B13` / Weiß | 5,88:1 | ≥ 4,5 |
| Feldrahmen `#78869A` / Weiß | 3,70:1 | ≥ 3 |
| Feldrahmen / eingelassene Fläche | 3,22:1 | ≥ 3 |
| Fensterkontur `#2F5E92` / Bandfläche | 5,83:1 | ≥ 3 |
| Fensterkontur / Fensterfüllung | 4,23:1 | ≥ 3 |
| Text `#243247` / Fensterfüllung `#B9D0EA` | 8,18:1 | ≥ 4,5 |

Die Fensterfüllung selbst liegt mit 1,38:1 nur knapp über der Bandfläche; die Abgrenzung
erfolgt deshalb über die 1-px-Kontur (5,83:1 gegen das Band) und zusätzlich über den Text
(Beginn, Ende, Dauer), nie allein über Farbe.

## Ausgeführte Prüfungen

- `./mvnw test`: **89 Tests, 0 Fehlschläge, 0 Fehler, 3 übersprungen** (je 2 in
  `ProfilePerformanceTests`, 1 in `ProfileBrowserTests` – beide Demo-Klassen sind wie
  dokumentiert standardmäßig deaktiviert). Davon 9 neue Tests in `PlanningTimelineTests`.
- `ProfileBrowserTests` mit `-Ddemo.browser=true` und gesetztem `PLAYWRIGHT_MODULE`:
  **1 Test bestanden** (Speichern, Bearbeiten, Verwenden, Abbrechen/Löschen, Neuladen,
  Ablauf ohne JavaScript, kein horizontales Scrollen bei 360 px, erstes Ergebnis bei
  1280 × 800 sichtbar).
- Manuelle HTTP-Prüfungen der laufenden Anwendung: Planung mit Ergebnissen (200),
  Leerzustand (200), ungültiger Zeitraum (200 mit Fehlermeldung), unbekannte Adresse (404),
  Profil speichern/duplizieren/löschen (302 mit Rückmeldung).
- Browserprüfung mit Playwright-MCP: siehe Prüfprotokoll; Messwerte für 960, 1280 und
  360 px ohne horizontalen Überlauf.

## Verbleibende Schwächen und nächste Schritte

1. **Formularhöhe.** Die Suchspalte ist mit 1774 px (1280) deutlich länger als die
   Ergebnisfläche; der Hauptbutton liegt unterhalb des ersten Bildschirms. Enter in einem
   Feld submittet zwar, aber eine sichtbare Aktion fehlt oben. Nächster Schritt: klebende
   Aktionsleiste am unteren Rand der Suchspalte oder ein zweistufiges Formular
   (Zeitraum/Aktivität zuerst, Wetterwünsche aufklappbar).
2. **Kurze Fenster im Band.** Ein 1-Stunden-Fenster ist bei 360 px nur 12,25 px breit. Die
   Beschriftung trägt die Information vollständig; eine Mindestbreite würde die Zeitachse
   verfälschen. Denkbar wäre eine Tick-plus-Label-Darstellung für Fenster unter zwei Stunden.
3. **Mehrtägige Fenster.** Bei extremen Einstellungen (alles „Egal“, 1 Minute) entsteht ein
   Fenster über alle sieben Tage; die Folgetage zeigen dann jeweils „Fortsetzung · bis …“.
   Korrekt und konsistent, aber textlastig. Eine Zusammenfassung am Bandanfang wäre eine
   Alternative.
4. **`color-mix()`** setzt einen modernen Browser voraus (Chromium 111+, Safari 16.2+). In
   älteren Browsern verlieren Hinweise ihre Tints; die Textfarben und Konturen bleiben
   lesbar. Bei Bedarf statische Tint-Variablen als Fallback ergänzen.
5. **Native Datumsfelder formatieren nach Browser-Sprache.** In einem englischsprachigen
   Browser zeigen `<input type="date">`-Felder `MM/DD/YYYY`, während Zusammenfassung und
   Ergebnisse `TT.MM.JJJJ` nutzen. Das ist eine Eigenschaft nativer Datumsfelder (bewusst
   kein Eigenbau); `min`/`max` und die Verarbeitung bleiben korrekt. Ein kurzer Hinweis am
   Feld könnte das Format erklären.
6. **Kein Dark Mode, keine Druckansicht.** Beides war nicht gefordert; die Tokens wären
   dafür vorbereitet.
7. **Lange Profilnamen** in Textfeldern scrollen nativ im Feld (kein Layout-Überlauf). Das
   ist erwartetes Browserverhalten, könnte aber durch eine Textarea oder eine Vorschau
   entschärft werden.

## Commits

1. `a3f9c3d` – Place suitable windows on a shared daily timeline
2. `a83ee1d` – Restyle every page after the Himmel & Schiefer design system
3. `ed5e1ff` – Keep the result header clear for screen readers
4. Dieser Bericht (eigener Commit)

Nichts wurde gepusht.
