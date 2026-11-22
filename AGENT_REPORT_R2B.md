# Abschlussbericht Runde 2b

„Himmel & Schiefer“ verbindet Planung, Ortssuche, Suchprofile und Fehlerseiten über weiße Hauptflächen auf einem hellen, blaugrauen Seitenhintergrund. Systemschrift, klare Überschriften und feine Linien gliedern die Ansichten. Die Tagesbänder machen Lage und Länge der Fenster vergleichbar. Die Zeitangaben bleiben auch beim Lesen ohne Grafik vollständig verständlich.

## Abnahme

| ID | Status | Umsetzung und Nachweis |
| --- | --- | --- |
| Z1 | Erfüllt | Die Farbrollen aus dem Designsystem sind als CSS-Variablen hinterlegt und im Einsatz. Trennlinien und Vergangenheitsschattierung sind aus dem blaugrauen Feldrahmen abgeleitet. |
| Z2 | Erfüllt | Systemschrift, Textgrößen 16/24 und 14/20 px, Titelgrößen 32/40 und 22/28 px, Gewichte 400–700, Tabellenziffern, vorgegebene Abstandsskala sowie Radien von 8 und 12 px. |
| Z3 | Erfüllt | Ab 960 px stehen Suchkriterien links auf 320 px und Ergebnisse rechts. Bei 1280 px begannen beide weißen Flächen bei y = 157 CSS-Pixeln und beide Überschriften bei y = 182. Auf schmalen Ansichten folgen auf die Zusammenfassung zuerst die Ergebnisse und dann das Formular. |
| Z4 | Erfüllt | Eine Zeile je Tag im gültigen Suchzeitraum bei Treffern, mit gemeinsamer Ortszeitskala von 0 bis 24 Uhr. Jedes Fenster zeigt Beginn, Ende und Dauer. Im Prüffall Montag 22 Uhr bis Dienstag 2 Uhr waren zwei Bänder und eine Beschriftung mit 4 Std. sichtbar. Ein Fenster, das genau um Mitternacht endet, steht im vorherigen Tag bis 24:00. |
| Z5 | Erfüllt | Temperatur, Wind und Niederschlag stehen in aufklappbaren Wetterdetails. Der heutige Streifen markiert die vergangene Zeit bis zur aktuellen Ortszeit und enthält eine Legende. |
| Z6 | Erfüllt | Der neutrale Leerzustand schlägt eine kürzere Mindestdauer, einen längeren Zeitraum und weniger strenge Wetterwünsche vor. Rot zeichnet Fehler und die zugehörigen Felder aus. |
| Z7 | Erfüllt | Bei 1280 × 800 endete die erste Ergebniszeile bei y = 398 und der Leerzustand bei y = 316. Beide waren beim Laden vollständig sichtbar. |
| Z8 | Erfüllt | Alle 14 Dokumentbreiten entsprachen dem Viewport. Sichtprüfung und Messung sichtbarer Textgrenzen ergaben keine abgeschnittenen Texte. Der 84 Zeichen lange Profilname passte auch im mobilen Eingabefeld vollständig. |
| Z9 | Erfüllt | Die niedrigsten gemessenen beziehungsweise berechneten Kontraste der verwendeten Kombinationen sind unten belegt. |
| Z10 | Erfüllt | Alle sieben Ansichten per Playwright-MCP bei 360 × 800 und 1280 × 800 geprüft, korrigierte Ansichten erneut geöffnet und betrachtet. Die Beurteilung des Gleichgewichts steht im Prüfprotokoll. |
| Z11 | Erfüllt | Die ursprünglichen Routen, Request-Parameter, Profil- und Wetterfunktionen arbeiten weiter. Die Tests aus Runde 1 sind grün. HTML-Assertions und Browserablauf wurden an die geänderte Darstellung angepasst. |
| Z12 | Erfüllt | Dieser Bericht und der separat nachvollziehbare Umsetzungscommit liegen auf `agent/codex-r2b`. Es wurde nichts gepusht. |

## Entscheidungen zur Darstellung

- Ein View-Model bildet die vorhandenen Zeitfenster auf Uhrzeitkoordinaten ab und sammelt die Werte für die Wetterdetails. Der Planungscontroller übergibt dafür die bereits berechneten Fenster, Ortszeit und gefilterten Stunden. Bei Niederschlag zählt jeder Stundenendwert zum vorausgehenden Intervall. Die Berechnung geeigneter Fenster, Wetterregeln und Datenbankstruktur blieben unverändert.
- Die Skala zeigt die Ortsuhr auch bei einer Zeitumstellung. Im Frühjahr bleibt die übersprungene Stunde frei. Im Herbst teilen sich wiederholte Stunden dieselbe Koordinate. Die Dauer bleibt die tatsächlich verstrichene Zeit. Bei mehrdeutigen Endpunkten steht der UTC-Offset in der Beschriftung.
- Die Gruppen für Temperatur, Wind und Niederschlag verwenden native Optionsfelder mit sichtbaren Grenzwerten aus den bestehenden Wetterregeln. Profilnamen verwenden ein mitwachsendes, umbrechendes Textfeld, damit lange Namen in voller Länge lesbar bleiben. Bei fehlender Unterstützung für automatisches Mitwachsen bleibt das Feld manuell vergrößerbar.
- Der Feldrahmen `#748497` erreicht auf Weiß wie auf den eingelassenen Flächen wenigstens 3:1. Die schwächeren Linien gliedern die Oberfläche. Ocker markiert die Warnung bei der Eingabe, Rot die serverseitig gemeldeten Feld- und Anwendungsfehler, Grün Erfolgsmeldungen.

