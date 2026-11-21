# DESIGN.md – Gestaltungskonzept „Wann passt es?“ (Runde 2)

## Leitidee

Wer auf das richtige Wetter wartet, sieht vor sich eine lange, dunkle Wand aus Stunden, in der hier und da ein Fenster aufgeht. Genau so zeigt die Seite die Antwort: Der Suchzeitraum ist ein dunkles Band, die passenden Zeitfenster sind darin warm leuchtende Öffnungen wie Licht, das durch ein Fenster fällt. Alles andere ist ruhig, weiß und typografisch gegliedert; das Formular ist ein Werkzeug am Rand, die Antwort steht zuerst.

## Farbpalette

Sechs Grundfarben, alle als CSS-Variablen in `static/css/app.css`. Abstufungen sind hellere oder dunklere Töne derselben Grundfarbe.

| Rolle | Variable | Hex | Verwendung |
| --- | --- | --- | --- |
| Papier (Grundfarbe 1) | `--paper` | `#ffffff` | Seitenhintergrund (reines Weiß) |
| Tinte (Grundfarbe 2) | `--ink` | `#1c1f26` | Text, Fensterband-Fläche |
| Tinte, gedämpft | `--ink-muted` | `#5a5f6b` | Nebentexte, Hinweise |
| Tinte, Rahmen | `--ink-border` | `#7d828c` | Rahmen von Eingabefeldern und Auswahlelementen |
| Tinte, Linie | `--ink-rule` | `#d8dade` | Trennlinien (nicht interaktiv) |
| Tinte, Band vergangen | `--ink-past` | `#50555f` | bereits vergangene Stunden im Band |
| Tinte, Band Raster | `--ink-grid` | `#8b909a` | Tagesgrenzen im Band |
| **Akzent (Grundfarbe 3)** | `--accent` | `#b54708` | Buttons, Links, Fokus, Hervorhebungen |
| Akzent, kräftig | `--accent-strong` | `#8a3405` | Hover/aktive Zustände |
| Akzent, Licht | `--accent-light` | `#f39a4b` | passende Fenster im dunklen Band |
| Akzent, Hauch | `--accent-tint` | `#fdf0e6` | Hover-Fläche von Listeneinträgen |
| Erfolg (Grundfarbe 4) | `--success` | `#1d7a3e` | Erfolgsmeldungen (Tönung `#e9f5ed`) |
| Warnung (Grundfarbe 5) | `--warning` | `#8a5a00` | Hinweise (Tönung `#fdf5dc`) |
| Fehler (Grundfarbe 6) | `--error` | `#b3261e` | Fehlermeldungen, Löschen (Tönung `#fcebea`) |

**Akzentfarbe alt und neu**

| | Hex | Farbton (HSL) |
| --- | --- | --- |
| bisher (Runde 1, `--color-primary`) | `#1a56a8` | 214,6° (Blau) |
| neu (`--accent`) | `#b54708` | 21,8° (Abendrot/Ziegelorange) |
| Farbtonabstand | | 167,2° (Kreisabstand; Anforderung ≥ 40°) |

Der neue Akzent liegt außerhalb des verbotenen Bereichs 90°–200°. Grün wird nur semantisch für Erfolgsmeldungen verwendet. Die passenden Fenster selbst sind bewusst nicht grün, sondern in der Akzentfarbe: Sie sind die Hervorhebung der Seite („hier scheint die Sonne durch“).

## Schrift

| Rolle | Familie | Einsatz |
| --- | --- | --- |
| Überschriften | `"Iowan Old Style", "Palatino Linotype", Palatino, Charter, "Bitstream Charter", Georgia, serif` | `h1`–`h3`, Markenname |
| Text und Bedienung | `system-ui, -apple-system, "Segoe UI", Roboto, sans-serif` | Fließtext, Formulare, Buttons, Uhrzeiten |
| Zahlen | `font-variant-numeric: tabular-nums` | Uhrzeiten, Daten, Dauern, Band-Beschriftung |

Größen-Skala (fünf Stufen, als Variablen):

