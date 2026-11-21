# Gestaltung – „Wann passt es?“

Konzept für die Neugestaltung des Frontends (Runde 2). Grundlage ist
`.agents/skills/wpe-design/SKILL.md`; die Umsetzung liegt in
`src/main/resources/static/css/app.css` und den Thymeleaf-Templates.

## Leitidee

Die App beantwortet eine einzige Frage: *wann* ist draußen *wie lange* etwas
möglich. Deshalb steht die Antwort nicht als Liste unter dem Formular, sondern
als dunkles „Nachtfenster“ oben rechts auf der Seite: ein Tagesband, auf dem
jedes passende Zeitfenster als heller Balken an seiner Stelle im 24-Stunden-Tag
sitzt. Das Formular ist ruhiges, weißes Werkzeug daneben; die Seite wirkt wie
ein Wetteraushang am Schwarzen Brett, nicht wie ein Formular mit Ergebnisseite.

## Farbpalette

Eine Grundfläche, eine Akzentfarbe, zwei semantische Farben. Alles als
CSS-Variablen in `:root`.

| Rolle | Name | Hex | Verwendung |
| --- | --- | --- | --- |
| Grundfläche | Papier | `#FFFFFF` | Seitenhintergrund (reines Weiß) |
| Text | Tinte | `#1A1512` | Fließtext, Überschriften |
| Text, gedämpft | Tinte blass | `#5B5249` | Hinweise, Sekundärangaben |
| Linie | Faden | `#D5CCC1` | Trennlinien, Raster |
| Feldrahmen | Rahmen | `#8A7F73` | Rahmen von Eingaben und Auswahllisten |
| Akzent | Glut | `#C2410C` | Aktionen, Fokus, aktive Navigation |
| Akzent, dunkel | Glut tief | `#8F2D08` | Links, Text auf hellem Grund |
| Akzent, hell | Glut hell | `#F0864E` | Balken im dunklen Ergebnisband |
| Ergebnisfläche | Nacht | `#17120F` | Ergebnispanel, Fehlerseite |
| Erfolg | Moos | `#2E6B3D` | Erfolgsmeldung |
| Fehler | Ziegel | `#A3201A` | Fehlermeldung |

Akzentfarben:

- bisher (vor der Überarbeitung, aus dem alten `app.css`): `#0F5F57`, Farbton **174°** (Petrol)
- neu: `#C2410C`, Farbton **17°** (gebranntes Orange)
- Farbtonabstand: **157°** (Anforderung: mindestens 40°)
- Der neue Akzent liegt außerhalb des verbotenen Bereichs 90°–200°.

## Schrift

| Rolle | Stack |
| --- | --- |
| Überschriften, Zahlen im Fehlerbild | `"Iowan Old Style", "Palatino Linotype", Palatino, Charter, "Bitstream Charter", Georgia, serif` |
| Fließtext, Formulare, Zeiten, Daten, Dauern | `system-ui, -apple-system, "Segoe UI", Roboto, sans-serif` |

Keine weiteren Familien, keine Webfonts, keine Monospace-Schrift. Uhrzeiten,
Datumsangaben und Dauern erhalten durchgehend `font-variant-numeric: tabular-nums`.

Größen-Skala (sechs Stufen, CSS-Variablen):

| Variable | Wert | Verwendung |
| --- | --- | --- |
| `--size-1` | 0.8125rem (13 px) | Hinweise, Feldbeschriftungen, Fußzeile |
| `--size-2` | 0.9375rem (15 px) | kleine Texte, Navigation, Metadaten |
| `--size-3` | 1.0625rem (17 px) | Fließtext, Eingaben |
| `--size-4` | 1.375rem (22 px) | Abschnittsüberschriften (Serife) |
| `--size-5` | 1.875rem (30 px) | Uhrzeiten im Ergebnisband, Profilnamen (Serife) |
| `--size-6` | `clamp(2rem, 4.5vw, 2.75rem)` | Seitenüberschrift, Statuszahl der Fehlerseite |

## Form und Struktur

- Zwei Eckenradien im gesamten Projekt: `--r-1: 3px` (Bedienelemente),
  `--r-2: 12px` (das dunkle Ergebnisband). Balken und Raster sind scharfkantig.
- Schatten nur am Ergebnisband, weil es über der Seite liegt; sonst keine.
- Gliederung durch Typografie, Abstände und Haarlinien, nicht durch Karten.
- Keine Versal-Labels, keine Mittelpunkt-Reihungen, keine Pfeile an Buttons,
  keine Emojis; die wenigen Symbole (Standortnadel, Sonnenmarke) sind Inline-SVG.
- Das Ergebnisband ist das eine mutige Element; alles andere bleibt weiß, ruhig
  und linksbündig.

## Layout

