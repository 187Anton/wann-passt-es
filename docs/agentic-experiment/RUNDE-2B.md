# Runde 2b: Community-Skill und positives Zielbild

**Frage:** Führt ein erprobter Community-Skill mit einem positiv beschriebenen Designsystem zu stimmigeren Oberflächen als der Verbots-Skill aus Runde 2?

**Aufbau:** Alle drei Agenten starten **vom Stand aus Runde 1** und gestalten nur das Frontend neu. Alle bekommen identisch:

- den Skill `interface-design` (Dammyjay93/interface-design, MIT, unverändert, mit Prüfsumme in `SOURCE.md`),
- das Designsystem `.interface-design/system.md` in der Richtung „Himmel & Schiefer“ (aus einer ChatGPT-Recherche vom 22.11.2026, vom Nutzer ausgewählt),
- die Aufgabe `TASK-R2B.md`, denselben Prompt und das Playwright-MCP, diesmal mit `--browser chromium`.

Vergleich: Runde 1 (ohne Skill) → Runde 2 (Verbots-Skill) → Runde 2b (Community-Skill plus Zielbild), jeweils ab demselben Runde-1-Stand.

| Kandidat | Ausgangsbranch | Branch Runde 2b | Worktree | Port während des Laufs |
| --- | --- | --- | --- | --- |
| Codex | `agent/codex` | `agent/codex-r2b` | `../wpe-codex` | 8091 |
| Claude Code | `agent/claude-opus` | `agent/claude-opus-r2b` | `../wpe-claude` | 8092 |
| Hermes | `agent/hermes-deepseek` | `agent/hermes-deepseek-r2b` | `../wpe-hermes` | 8093 |

---

## 1. Alles beenden, was noch läuft

```bash
lsof -ti :8081 -ti :8082 -ti :8083 -ti :8091 -ti :8092 -ti :8093 | xargs kill
```

Eine Meldung wie „usage: kill“ heißt nur, dass nichts mehr lief.

## 2. Branches für Runde 2b anlegen

Die Datei `runde2b-branch.zip` muss in `~/Downloads` liegen.

**Codex:**

```bash
cd ~/Projekte/wpe-codex
git switch -c agent/codex-r2b agent/codex
unzip -o ~/Downloads/runde2b-branch.zip -d .
mkdir -p .claude/skills && ln -s ../../.agents/skills/interface-design .claude/skills/interface-design
git add TASK-R2B.md .agents .interface-design .claude
git commit -m "Add round 2b task, design system and interface-design skill"
```

**Claude:**

```bash
cd ~/Projekte/wpe-claude
git switch -c agent/claude-opus-r2b agent/claude-opus
unzip -o ~/Downloads/runde2b-branch.zip -d .
mkdir -p .claude/skills && ln -s ../../.agents/skills/interface-design .claude/skills/interface-design
git add TASK-R2B.md .agents .interface-design .claude
git commit -m "Add round 2b task, design system and interface-design skill"
```

**Hermes:**

```bash
cd ~/Projekte/wpe-hermes
git switch -c agent/hermes-deepseek-r2b agent/hermes-deepseek
unzip -o ~/Downloads/runde2b-branch.zip -d .
mkdir -p .claude/skills && ln -s ../../.agents/skills/interface-design .claude/skills/interface-design
git add TASK-R2B.md .agents .interface-design .claude
git commit -m "Add round 2b task, design system and interface-design skill"
hermes skills trust
```

**Kontrolle:**

```bash
cd ~/Projekte/wann-passt-es
git worktree list
for d in codex claude hermes; do echo "== $d"; cat ../wpe-$d/.port; git -C ../wpe-$d status --short; ls ../wpe-$d/.interface-design; done
```

Erwartet: Branches `agent/…-r2b`, Ports 8091/8092/8093, keine offenen Änderungen, je eine `system.md`.

## 3. Gleiche Bedingungen

**Globale Codex-Regeln ausblenden**, falls vorhanden:

```bash
ls ~/.codex | grep -i agents
```

Steht dort `AGENTS.md` (ohne `.bak`):

```bash
mv ~/.codex/AGENTS.md ~/.codex/AGENTS.md.bak
```

**Playwright-MCP überall auf Chromium umstellen.**

Claude Code, im Ordner `~/Projekte/wpe-claude`:

```bash
cd ~/Projekte/wpe-claude
claude mcp remove playwright
claude mcp add playwright -- npx -y @playwright/mcp@0.0.84 --browser chromium --headless --isolated
claude mcp list | grep playwright
```

Codex und Hermes (ändert in beiden Konfigurationsdateien nur die Argumentliste, vorher wird gesichert):

```bash
cp ~/.codex/config.toml ~/.codex/config.toml.vor-runde2b
cp ~/.hermes/config.yaml ~/.hermes/config.yaml.vor-runde2b
python3 - <<'EOF'
import pathlib
old = '"@playwright/mcp@0.0.84", "--headless", "--isolated"'
new = '"@playwright/mcp@0.0.84", "--browser", "chromium", "--headless", "--isolated"'
for name in [".codex/config.toml", ".hermes/config.yaml"]:
    p = pathlib.Path.home() / name
    s = p.read_text()
    print(name, "Vorkommen:", s.count(old))
    p.write_text(s.replace(old, new))
EOF
grep -n "chromium" ~/.codex/config.toml ~/.hermes/config.yaml
```