| Variable | Wert | Einsatz |
| --- | --- | --- |
| `--text-s` | 0,8125rem (13 px) | Hinweise, Band-Beschriftung, Fußzeile |
| `--text-m` | 1rem (16 px) | Fließtext, Formulare, Buttons |
| `--text-l` | 1,25rem (20 px) | Uhrzeiten der Fenster, `h3`, Sidebar-Überschrift |
| `--text-xl` | 1,625rem (26 px) | `h1` mobil, `h2` |
| `--text-2xl` | 2,25rem (36 px) | `h1` ab 40rem Breite |

## Layout

Gemeinsamer Rahmen: weiße Kopfzeile mit Markenname in Serifenschrift und Navigation, darunter eine feine Linie; Fußzeile mit Quellenangaben hinter einer Linie. Keine Karten mit Schatten. Gliederung über Überschriften, Abstände und feine Trennlinien.

**Planung, 360 px:** Ergebnisse stehen vor dem Formular. Oben die Überschrift (Aktivität), Ort und Zeitraum, darunter eine kurze Zusammenfassung, das Fensterband und die Liste der Fenster. Ein Link „Suche anpassen“ springt zum Formular, das darunter folgt.

```
+--------------------------------+
| Wann passt es?                 |
| Planung  Ort suchen  Profile   |
|--------------------------------|
| Zeitfenster für                |
| Spazieren gehen                |   <- h1 Serif
| Berlin, am 22.11.2026          |
| Suche anpassen                 |
|                                |
| Passende Fenster 3  Längstes 4 |
| ██████▒▒▒▒▒▒▒▓▓▓▓▓▒▒▒▒▓▓▓▒▒▒▒▒ |   <- Fensterband (dunkel,
| 0     6     12     18     24   |      Fenster leuchten)
|                                |
| Samstag, 22. November          |   <- h3 Serif
| ▮ 10:00 – 14:00 Uhr    4 Std.  |
| ------------------------------ |
| ▮ 18:00 – 21:00 Uhr    3 Std.  |
|================================|
| Suche anpassen                 |   <- Formular (Werkzeug)
| Ort: Berlin  [Potsdam ][Suchen]|
| Von [    ]  Bis [    ]         |
| Aktivität [Spazieren gehen   ] |
| Mindestdauer [60]              |
| Temperatur ( ) Kühl (•) Mild … |
| [ Zeitfenster suchen         ] |
| Als Suchprofil speichern …     |
|--------------------------------|
| Quellen: Open-Meteo, GeoNames  |
+--------------------------------+
```

**Planung, 1280 px:** Ergebnisse links in der breiten Spalte, das Formular rechts als schmale Werkzeugspalte (22rem) hinter einer senkrechten Linie. Die DOM-Reihenfolge (Ergebnisse, dann Formular) entspricht damit auch auf dem Desktop der Lesereihenfolge.

```
+---------------------------------------------------------------------------+
| Wann passt es?                                Planung  Ort suchen  Profile |
|---------------------------------------------------------------------------|
| Zeitfenster für Spazieren gehen              | Suche anpassen             |
| Berlin, Land Berlin, am 22.11.2026           | Ort  Berlin                |
|                                              | [Ort eingeben   ][Suchen]  |
| Passende Zeitfenster 3  Längstes 4 Std.      |                            |
| ████████▒▒▒▒▒▒▒▒▒▓▓▓▓▓▓▓▒▒▒▒▒▒▓▓▓▓▒▒▒▒▒▒▒▒▒ | Von [     ]  Bis [     ]   |
| 0 Uhr     6        12        18        24    | Aktivität [            ]   |
|                                              | Mindestdauer [   ]         |
| Samstag, 22. November                        | Temperatur (•) Mild …      |
| ▮ 10:00 – 14:00 Uhr                 4 Std.   | Wind …   Niederschlag …    |
| ---------------------------------------------| [ Zeitfenster suchen ]     |
| ▮ 18:00 – 21:00 Uhr                 3 Std.   | Als Suchprofil speichern   |
|---------------------------------------------------------------------------|
| Wetterdaten: Open-Meteo …                                                 |
+---------------------------------------------------------------------------+
```

**Ortssuche, Profil bearbeiten, Fehlerseiten:** eine schmale Spalte (max. 46rem), links ausgerichtet. **Suchprofile:** Liste ohne Karten; jedes Profil ist ein Abschnitt mit Name (Serif), Ort, Einstellungen als ausgerichtetes Begriffsraster (2 Spalten mobil, 3 Spalten breit) und Aktionen, getrennt durch Linien.

