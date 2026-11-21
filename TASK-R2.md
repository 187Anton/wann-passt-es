# TASK-R2.md – Runde 2: Neugestaltung des Frontends

Dieser Branch enthält deine Umsetzung aus Runde 1 (`TASK.md`). In Runde 2 wird **nur das Frontend** neu gestaltet. Grundlage ist der Design-Skill `.agents/skills/wpe-design/SKILL.md`. Lies ihn vollständig und befolge ihn, zusammen mit `AGENTS.md`.

## Rahmenbedingungen

- Die Funktionalität aus Runde 1 bleibt vollständig erhalten. Alle Kriterien aus `TASK.md` gelten weiter.
- Keine Änderungen an Fachlogik, Datenmodell oder Wetterregeln. Controller nur anpassen, wenn das Template es zwingend erfordert.
- URLs, HTTP-Methoden und Namen der Request-Parameter bleiben unverändert (z. B. `/`, `/locations`, `/profiles`, `/profiles/{id}/edit`, `startDate`, `endDate`, `profileName`).
- Es gelten weiter die Frontend-Regeln aus `TASK.md`: Thymeleaf, eigenes CSS, optional Vanilla-JavaScript, keine Bibliotheken, keine CDNs, keine Webfonts, kein Build-Schritt.
- Falls im Projektwurzelverzeichnis eine Datei `.port` existiert, starte die Anwendung für deine Prüfungen auf dem darin genannten Port. `.port` nicht committen.
- `AGENTS.md`, `CLAUDE.md`, `TASK.md`, `TASK-R2.md` und den Skill nicht verändern.

## Akzeptanzkriterien

| ID | Kriterium |
| --- | --- |
| R1 | `DESIGN.md` existiert und enthält alle in Abschnitt 3.1 des Skills verlangten Punkte, inklusive Selbstprüfung gegen den generischen Look. |
| R2 | Schriften entsprechen den festen Vorgaben (Serifen-Stack für Überschriften, System-Stack für Text, `tabular-nums` für Zeiten und Dauern, keine weiteren Familien). |
| R3 | Seitenhintergrund ist reines Weiß oder eine deutlich farbige bzw. dunkle Fläche, kein fast weißer Ton mit Farbstich. |
| R4 | Genau eine Akzentfarbe; nicht im Bereich 90°–200°; Farbtonabstand zur bisherigen Akzentfarbe mindestens 40° (beide Werte in `DESIGN.md`). |
| R5 | Keine der im Skill verbotenen Muster (einheitliches Karten-Kit, mehr als zwei Eckenradien, Versal-Labels, Mittelpunkt-Aufzählungen, Pfeile an Buttons, Emojis, dekorative Verläufe). |
| R6 | Auf der Planungsseite ist bei 1280 × 800 px das erste Zeitfenster bzw. der Leerzustand ohne Scrollen sichtbar. |
| R7 | Bei 360 und 1280 px auf allen Seiten kein horizontales Scrollen und kein abgeschnittener Text in Feldern, Auswahllisten, Buttons oder Karten. |
| R8 | Kontraste: Text ≥ 4,5:1, große Schrift ≥ 3:1, Rahmen und Fokus von Bedienelementen ≥ 3:1, rechnerisch geprüft und im Bericht mit den Werten belegt. |
| R9 | Visuelle Selbstkontrolle gemäß Abschnitt 3.3 des Skills für alle genannten Seiten bei beiden Breiten durchgeführt und dokumentiert. |
| R10 | Alle Kriterien aus `TASK.md` sind weiterhin erfüllt; `./mvnw test` läuft grün; markupabhängige Tests und `src/test/browser/profile-flow.mjs` sind angepasst. |
| R11 | `AGENT_REPORT_R2.md` angelegt: Gestaltungskonzept in Kürze, Zuordnung zu R1–R12, Prüfprotokoll der visuellen Kontrolle mit gefundenen und behobenen Problemen, verbleibende Schwächen. |
| R12 | Änderungen in nachvollziehbaren Schritten auf dem aktuellen Branch committet; nichts gepusht. |
