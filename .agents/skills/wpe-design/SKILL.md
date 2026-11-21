---
name: wpe-design
description: Gestaltungsrichtlinien für das Frontend von „Wann passt es?“. Verwenden, sobald Templates, CSS oder clientseitiges JavaScript der Anwendung gestaltet oder überarbeitet werden. Enthält feste Vorgaben (Schrift, Farben, Layout, Barrierefreiheit), Prinzipien gegen den generischen KI-Look und einen verbindlichen Ablauf mit visueller Selbstkontrolle.
---

# Gestaltung von „Wann passt es?“

Ziel ist ein Frontend mit eigenem Charakter, das man nicht mit einer beliebigen anderen generierten Web-App verwechseln würde. Die Gestaltung soll aus dem Thema entstehen: Wetter, Tageszeit, Draußensein, das Warten auf das richtige Zeitfenster.

Dieser Skill hat drei Teile: **feste Vorgaben** (nicht verhandelbar), **Prinzipien** (Richtung, innerhalb derer du frei entscheidest) und einen **verbindlichen Ablauf**.

## 1. Feste Vorgaben

### Schrift
- Überschriften in einer Serifenschrift aus dem System-Stack: `"Iowan Old Style", "Palatino Linotype", Palatino, Charter, "Bitstream Charter", Georgia, serif`.
- Fließtext, Formulare und Bedienelemente in `system-ui, -apple-system, "Segoe UI", Roboto, sans-serif`.
- Keine weiteren Schriftfamilien, keine Webfonts, keine Monospace-Schrift für Datenlabels.
- Uhrzeiten, Daten und Dauern mit `font-variant-numeric: tabular-nums`.
- Eine bewusste Schriftgrößen-Skala mit höchstens sechs Stufen, als CSS-Variablen definiert.

### Farbe
- **Hintergrund der Seite:** entweder reines Weiß (`#ffffff`) oder eine deutlich farbige bzw. dunkle Fläche. Verboten sind fast weiße Hintergründe mit leichtem Farbstich (Creme, Beige, Mintgrau, Blaugrau, Greige), also Helligkeit über 94 % bei geringer Sättigung. Genau das ist der typische Standard-Look.
- **Eine** Akzentfarbe für Aktionen und Hervorhebungen, dazu semantische Farben für Erfolg, Warnung und Fehler.
- Die Akzentfarbe muss sich im Farbton um **mindestens 40°** von der Akzentfarbe unterscheiden, die dieses Projekt vor der Überarbeitung verwendet hat. Prüfe die bisherige Farbe im bestehenden CSS und nenne beide Werte in `DESIGN.md`.
- Verboten als Akzentfarbe: Grün, Türkis und Petrol (Farbton 90°–200°). Grün darf nur semantisch für „passt“ bzw. Erfolg verwendet werden.
- Keine Farbverläufe als Dekoration.
- Alle Farben als CSS-Variablen; maximal sechs Grundfarben plus Abstufungen.

### Form und Struktur
- Nicht jeden Inhalt in eine eigene weiße Karte mit Schatten packen. Gliederung entsteht vorrangig durch Typografie, Abstand und Ausrichtung.
- Höchstens zwei Eckenradien im ganzen Projekt; Schatten nur, wo etwas wirklich über anderem liegt.
- Keine Versal-Labels („EYEBROWS“) über Überschriften, keine Aufzählungen mit Mittelpunkt („A · B · C“), keine an Buttons angehängten Pfeile („→“).
- Symbole nur als Inline-SVG, keine Emojis.

### Layout und Barrierefreiheit
- **Ergebnisse zuerst:** Auf der Planungsseite muss bei 1280 × 800 px das erste Zeitfenster bzw. der Leerzustand ohne Scrollen sichtbar sein.
- Bei 360 px und 1280 px Breite kein horizontales Scrollen und **kein abgeschnittener Text** in Feldern, Auswahllisten, Buttons oder Karten.
- Kontrast: Text mindestens 4,5:1, große Schrift mindestens 3:1, Rahmen und Fokusmarkierungen von Bedienelementen mindestens **3:1** gegen ihren Hintergrund. Rechnerisch nach WCAG prüfen.
- Sichtbarer Fokus, verknüpfte Labels, Tastaturbedienung; alles funktioniert ohne JavaScript.
- `prefers-reduced-motion` respektieren.

## 2. Prinzipien

- **Vom Thema ausgehen.** Leite Stimmung, Farbe und das eine prägende Element aus dem Inhalt ab, nicht aus einer Vorlage. Was ist das Charakteristische an „das richtige Zeitfenster finden“?
- **Die Ergebnisse sind die Hauptsache.** Das Formular ist Werkzeug, die passenden Zeitfenster sind die Antwort. Die Hierarchie der Seite soll das zeigen.
- **Daten sichtbar machen, wo es hilft.** Zeit lässt sich räumlich darstellen. Wähle eine Darstellung, die die Frage „wann und wie lange?“ auf einen Blick beantwortet, und begründe sie.
- **Ein mutiges Element, der Rest ruhig.** Setze genau eine Stelle bewusst auffällig und halte alles drumherum diszipliniert. Entferne vor dem Abschluss ein Element, das nichts beiträgt.
- **Texte sind Gestaltung.** Kurz, konkret, aus Sicht der Nutzerin. Buttons sagen, was passiert. Fehler- und Leerzustände erklären, was zu tun ist, ohne sich zu entschuldigen.
- **Mobil zuerst.** Gestalte für 360 px und erweitere für breite Bildschirme, statt die Desktop-Ansicht zusammenzuquetschen.

## 3. Verbindlicher Ablauf

1. **Konzept vor Code.** Lege `DESIGN.md` im Projektwurzelverzeichnis an mit:
   - Leitidee in zwei bis drei Sätzen,
   - Farbpalette als benannte Hex-Werte, inklusive alter und neuer Akzentfarbe mit Farbtonabstand,
   - Schriftrollen und Größen-Skala,
   - Layout als kurze Beschreibung und ASCII-Skizze für 360 px und 1280 px,
   - das eine prägende Element,
   - **Selbstprüfung:** Welche Teile deines Plans würdest du genauso für jede andere App vorschlagen? Ändere sie und notiere, was du geändert hast.
2. **Umsetzen** nach dem Konzept.
3. **Visuell prüfen** mit dem Browser-Werkzeug, das dir zur Verfügung steht (z. B. Playwright-MCP). Rufe jede Seite bei 360 × 800 und 1280 × 800 auf: Planung mit Ergebnissen, Planung ohne Treffer, Planung mit ungültigem Zeitraum, Ortssuche, Profilübersicht, Profil bearbeiten, 404. Prüfe jeweils:
   - Ist das erste Ergebnis bei 1280 × 800 ohne Scrollen sichtbar?
   - Gibt es abgeschnittene Texte oder horizontales Scrollen (`document.documentElement.scrollWidth > window.innerWidth`)?
   - Stimmen Hierarchie, Abstände und Ausrichtung mit dem Konzept überein?
   - Wirkt die Seite wie eine generische KI-App? Wenn ja: Was genau, und was änderst du?
4. **Korrigieren und erneut prüfen**, bis alle Punkte erfüllt sind.
5. **Dokumentieren** im Abschlussbericht: welche Seiten und Breiten du wie geprüft hast (Screenshot, Seitenstruktur, Messung), welche Probleme du gefunden und wie du sie behoben hast. Unterscheide klar, was du tatsächlich gesehen hast und was du nur aus dem Code ableitest.
