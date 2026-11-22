# Designsystem „Wann passt es?“ – Richtung „Himmel & Schiefer“

Diese Datei hält die gestalterischen Entscheidungen fest. Sie sind getroffen und werden angewendet, nicht neu verhandelt.

## Richtung und Gefühl

Ein heller, ruhiger Wetterplaner, der Orientierung gibt. Man soll auf einen Blick sehen, an welchen Tagen zusammenhängende Zeitfenster die eigenen Wetterwünsche erfüllen. Die Oberfläche wirkt klar und freundlich wie ein heller Himmel: nichts schreit, alles hat seinen Platz. Formular, Suchprofile und Ergebnisse gehören sichtbar zusammen und bilden ein ausgewogenes Ganzes. Kein Bereich dominiert durch eine dunkle oder vollflächig gesättigte Fläche.

## Farben

Als CSS-Variablen definieren und ausschließlich darüber verwenden.

| Rolle | Wert |
| --- | --- |
| Seitenhintergrund | `#F3F6FA` |
| Hauptflächen (Formular, Ergebnisse, Listen) | `#FFFFFF` |
| Eingelassene Flächen (Eingabefelder, Tageszeilen-Hintergrund) | `#EAF0F6` |
| Haupttext | `#243247` |
| Sekundärtext | `#53657A` |
| Akzent und Fokus | `#2F5E92` |
| Dezente Auswahl- und Zeitfensterfläche | `#E7EFF8` |
| Zeitfenster im Band | `#B9D0EA` mit 1 px Kontur in Akzentfarbe |
| Erfolg / Kriterien erfüllt | `#2D6A4F` |
| Warnung | `#895B13` |
| Fehler | `#A33A3A` |
| Linien und Feldrahmen | ein Ton zwischen Eingelassener Fläche und Sekundärtext, der gegen Weiß mindestens 3:1 erreicht |

- Ein Farbton für alle Flächen, nur die Helligkeit variiert.
- Der Akzent erscheint nur bei Hauptaktion, Fokus, aktueller Auswahl und den Zeitfenstern. Keine weiteren Akzentfarben, keine Verläufe.
- Weiße Schrift steht nur auf dem Akzent (Hauptbutton).

## Schrift

- Ausschließlich Systemschriften: `system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif` für alles. Das ist eine bewusste Entscheidung dieses Projekts (keine Webfonts erlaubt), keine Nachlässigkeit. Charakter entsteht über Gewicht, Größenverhältnis und Abstände.
- Größen (Schriftgröße/Zeilenhöhe): Fließtext 16/24 px, Hilfs- und Metatext 14/20 px, Abschnittstitel 22/28 px, Seitentitel 32/40 px.
- Gewichte: Text 400, Labels und Zeitangaben 500–600, Titel 600–700.
- Uhrzeiten, Daten und Dauern mit `font-variant-numeric: tabular-nums`.

## Abstände, Radien, Tiefe

- Abstandsskala: 4, 8, 12, 16, 24, 32, 48 px. Innerhalb zusammengehöriger Felder 12–16 px, zwischen Formulargruppen 24 px, zwischen großen Bereichen 32–48 px.
- Radien: 8 px an Bedienelementen, 12 px an größeren Flächen. Keine anderen Werte.
- Tiefe: Flächen und feine Linien statt Schatten. Höchstens ein sehr dezenter Schatten für Hauptflächen, einheitlich verwendet.

## Aufbau

- **Ab etwa 960 px Breite:** links ein rund 320 px breiter Suchbereich (Ort, Zeitraum, Aktivität, Mindestdauer, Wetterwünsche, Profil speichern), rechts eine flexible Ergebnisfläche. Beide Spalten sind gleich hell und beginnen auf derselben Höhe.
- **Schmale Bildschirme:** zuerst eine kompakte Zusammenfassung der Suche (Ort, Zeitraum, Aktivität) mit Link „Suche anpassen“, dann die Ergebnisse, danach das Formular.
- Gemeinsame Kopfzeile mit Navigation (Planung, Ortssuche, Suchprofile) und eine schlichte Fußzeile mit den Quellenangaben.

## Ergebnisse (das prägende Element)

- Eine Zeile pro Tag im gewählten Zeitraum, alle mit **derselben Zeitskala 0–24 Uhr** (dezente Stundenmarken bei 0, 6, 12, 18, 24).
- Jedes Zeitfenster erscheint als hellblaues Intervall im Band **und** als Text daneben bzw. darunter: Beginn, Ende und Dauer, etwa „17:00–24:00 · 7 Std.“. Dauer in Stunden und Minuten, nicht in reinen Minuten.
- Fenster über Mitternacht werden in beiden Tageszeilen gezeigt und als ein zusammenhängendes Fenster beschriftet (z. B. „Sa 19:00 – Mo 00:00 · 29 Std.“).
- Wetterdetails (Temperatur-, Wind-, Niederschlagsbereich im Fenster) als zweite Informationsebene, zum Beispiel über ein natives `<details>`-Element.
- Bereits vergangene Stunden des heutigen Tags dezent ausgegraut.
- „Keine passenden Zeitfenster“ ist ein normales Ergebnis in ruhigem Stil mit konkreten Vorschlägen, kein Fehler. Rot nur für echte Fehler.

## Formular und Suchprofile

- Native Formularelemente. Für die Wetterwünsche sind Optionsfelder mit sichtbaren Grenzwerten (z. B. „Höchstens mäßiger Wind · bis 30 km/h“) ausdrücklich erwünscht, weil sie mit einem Klick bedienbar sind.
- Suchprofile als ruhige Liste mit klarer Hauptaktion („Profil verwenden“) und zurückhaltenden Nebenaktionen (Bearbeiten, Löschen).

## Gleichgewicht (Prüffrage vor dem Abschluss)

Kneif die Augen zusammen: Wirkt die Seite als ein ruhiges Ganzes, in dem man das Ergebnis schnell findet, ohne dass irgendein Block grell herausspringt? Wenn ein Element zuerst und zu laut ins Auge fällt, ist es zu stark.
