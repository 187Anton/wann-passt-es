# TASK-R3.md – Runde 3: Review des Ergebnisses eines anderen Agenten

In dieser Runde schreibst du **keinen Anwendungscode**. Du begutachtest den fertigen Stand eines anderen Agenten so, wie es ein sorgfältiger Senior-Entwickler bei einem Pull Request tun würde. Welchen Branch du prüfst, steht im Prompt (im Folgenden: **Ziel-Branch**).

## Was geprüft wird

Der Ziel-Branch enthält die Umsetzung von `TASK.md` (Runde 1: Profile, mehrtägiger Zeitraum, Robustheit, Frontend) und darauf aufbauend `TASK-R2B.md` (Runde 2b: Frontend nach `.interface-design/system.md`). Ausgangspunkt beider Runden ist der Tag `agentic-baseline`.

Prüfumfang ist die gesamte Änderung:

```bash
git diff agentic-baseline..<Ziel-Branch> -- . ':!.agents' ':!.claude' ':!.interface-design' ':!TASK-R2B.md'
```

Die Aufgabendateien, der Skill und das Designsystem selbst gehören nicht zum Prüfumfang, wohl aber, ob der Code sie einhält. Berichte des Agenten (`AGENT_REPORT*.md`) darfst du lesen, aber prüfe ihre Aussagen selbst, statt sie zu übernehmen.

## Vorgehen

1. Lege für den Ziel-Branch einen **temporären Worktree außerhalb deines Projektordners** an. Der Pfad enthält den Namen deines Projektordners, damit parallel laufende Reviews sich nicht in die Quere kommen:

   ```bash
   git worktree add --detach "/tmp/r3-review-$(basename "$PWD")" <Ziel-Branch>
   ```

   Führe dort `./mvnw test` aus. Du darfst die Anwendung dort zum Ausprobieren starten. Verwende dafür den Port aus deiner eigenen Datei `.port`, falls vorhanden.
2. Prüfe die Änderung mindestens auf:
   - **Korrektheit:** Akzeptanzkriterien aus `TASK.md` und `TASK-R2B.md`, Randfälle (Mitternacht, Zeitraumgrenzen, ungültige Eingaben, doppelte Profilnamen, leere Ergebnisse, Zeitzonen).
   - **Sicherheit:** Eingabevalidierung, Ausgabe-Escaping in Templates (`th:utext`, Inline-JavaScript), Fehlerseiten ohne interne Details.
   - **Tests:** Decken sie das geänderte Verhalten ab? Gibt es Lücken, schwache oder irreführende Tests?
   - **Wartbarkeit:** Verständlichkeit, Duplikate, unnötige Komplexität, Abweichungen von bestehenden Mustern im Projekt.
   - **Frontend:** Einhaltung von `system.md`, Barrierefreiheit (Labels, Fokus, Tastaturbedienung, Kontrast), Darstellung bei 360 und 1280 px. Ein Browser steht dir über das Playwright-MCP zur Verfügung.
3. Ändere **nichts** am Ziel-Branch: keine Commits, keine Pushes, keine Änderungen an seinen Dateien.
4. Entferne den temporären Worktree am Ende wieder:

   ```bash
   git worktree remove --force "/tmp/r3-review-$(basename "$PWD")"
   ```

## Ergebnis: `REVIEW-R3.md`

Lege im Wurzelverzeichnis **deines eigenen** Worktrees die Datei `REVIEW-R3.md` an, mit diesem Aufbau:

1. **Kopf:** geprüfter Branch und Commit, Datum, was du tatsächlich ausgeführt hast (Tests mit Ergebnis, gestartete Seiten, Werkzeuge).
2. **Gesamturteil:** eine von *„merge-fähig“*, *„merge-fähig nach kleinen Änderungen“*, *„nicht merge-fähig“*, mit zwei bis drei Sätzen Begründung.
3. **Befunde:** höchstens 15, nach Schwere sortiert (*kritisch*, *hoch*, *mittel*, *niedrig*). Jeder Befund enthält:
   - Datei und Zeile bzw. Seite,
   - was falsch ist und warum es relevant ist,
   - **Nachweis:** *verifiziert* (mit dem Befehl, Test oder Schritt, der es zeigt) oder *Vermutung* (nur aus dem Lesen des Codes),
   - einen konkreten Verbesserungsvorschlag.
4. **Stärken:** bis zu fünf Punkte, die im Ziel-Branch besonders gut gelöst sind und sich zur Übernahme eignen.
5. **Übernahmeempfehlung:** Was sollte man aus diesem Branch in die endgültige Fassung übernehmen, was nicht?

Keine Befunde erfinden, um die Liste zu füllen. Ein kurzes Review mit wenigen, belegten Befunden ist besser als ein langes mit Vermutungen.

## Abschluss

Committe ausschließlich `REVIEW-R3.md` auf deinem aktuellen Branch. Nichts pushen, bis du dazu aufgefordert wirst.