## Prüfprotokoll

- `./mvnw test` mit Java 21.0.13. 66 Tests insgesamt, 63 bestanden, 0 fehlgeschlagen, 0 Fehler, 3 standardmäßig übersprungen. Der MVC-Test prüft jetzt Beschriftung und Bandteile am Tageswechsel sowie Temperatur-, Wind- und Niederschlagswerte. Zwei zusätzliche Tests prüfen die Darstellung bei Sommer- und Winterzeit. Die HTTP-Tests prüfen den Erhalt des Profilnamens im neuen Textfeld.
- `./mvnw -Ddemo.browser=true -Dtest=ProfileBrowserTests test` mit lokalem Chromium. 1 bestanden, 0 fehlgeschlagen, 0 übersprungen. Mit JavaScript bei 1280 px und ohne JavaScript bei 360 px geprüft. Der Ablauf umfasst Ortssuche mit Erhalt der Einstellungen, Aufklappen der Wetterdetails, Speichern, Bearbeiten, Verwenden und Löschen eines Profils sowie die 404-Seite.
- Playwright-MCP mit Headless Chromium 157, Port 8091 aus `.port`, simulierte Orts- und Wetterdaten und H2 im Speicher. Bei beiden Größen angesehen wurden Planung mit vier Tagen und einem Fenster über Mitternacht, Planung ohne Treffer, ungültiger Zeitraum (HTTP 400), Ortssuche mit Treffern, Profilübersicht, Profilbearbeitung und 404. Auch Fokus beim Aufklappen mit Enter, mobile Sprungnavigation, Dokumentbreiten und die Größe des befüllten Namensfeldes geprüft.
- Bei der Sichtprüfung belegte „bis“ mobil eine eigene Zeile. Der Zeitraum steht nun in einer Zeile. Das Wort „Niederschlag“ brach in den geöffneten Details ungünstig um. Auf schmalen Bildschirmen stehen die Wetterwerte jetzt in einzelnen Zeilen, die Beschriftung passt vollständig. Die Überschriften der Desktop-Spalten erhielten einen gemeinsamen Innenabstand. Diese Änderungen wurden bei beiden Sollgrößen erneut geprüft.
- **Gleichgewicht.** Die beiden weißen Flächen beginnen auf gleicher Höhe. Beim Zusammenkneifen der Augen bleibt die Folge der hellblauen Zeitbänder gut erkennbar. Die Suchkriterien treten durch gleichmäßige Feldrahmen zurück. Beim leeren Ergebnis bleibt die Meldung schnell auffindbar, und mobil führt die Abfolge vom Suchkontext direkt zur Ergebnisfläche.
- `node --check src/test/browser/profile-flow.mjs` und `git diff --check` ohne Befund.

## Kontrast

Verwendet wurde das WCAG-Verhältnis `(Lhell + 0,05) / (Ldunkel + 0,05)` mit relativer Leuchtdichte aus linearem sRGB. Die Werte sind auf zwei Dezimalstellen gerundet.

| Verwendung | Kombination | Verhältnis |
| --- | --- | ---: |
| Haupttext auf den verwendeten Flächen | `#243247` auf `#E7EFF8` als schwächste Kombination | 11,16:1 |
| Metatext und Hilfetext | `#53657A` auf `#E7EFF8` als schwächste Kombination | 5,16:1 |
| Hauptaktion | `#FFFFFF` auf `#2F5E92` | 6,69:1 |
| Aktive Navigation | `#2F5E92` auf `#E7EFF8` | 5,77:1 |
| Erfolg und Fehler auf Hinweisfläche | `#2D6A4F` beziehungsweise `#A33A3A` auf `#F3F6FA` | 5,90:1 / 6,01:1 |
| Feldrahmen | `#748497` gegen Weiß, eingelassene Fläche und Auswahlfläche | 3,83:1 / 3,33:1 / 3,30:1 |
| Warn- und Fehlerrand im Feld | `#895B13` beziehungsweise `#A33A3A` gegen `#EAF0F6` | 5,12:1 / 5,67:1 |
| Fokus | `#2F5E92` gegen `#E7EFF8` als schwächste angrenzende Fläche | 5,77:1 |
| Kontur der Zeitbänder | `#2F5E92` gegen `#B9D0EA` und `#EAF0F6` | 4,23:1 / 5,83:1 |

## Verbleibende Grenzen

Die Sichtprüfung deckt Chromium ab. Native Datumsfelder richten sich bei der Eingabe nach der Browser- und Systemeinstellung, die Zusammenfassung verwendet ein deutsches Datumsformat. Weitere Browser und Screenreader wurden in dieser Runde nicht separat geprüft. Die Wetter- und Ortssuche verwendeten in allen Prüfungen simulierte Antworten.