Mobil zuerst: eine Spalte, das Ergebnisband steht **über** dem Formular. Ab
900 px zweispaltig: links das Formular (360 px), rechts das Ergebnisband, das
den Rest der Breite einnimmt und zuerst ins Auge fällt.

360 px:

```
+--------------------------------+
| Wann passt es?      Planung /  |
|              Ort / Suchprofile |   Kopfzeile, umbrechend
+--------------------------------+
| Wann passt es für              |
| Spazieren gehen?               |
| (Nadel) Berlin   Ort ändern    |
| +----------------------------+ |
| | Passende Zeitfenster  3    | |   dunkles Band
| | Sa, 22.11.  10:00-12:00    | |
| | 2 Stunden                  | |
| | ####                       | |   Balken im Tagesband
| | Sa, 22.11.  17:00-19:00    | |
| +----------------------------+ |
| Zeitraum                       |
| Von [        ] Bis [        ]  |
| Aktivität                      |
| [Spazieren gehen v] [60     ]  |
| Wetterwünsche                  |
| [Mild v] [Wind v] [Regen v]    |
| [Zeitfenster finden]           |
| Profil speichern ...           |
+--------------------------------+
```

1280 px:

```
+----------------------------------------------------------------------+
| Wann passt es?                          Planung  Ort suchen  Profile |
+----------------------------------------------------------------------+
| Wann passt es für Spazieren gehen?     |  Passende Zeitfenster  3   |
| (Nadel) Berlin   Ort ändern            |  Sa, 22.11. 10:00-12:00    |
| +--------------------+                 |  2 Stunden                 |
| | Zeitraum           |                 |  ########                  |
| | Von / Bis          |                 |  Sa, 22.11. 17:00-19:00    |
| | Aktivität          |                 |  2 Stunden                 |
| | [Spazieren gehen v]|                 |  ########                  |
| | [60              ] |                 |                            |
| | Wetterwünsche      |                 |                            |
| | [Mild][Wind][Regen]|                 |                            |
| | [Zeitfenster finden]                 |                            |
| +--------------------+                 |                            |
| | Profil speichern ...                 |                            |
+----------------------------------------------------------------------+
| Wetterdaten von Open-Meteo, Ortsdaten von GeoNames ...               |
+----------------------------------------------------------------------+
```

Das erste Zeitfenster liegt bei 1280 × 800 px oberhalb der Falz
(Kopfzeile rund 64 px, Seitenkopf rund 130 px, Bandkopf rund 80 px).

## Das prägende Element

Das **Tagesband**: eine dunkle Fläche mit je Zeitfenster einem hellen Balken,
dessen linke Kante dem Beginn und dessen Breite der Dauer im 24-Stunden-Tag
entspricht. Damit beantwortet ein Blick „wann“ und „wie lange“, nicht erst das
Lesen der Uhrzeiten. Die Balken sind bewusst scharfkantig und stehen auf einer
Haarlinie, die den Tag von 0 bis 24 Uhr aufspannt; ein Fenster über Mitternacht
wird zusätzlich als Text „bis Sonntag, 23.11., 02:00“ ausgewiesen, damit die
Aussage auch dann eindeutig bleibt, wenn der Balken am Tagesende endet.

## Selbstprüfung gegen den generischen Look

Welche Teile des Plans würde ich auch für jede andere App vorschlagen?

1. **Weiße Karten mit weichem Schatten auf hellem Grund, alles zentriert.**
   Gestrichen. Die Seite ist reines Weiß mit Haarlinien; nur das Ergebnisband
   hat Fläche und Schatten, weil es inhaltlich die Antwort ist.
2. **Abgerundete Pill-Buttons und Chips für Ort und Status.**
   Gestrichen. Buttons sind Rechtecke mit 3 px Radius; der Ort steht als
   Textzeile mit Nadel, nicht als Plakette.
3. **Petrol/Grün als „Wetter“-Farbe, wie im alten Entwurf.**
   Ersetzt durch gebranntes Orange mit 157° Abstand; Grün bleibt ausschließlich
   der Erfolgsmeldung vorbehalten.
4. **Formular oben, Ergebnisliste unten, beides gleich wichtig.**
   Umgedreht: das Ergebnis steht mobil oben und auf dem Desktop als dunkle
   Fläche rechts; das Formular ist erkennbar Werkzeug.
5. **Versal-Kleinlabel („ZEITRAUM“, „ERGEBNISSE“) als Gliederung.**
   Ersetzt durch Serifenzwischenüberschriften und Abstand.
6. **Fortschrittsbalken/Ladeanimation als Schmuck.**
   Gestrichen; nur der Submit-Button zeigt bei Bedarf einen dezenten Ladezustand.

Bewusst behalten: die vertraute einspaltige Feldanordnung auf Mobilgeräten und
die deutsche, knappe Sprache – beides dient der Aufgabe, nicht dem Muster.
