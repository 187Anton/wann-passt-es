# Runde 3: Gegenseitiges Review

**Frage:** Wie gut finden die Agenten Fehler und Schwächen im Ergebnis eines anderen Agenten, und wie belastbar sind ihre Befunde?

Jeder Agent begutachtet den Endstand eines anderen (Runde 1 plus Runde 2b) im Ringtausch. Geprüft wird gegen den Tag `agentic-baseline`. Ergebnis ist je eine Datei `REVIEW-R3.md` auf einem eigenen Branch. Am geprüften Code wird nichts verändert.

| Reviewer | Worktree | Neuer Branch (ab eigenem `-r2b`) | Prüft | Port |
| --- | --- | --- | --- | --- |
| Codex | `../wpe-codex` | `agent/codex-r3` | `agent/claude-opus-r2b` | 8091 |
| Claude Code | `../wpe-claude` | `agent/claude-opus-r3` | `agent/hermes-deepseek-r2b` | 8092 |
| Hermes | `../wpe-hermes` | `agent/hermes-deepseek-r3` | `agent/codex-r2b` | 8093 |

---

## 1. Vorbereitung

Alles beenden, was noch läuft:

```bash
lsof -ti :8081 -ti :8082 -ti :8083 -ti :8091 -ti :8092 -ti :8093 | xargs kill
```

Globale Codex-Regeln ausblenden, falls vorhanden:

```bash
ls ~/.codex | grep -i agents
```

Steht dort `AGENTS.md` (ohne `.bak`):

```bash
mv ~/.codex/AGENTS.md ~/.codex/AGENTS.md.bak
```

## 2. Branches anlegen

Die Datei `runde3.zip` muss in `~/Downloads` liegen.

```bash
cd ~/Projekte/wpe-codex
git switch -c agent/codex-r3 agent/codex-r2b
unzip -o ~/Downloads/runde3.zip TASK-R3.md -d .
git add TASK-R3.md && git commit -m "Add round 3 review task"
```

```bash
cd ~/Projekte/wpe-claude
git switch -c agent/claude-opus-r3 agent/claude-opus-r2b
unzip -o ~/Downloads/runde3.zip TASK-R3.md -d .
git add TASK-R3.md && git commit -m "Add round 3 review task"
```

```bash
cd ~/Projekte/wpe-hermes
git switch -c agent/hermes-deepseek-r3 agent/hermes-deepseek-r2b
unzip -o ~/Downloads/runde3.zip TASK-R3.md -d .
git add TASK-R3.md && git commit -m "Add round 3 review task"
```

Kontrolle:

```bash
cd ~/Projekte/wann-passt-es
git worktree list
for d in codex claude hermes; do echo "== $d"; git -C ../wpe-$d status --short; ls ../wpe-$d/TASK-R3.md; done
```

Erwartet: die drei `…-r3`-Branches, keine offenen Änderungen, je eine `TASK-R3.md`.

## 3. Durchführung

Modelle wie bisher (Codex GPT-6.2-Sol high mit „nur riskante Aktionen“, Claude Opus 5.5 im auto mode, Hermes `deepseek-flash` über DeepSeek, Statuszeile prüfen). Vorher das DeepSeek-Guthaben abfragen:

```bash
curl -s https://api.deepseek.com/user/balance -H "Authorization: Bearer $DEEPSEEK_API_KEY"
```

In jedem Worktree eine **frische Session** starten und den Prompt mit dem jeweiligen Ziel-Branch schicken.

**Codex:**

```text
Lies AGENTS.md, README.md und TASK-R3.md vollständig. Führe danach das Review für den Ziel-Branch agent/claude-opus-r2b durch, wie in TASK-R3.md beschrieben. Ändere nichts am Ziel-Branch. Frag nur bei echten Blockern nach. Wenn du fertig bist, committe REVIEW-R3.md auf dem aktuellen Branch und gib mir eine kurze Zusammenfassung.
```

**Claude Code:**

```text
Lies AGENTS.md, README.md und TASK-R3.md vollständig. Führe danach das Review für den Ziel-Branch agent/hermes-deepseek-r2b durch, wie in TASK-R3.md beschrieben. Ändere nichts am Ziel-Branch. Frag nur bei echten Blockern nach. Wenn du fertig bist, committe REVIEW-R3.md auf dem aktuellen Branch und gib mir eine kurze Zusammenfassung.
```

**Hermes:**

```text
Lies AGENTS.md, README.md und TASK-R3.md vollständig. Führe danach das Review für den Ziel-Branch agent/codex-r2b durch, wie in TASK-R3.md beschrieben. Ändere nichts am Ziel-Branch. Frag nur bei echten Blockern nach. Wenn du fertig bist, committe REVIEW-R3.md auf dem aktuellen Branch und gib mir eine kurze Zusammenfassung.
```

Wenn ein Agent fertig ist: Endzeit notieren, Verbrauch ablesen (Codex: `npx ccusage@latest codex session --json | tail -60`, Claude: `/usage`, Hermes: `/usage` und DeepSeek-Guthaben), dann:

```text
Die Aufgabe ist abgeschlossen. Pushe jetzt ausschließlich deinen aktuellen Branch mit: git push -u origin HEAD
```

## 4. Danach

```bash
mv ~/.codex/AGENTS.md.bak ~/.codex/AGENTS.md
git worktree list
```

In der Liste darf kein Eintrag unter `/tmp/r3-review-…` mehr stehen. Falls doch, für jeden übrig gebliebenen Eintrag: `git worktree remove --force /tmp/r3-review-wpe-codex` (Namen entsprechend anpassen).

Dann Messwerte und Zusammenfassungen der Agenten in den Chat geben. Die Befunde werden anschließend auf ihre Richtigkeit geprüft (verifiziert, falsch, unbelegt).

## 5. Messwerte Runde 3

| Messgröße | Codex (prüft Claude) | Claude (prüft Hermes) | Hermes (prüft Codex) |
| --- | --- | --- | --- |
| Arbeitszeit | | | |
| Tokens | | | |
| Kosten | | | |
| Eingriffe / Freigaben | | | |
| Gesamturteil | | | |
| Befunde (kritisch / hoch / mittel / niedrig) | | | |
| davon bestätigt / falsch / unbelegt | | | |