## Das eine prägende Element: das Fensterband

Ein durchgehendes, dunkles Band (Tinte `#1c1f26`) über den ganzen Suchzeitraum. Passende Zeitfenster sind darin als hell leuchtende Blöcke (`#f39a4b`) eingesetzt, bereits vergangene Stunden sind grau abgesetzt, Tagesgrenzen fein markiert. Bei einem einzelnen Tag beschriften Stundenmarken (0, 6, 12, 18, 24 Uhr) das Band, bei mehreren Tagen Wochentag und Datum.

Begründung: Die Frage lautet „wann und wie lange?“. Eine Zeitachse beantwortet beides räumlich – Lage = wann, Breite = wie lange – und zeigt auch, wie viel vom Zeitraum *nicht* passt. Das dunkle Band mit Lichtöffnungen greift wörtlich das „Zeitfenster“ auf. Ein Fenster über Mitternacht bleibt im durchgehenden Band ein einziger zusammenhängender Block. Das Band ist für Screenreader ausgeblendet; die Liste darunter trägt dieselbe Information als Text.

Alles andere bleibt ruhig: weißer Grund, eine Akzentfarbe, keine Schatten, keine Kacheln.

## Selbstprüfung gegen den generischen Look

Erster Entwurf, ehrlich betrachtet – was würde ich genauso für jede andere App vorschlagen?

| Generischer Plan | Warum generisch | Änderung |
| --- | --- | --- |
| Formular links, Ergebnisse rechts, beide als weiße Karten mit Schatten | Standard-Dashboard aus jeder Vorlage | Ergebnisse links/zuerst, Formular als schmale Werkzeugspalte ohne Karte, getrennt durch eine Linie |
| Drei Kennzahl-Kacheln („3 Fenster / 4 Std. / 7 Std.“) | KPI-Kachel-Reihe ist das häufigste KI-Muster | Eine einzige typografische Zeile ohne Kästen |
| Grüne Balken für passende Fenster auf hellgrauer Spur | Fortschrittsbalken-Optik, austauschbar | Dunkles Band mit leuchtenden Öffnungen im Akzentton – das Fenster-Motiv |
| Einleitungsblock mit „Finde Zeitfenster, in denen …“ über allem | Hero-Text, der nur Platz kostet | Gestrichen; die Seite beginnt direkt mit der Antwort |
| Pillen-Badges für Dauer und „über Mitternacht“ | Badge-Kit aus jeder UI-Bibliothek | Dauer als rechtsbündige Zahl, Mitternacht als Klartext mit Folgetag |
| Auswahllisten für die Wetterwünsche | Lange Optionstexte werden abgeschnitten, Wahl unsichtbar | Sichtbare Optionsgruppen (Radio-Buttons) mit Grenzwert in zweiter Zeile |
| Lade-Spinner (runder Kreis) | Generisch, bräuchte einen dritten Eckenradius | Buttontext „Suche läuft …“ und abgedunkelte Ergebnisse |
| Profile als Karten-Raster | Einheitliches Karten-Kit | Linierte Liste, Einstellungen als ausgerichtetes Begriffsraster ohne Kästen |
| „Fehler 404“ als Versal-Label über der Überschrift | Eyebrow-Muster | Normale Schreibweise, kleine Zeile unter der Überschrift |
| Pfeile „←“ vor Zurück-Links | Pfeil-Deko | Gestrichen, der Linktext sagt, wohin es geht |

Entfernt vor Abschluss (nach der ersten Sichtprüfung): der Wert „Zusammen“ (Gesamtdauer aller Fenster) in der Zusammenfassung. Die Gesamtmenge zeigt das Band auf einen Blick, und „Längstes“ beantwortet „wie lange?“ direkter.

## Formen

- Zwei Eckenradien: `--radius-s: 3px` (Eingabefelder, Buttons, Meldungen) und `--radius-band: 6px` (Fensterband und seine Fenster). Sonst rechte Winkel.
- Keine Schatten: nichts liegt über anderem.
- Keine Farbverläufe; Rastermarken im Band sind eigene Elemente.
- Symbole nur als Inline-SVG (Logo, Meldungs-Icons).
