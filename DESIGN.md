# Gestaltung von „Wann passt es?“

Die Planung liest sich wie ein Stundenplan fürs Draußensein. Ein blaues Ergebnisfeld gibt der Antwort den größten Raum. Die Einstellungen stehen auf weißem Grund und folgen der Reihenfolge Ort, Zeitraum, Aktivität und Wetter.

## Farben

| Rolle | Hex |
| --- | --- |
| Seitenfläche und helle Schrift im Ergebnisfeld | `#ffffff` |
| Text | `#151925` |
| Nebeninformationen und feine Trennlinien | `#50596c`, `#d6d9e1` |
| Rahmen der Bedienelemente | `#626979` |
| Akzent, Hover-Abstufung und helle Abstufung | `#243ca6`, `#182b7d`, `#eef1ff` |
| Erfolg | `#15583c` auf `#e9f5ee` |
| Warnung bei fehlenden Treffern | `#784704` auf `#fff3d9` |
| Fehler | `#8b282d` auf `#fff0f0` |

Die bisherige Akzentfarbe `#185740` hat einen HSL-Farbton von 158,1°. Das neue Kobaltblau `#243ca6` liegt bei 228,9°. Der Abstand auf dem Farbkreis beträgt 70,8°.

## Schrift

Überschriften setzen sich in „Iowan Old Style“, „Palatino Linotype“, Palatino, Charter, „Bitstream Charter“, Georgia, serif vom Rest ab. Text, Navigation, Felder und Ergebnisdaten verwenden system-ui, -apple-system, „Segoe UI“, Roboto, sans-serif. Daten, Uhrzeiten und Dauern verwenden Tabellenziffern.

| Größe | Rolle |
| --- | --- |
| 0,875 rem | Hinweise, Datumszeilen und ergänzende Angaben |
| 1 rem | Text, Navigation und Bedienelemente |
| 1,25 rem | Unterüberschriften und Absender |
| 1,65 rem | Abschnittsüberschriften und mobile Uhrzeiten |
| 2,25 rem | Mobile Seitentitel und breite Uhrzeiten |
| 3,5 rem | Breite Seitentitel |

## Layout

Mobil stehen die Ergebnisse direkt unter dem Titel. Der Sprunglink zu den Einstellungen hält auch bei langen Ergebnislisten den Weg zum Formular kurz. Ort und Zeitraum stehen am Anfang des Formulars, lange Auswahllisten erhalten die volle Breite. Auf großen Bildschirmen stehen die kompakte Einstellspalte links und die breitere Ergebnisfläche rechts. Ortssuche, Profile und Fehlerseiten teilen sich die linke Flucht. Die Profile und Ortsvorschläge stehen in Zeilen mit Trennlinien.

```text
360 px
┌───────────────────────┐
│ Wann passt es?        │
│ Planung  Orte  Profile│
├───────────────────────┤
│ Wetterfenster finden  │
│ Suche anpassen        │
│ ╔═══════════════════╗ │
│ ║Passende Zeitfenster║ │
│ ║08:00 ──── 11:00   ║ │
│ ║       180 Min.    ║ │
│ ╚═══════════════════╝ │
│ Einstellungen         │
│ Ort, Zeitraum, Wetter │
│ Zeitfenster suchen    │
│ Suchprofil speichern  │
└───────────────────────┘

1280 px
┌────────────────────────────────────────────────────────────────────┐
│ Wann passt es?                          Planung  Ortssuche  Profile│
├────────────────────────────────────────────────────────────────────┤
│ Wetterfenster finden                                               │
│ Einstellungen          ╔═════════════════════════════════════════╗ │
│ Ort + Ort ändern       ║ Passende Zeitfenster                     ║ │
│ Startdatum  Enddatum   ║ Ort und Aktivität                         ║ │
│ Aktivität   Dauer      ║ 08:00 ─────── 180 Min. ────────── 11:00  ║ │
│ Wetterwünsche          ╚═════════════════════════════════════════╝ │
│ Zeitfenster suchen                                                 │
│ Suchprofil speichern                                               │
└────────────────────────────────────────────────────────────────────┘
```

## Prägendes Element

Das Ergebnisfeld verbindet Start und Ende eines Fensters mit einer waagerechten Linie. Beginn und Ende haben je eine Datumszeile. So bleibt auch über Mitternacht klar, zu welchem Tag die beiden Uhrzeiten gehören. Die Zahl unter oder auf der Verbindung gibt die Dauer an. Die Linie verbindet die beiden Zeitpunkte ohne Maßstab.

## Selbstprüfung

Ein Raster aus Karten für Planung, Wetterwünsche und Profile wäre mit beliebigen Inhalten einsetzbar. Im Entwurf habe ich es durch offene Formularabschnitte und Listenzeilen ersetzt. Wetterfenster erhalten als einzige Inhaltsgruppe eine vollflächige Akzentfarbe. Das frühere dekorative Verlaufszeichen entfällt. Den allgemeinen Einführungssatz auf der Planung ersetzt der konkrete Seitentitel.