Bei beiden Dateien sollte „Vorkommen: 1“ stehen und danach je eine Zeile mit `chromium` erscheinen.

## 4. MCP wirklich testen

Diesmal nicht nur die Werkzeugliste prüfen, sondern einen echten Aufruf, jeweils in einer **Wegwerf-Session** im jeweiligen Worktree:

```text
Rufe mit dem Playwright-MCP die Seite about:blank auf und mache einen Screenshot. Antworte nur mit „MCP funktioniert“ oder mit der genauen Fehlermeldung. Falls der Browser fehlt, installiere ihn mit dem Installationswerkzeug des MCP und versuche es erneut.
```

Danach `/exit`. Erst wenn alle drei „MCP funktioniert“ melden, geht es weiter.

## 5. Durchführung

Modelle wie bisher: GPT-6.2-Sol high (Codex, Freigaben „nur riskante Aktionen“), Claude Opus 5.5 (auto mode), deepseek-flash über DeepSeek (Hermes, in der Statuszeile prüfen). Bei Hermes vorher das Guthaben abfragen:

```bash
curl -s https://api.deepseek.com/user/balance -H "Authorization: Bearer $DEEPSEEK_API_KEY"
```

Start-Prompt, für alle drei wörtlich gleich, jeweils in einer frischen Session:

```text
Lies AGENTS.md, README.md, TASK-R2B.md, den Skill unter .agents/skills/interface-design/SKILL.md und das Designsystem .interface-design/system.md vollständig. Setze danach TASK-R2B.md vollständig und selbstständig um. Für die visuelle Prüfung steht dir ein Browser über das Playwright-MCP zur Verfügung. Frag nur bei echten Blockern nach. Wenn du fertig bist, lege AGENT_REPORT_R2B.md an, committe alles auf dem aktuellen Branch und gib mir eine kurze Zusammenfassung.
```

Wenn ein Agent fertig ist: Endzeit notieren, Verbrauch ablesen (Codex über `npx ccusage@latest codex session --json`, Claude über `/usage`, Hermes über `/usage` und das DeepSeek-Guthaben), dann:

```text
Die Aufgabe ist abgeschlossen. Pushe jetzt ausschließlich deinen aktuellen Branch mit: git push -u origin HEAD
```

## 6. Danach

1. Globale Codex-Regeln zurück, falls umbenannt:

   ```bash
   mv ~/.codex/AGENTS.md.bak ~/.codex/AGENTS.md
   ```

2. Alles beenden, was die Agenten gestartet haben, und prüfen, dass keine Datenbank mehr gesperrt ist:

   ```bash
   lsof -ti :8091 -ti :8092 -ti :8093 | xargs kill
   lsof ~/Projekte/wpe-*/data/wann-passt-es.mv.db
   ```

   Die zweite Zeile sollte nichts ausgeben.

3. Die drei Stände auf 8081–8083 starten (je ein Terminal):

   ```bash
   cd ~/Projekte/wpe-codex && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
   ```

   ```bash
   cd ~/Projekte/wpe-claude && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
   ```

   ```bash
   cd ~/Projekte/wpe-hermes && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8083
   ```

4. Screenshots:

   ```bash
   cd ~/Projekte/wann-passt-es/docs/agentic-experiment/tools
   node screenshots.mjs r2b codex=8081 claude=8082 hermes=8083
   cd .. && zip -r ~/Desktop/screenshots-runde2b.zip screenshots/r2b
   ```

5. **Selbst durchklicken und bewerten** (Tabelle unten), dann `screenshots-runde2b.zip` vom Schreibtisch hochladen und Messwerte sowie Bewertung dazuschreiben.

## 7. Bewertung durch den Nutzer (1 = schlecht, 5 = sehr gut)

| Kriterium | Codex | Claude | Hermes |
| --- | --- | --- | --- |
| Gesamteindruck | | | |
| Gleichgewicht (nichts springt grell heraus) | | | |
| Ergebnisse schnell erfassbar | | | |
| Formular angenehm bedienbar | | | |
| Mobilansicht | | | |
| Passt zum Zielbild „Himmel & Schiefer“ | | | |
| Schöner als Runde 1? (ja/nein) | | | |

Freitext: Was gefällt, was stört?

## 8. Messwerte Runde 2b

| Messgröße | Codex | Claude Code | Hermes + DeepSeek |
| --- | --- | --- | --- |
| Arbeitszeit | | | |
| Tokens Input / Cache / Output | | | |
| Kosten | | | |
| Eingriffe / Freigaben | | | |
| MCP tatsächlich genutzt | | | |
| Commits | | | |
| Z1–Z12 erfüllt | | | |
