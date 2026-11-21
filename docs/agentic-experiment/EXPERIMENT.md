# Agentic-Engineering-Experiment: drei Agenten, eine Aufgabe

Vergleich von drei Coding-Agenten an derselben Aufgabe (`TASK.md`) auf demselben Ausgangsstand (`agentic-baseline`).

| Kandidat | Werkzeug (Harness) | Modell | Branch | Worktree |
| --- | --- | --- | --- | --- |
| 1 | Codex CLI | _eintragen_ | `agent/codex` | `../wpe-codex` |
| 2 | Claude Code | Claude Opus _(Version eintragen)_ | `agent/claude-opus` | `../wpe-claude` |
| 3 | Hermes Agent | DeepSeek _(Modell eintragen)_ via API | `agent/hermes-deepseek` | `../wpe-hermes` |

Hinweis zur Aussagekraft: Verglichen wird jeweils die Kombination aus Werkzeug und Modell, nicht das Modell allein. Jeder Kandidat läuft einmal; Unterschiede können daher auch Zufall sein.

---

## 1. Ausgangsstand herstellen (einmalig)

```bash
cd ~/Projekte/wann-passt-es
git switch main && git pull

# private Übergabedateien entfernen
rm -f projektstatus.md AGENTS.md
sed -i '' '/AGENTS.md/d;/PROJECT_STATUS.md/d;/projektstatus.md/d' .git/info/exclude

# Dateien aus agentic-baseline.zip ins Projekt kopieren
unzip -o ~/Downloads/agentic-baseline.zip -d .

# README gehört zum normalen Projekt -> main
git add README.md
git commit -m "Add README with setup, features and known limitations"
git push

# Experiment-Dateien -> eigener Baseline-Branch
git switch -c agentic/baseline
git add AGENTS.md CLAUDE.md TASK.md
git commit -m "Add agent instructions and task for agentic engineering experiment"
git push -u origin agentic/baseline
git tag agentic-baseline
git push origin agentic-baseline

git switch main
```

## 2. Branches und Worktrees anlegen

```bash
cd ~/Projekte/wann-passt-es
git worktree add -b agent/codex          ../wpe-codex          agentic-baseline
git worktree add -b agent/claude-opus    ../wpe-claude         agentic-baseline
git worktree add -b agent/hermes-deepseek ../wpe-hermes        agentic-baseline
git worktree list
```

Jeder Worktree ist ein eigener Ordner mit eigenem Branch und eigener lokaler Datenbank (`./data/`). Laufen Agenten parallel und starten die Anwendung, sollten sie verschiedene Ports nutzen (z. B. 8081/8082/8083), sonst kollidiert Port 8080.

## 3. Identischer Start-Prompt

Für alle drei Kandidaten wörtlich gleich, jeweils in einer **neuen Session**, gestartet **im jeweiligen Worktree-Ordner**:

```text
Lies AGENTS.md, README.md und TASK.md vollständig. Setze danach den Auftrag aus TASK.md vollständig und selbstständig um und halte dich an die Regeln aus AGENTS.md. Frag nur bei echten Blockern nach. Wenn du fertig bist, lege AGENT_REPORT.md an, committe alles auf dem aktuellen Branch und gib mir eine kurze Zusammenfassung.
```

## 4. Durchführungsregeln

- Zeitmessung beginnt beim Absenden des Start-Prompts und endet mit der Abschlussmeldung des Agenten.
- Jeder Agent läuft im autonomsten Modus, den sein Werkzeug innerhalb des Worktrees anbietet. Den gewählten Modus pro Kandidat notieren.
- **Eingriffe** sind alle eigenen Nachrichten nach dem Start-Prompt. Erlaubt sind nur diese Standardantworten:
  - Agent hört vorzeitig auf oder fragt, ob er weitermachen soll: `Mach selbstständig weiter, bis TASK.md vollständig umgesetzt ist.`
  - Agent fragt nach einer Entscheidung, die TASK.md offenlässt: `Entscheide selbst und dokumentiere es in AGENT_REPORT.md.`
  - Echter Blocker (z. B. fehlendes Werkzeug): minimale sachliche Hilfe, wörtlich protokollieren.
- **Freigaben** (Bestätigung von Befehlen/Dateizugriffen) getrennt von Eingriffen zählen.
- Keine eigenen Codeänderungen während eines Durchlaufs.
- Nach dem Durchlauf im Worktree prüfen und dann pushen:

```bash
cd ../wpe-codex                       # bzw. wpe-claude / wpe-hermes
git status --short                    # sollte leer sein
git log --oneline agentic-baseline..HEAD
git diff --shortstat agentic-baseline..HEAD
./mvnw test                           # eigene Gegenprobe
git push -u origin agent/codex
```

Auf GitHub dann je einen Pull Request von `agent/...` gegen `agentic/baseline` öffnen (nicht mergen).

## 5. Messwerte

| Messgröße | Codex | Claude Opus | Hermes + DeepSeek |
| --- | --- | --- | --- |
| Werkzeug-Version | | | |
| Modell / Reasoning-Einstellung | | | |
| Autonomie-Modus | | | |
| Start / Ende | | | |
| Dauer (min) | | | |
| Tokens Input / Output | | | |
| Kosten bzw. Abo-Verbrauch | | | |
| Eingriffe (Anzahl) | | | |
| Freigaben (Anzahl) | | | |
| Commits | | | |
| Geänderte Dateien / +Zeilen / −Zeilen | | | |
| Tests gesamt / bestanden / fehlgeschlagen / übersprungen (eigene Gegenprobe) | | | |
| Neue Tests (Anzahl) | | | |
| Neue Abhängigkeiten | | | |
| Akzeptanzkriterien erfüllt (von 33) | | | |

**Tokens erfassen:** Codex CLI und Claude Code zeigen den Verbrauch in der Session an bzw. speichern ihn in lokalen Logs; das Kommandozeilenwerkzeug `ccusage` kann diese Logs auswerten. Werkzeugstand vor dem Durchlauf prüfen, das ändert sich schnell. Für DeepSeek am besten einen eigenen API-Key nur für diesen Durchlauf anlegen; dann zeigt das Nutzungs-Dashboard genau diesen Verbrauch.

## 6. Bewertung

**Objektiv:** Jedes Akzeptanzkriterium aus `TASK.md` (A1–A8, B1–B7, C1–C5, D1–D8, G1–G5) pro Kandidat mit erfüllt / teilweise / nicht erfüllt bewerten, mit kurzer Begründung. Die Selbstauskunft in `AGENT_REPORT.md` mit der eigenen Prüfung vergleichen: Hat der Agent ehrlich berichtet?

**Code-Qualität (je 1–5):** Lesbarkeit, Passung zur bestehenden Architektur, Testqualität, unnötige oder überraschende Änderungen, Fehlerbehandlung.

**Frontend (je 1–5):** Gesamteindruck, Verständlichkeit des Formulars, Darstellung der Ergebnisse, Mobilansicht, Konsistenz über alle Seiten. Dazu Screenshots jeder Seite bei 360 px und 1280 px Breite pro Kandidat.

**Fazit:** Stärken und Schwächen je Kandidat, Kosten-Nutzen-Verhältnis, Überraschungen, was du beim nächsten Mal anders machen würdest.

## 7. Optionale Runde 2

Einen Kandidaten erneut mit einer Erweiterung laufen lassen (z. B. einem Skill oder einem MCP-Server), gleiche Aufgabe, gleicher Prompt, neuer Branch ab `agentic-baseline`. Dann vergleichen, ob sich Messwerte und Bewertung verändern.
