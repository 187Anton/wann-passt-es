# TASK-R2B.md – Runde 2b: Frontend nach gemeinsamem Designsystem

Dieser Branch enthält deine Umsetzung aus Runde 1 (`TASK.md`). In Runde 2b wird **nur das Frontend** neu gestaltet, nach einem festen Zielbild.

Lies vorher vollständig:

1. `AGENTS.md` und `README.md`
2. den Skill `.agents/skills/interface-design/SKILL.md` (allgemeines Gestaltungshandwerk)
3. `.interface-design/system.md` (die getroffenen Designentscheidungen dieses Projekts)

## Rangfolge bei Widersprüchen

1. Die technischen Rahmenbedingungen unten und in `TASK.md`
2. `.interface-design/system.md`
3. der Skill `interface-design`

Insbesondere gilt: **keine** Frameworks, Komponenten-Bibliotheken (z. B. Radix, shadcn), Tailwind, CDNs oder Webfonts. Wo der Skill solche Werkzeuge empfiehlt, setzt du das Gleiche mit nativen HTML-Elementen, Thymeleaf-Fragmenten und eigenem CSS um. Systemschriften sind hier eine bewusste Projektentscheidung. Arbeite selbstständig: Wo der Skill vorsieht, Rückfragen zu stellen oder Vorschläge bestätigen zu lassen, entscheidest du selbst im Sinne von `system.md` und dokumentierst es.

## Rahmenbedingungen

- Die Funktionalität aus Runde 1 bleibt vollständig erhalten; alle Kriterien aus `TASK.md` gelten weiter.
- Keine Änderungen an Fachlogik, Datenmodell oder Wetterregeln. Controller nur anpassen, wenn das Template es zwingend erfordert.
- URLs, HTTP-Methoden und Namen der Request-Parameter bleiben unverändert.
- Falls im Projektwurzelverzeichnis eine Datei `.port` existiert, starte die Anwendung für deine Prüfungen auf diesem Port. `.port` nicht committen.
- `AGENTS.md`, `CLAUDE.md`, `TASK.md`, `TASK-R2B.md`, `.interface-design/system.md` und den Skill nicht verändern.

## Visuelle Prüfung

Dir steht ein Browser über das **Playwright-MCP** zur Verfügung. Nutze es, um jede Seite bei 360 × 800 und 1280 × 800 aufzurufen und anzusehen: Planung mit Ergebnissen über mehrere Tage, Planung ohne Treffer, Planung mit ungültigem Zeitraum, Ortssuche, Profilübersicht, Profil bearbeiten, 404. Korrigiere gefundene Probleme und prüfe erneut. Falls das MCP nicht funktioniert, dokumentiere den Fehler und weiche auf eine andere Möglichkeit aus.

## Akzeptanzkriterien

| ID | Kriterium |
| --- | --- |
| Z1 | Alle Farben aus `system.md` sind als CSS-Variablen umgesetzt und werden verwendet; keine weiteren Akzentfarben, keine Verläufe, keine dunklen oder vollflächig gesättigten Ergebnisblöcke. |
| Z2 | Schrift, Größen, Gewichte, Abstandsskala und Radien entsprechen `system.md`. |
| Z3 | Aufbau wie in `system.md`: ab ca. 960 px Suchbereich (~320 px) links und Ergebnisse rechts, beide gleich hell und auf gleicher Höhe; auf schmalen Bildschirmen Zusammenfassung, dann Ergebnisse, dann Formular. |
| Z4 | Ergebnisse als Tageszeilen mit gemeinsamer Zeitskala 0–24 Uhr; jedes Fenster zusätzlich mit Beginn, Ende und Dauer (Stunden/Minuten) als Text; Fenster über Mitternacht korrekt über beide Tage dargestellt und als ein Fenster beschriftet. |
| Z5 | Wetterdetails als zweite Informationsebene (z. B. `<details>`); vergangene Stunden des heutigen Tags dezent markiert. |
| Z6 | „Keine passenden Zeitfenster“ als ruhiger Normalzustand mit Vorschlägen; Rot nur für echte Fehler. |
| Z7 | Auf der Planungsseite ist bei 1280 × 800 das erste Ergebnis bzw. der Leerzustand ohne Scrollen sichtbar. |
| Z8 | Bei 360 und 1280 px auf allen Seiten kein horizontales Scrollen und kein abgeschnittener Text. |
| Z9 | Kontraste: Text ≥ 4,5:1, große Schrift ≥ 3:1, Rahmen und Fokus von Bedienelementen ≥ 3:1, rechnerisch geprüft und im Bericht belegt. |
| Z10 | Visuelle Prüfung aller genannten Seiten bei beiden Breiten durchgeführt; im Bericht steht, mit welchem Werkzeug (MCP oder Ausweichweg) und was gefunden und behoben wurde. Die Prüffrage „Gleichgewicht“ aus `system.md` ist beantwortet. |
| Z11 | Alle Kriterien aus `TASK.md` weiterhin erfüllt; `./mvnw test` grün; markupabhängige Tests und `src/test/browser/profile-flow.mjs` angepasst. |
| Z12 | `AGENT_REPORT_R2B.md` angelegt: Umsetzung des Designsystems in Kürze, Zuordnung zu Z1–Z12, Prüfprotokoll, verbleibende Schwächen. Änderungen in nachvollziehbaren Schritten committet, nichts gepusht. |
