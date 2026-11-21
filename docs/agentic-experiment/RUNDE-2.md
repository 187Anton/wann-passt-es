# Runde 2: Design-Skill und visuelle Selbstkontrolle per MCP

**Frage:** Bricht ein gemeinsamer Design-Skill den generischen KI-Look aus Runde 1 auf? Und können die Agenten ihre Oberfläche mit einem Browser-Werkzeug (Playwright-MCP) selbst prüfen und korrigieren?

**Aufbau:** Jeder Agent arbeitet auf **seinem eigenen Ergebnis aus Runde 1** weiter und gestaltet nur das Frontend neu. Alle bekommen denselben Skill (`.agents/skills/wpe-design/SKILL.md`), dieselbe Aufgabe (`TASK-R2.md`), denselben Prompt und dasselbe MCP in derselben Version. So lassen sich der Vorher-nachher-Effekt pro Agent und der Vergleich zwischen den Agenten messen.

| Kandidat | Ausgangsbranch | Branch Runde 2 | Worktree | Port während des Laufs |
| --- | --- | --- | --- | --- |
| Codex | `agent/codex` | `agent/codex-r2` | `../wpe-codex` | 8091 |
| Claude Code | `agent/claude-opus` | `agent/claude-opus-r2` | `../wpe-claude` | 8092 |
| Hermes | `agent/hermes-deepseek` | `agent/hermes-deepseek-r2` | `../wpe-hermes` | 8093 |

---

## 1. Werkzeug für Screenshots einrichten (einmalig)

```bash
cd ~/Projekte/wann-passt-es/docs/agentic-experiment/tools
npm install
npx playwright install chromium
```

## 2. Vorher-Screenshots aus Runde 1

Die drei Versionen aus Runde 1 in drei Terminals starten:

```bash
cd ~/Projekte/wpe-codex && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

```bash
cd ~/Projekte/wpe-claude && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

```bash
cd ~/Projekte/wpe-hermes && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8083
```

Dann in einem vierten Terminal:

```bash
cd ~/Projekte/wann-passt-es/docs/agentic-experiment/tools
node screenshots.mjs r1 codex=8081 claude=8082 hermes=8083
open ../screenshots/r1/index.html
```

Das Skript erzeugt pro Kandidat 7 Seiten in 2 Breiten (plus einen Ausschnitt „ohne Scrollen sichtbar“), eine Übersichtsseite `index.html` und `ergebnis.json` mit HTTP-Status und Prüfung auf horizontales Scrollen. Danach die drei Anwendungen mit Ctrl+C beenden.

## 3. Branches für Runde 2 vorbereiten

Einmalig, damit die Port-Datei nie committet wird:

```bash
echo ".port" >> ~/Projekte/wann-passt-es/.git/info/exclude
```

Dann für **Codex**:

```bash
cd ~/Projekte/wpe-codex
git switch -c agent/codex-r2
unzip -o ~/Downloads/runde2-branch.zip -d .
mkdir -p .claude/skills && ln -s ../../.agents/skills/wpe-design .claude/skills/wpe-design
git add TASK-R2.md .agents .claude
git commit -m "Add round 2 task and design skill"
echo 8091 > .port
```

Für **Claude** genauso, mit `cd ~/Projekte/wpe-claude`, Branch `agent/claude-opus-r2` und `echo 8092 > .port`.

Für **Hermes** genauso, mit `cd ~/Projekte/wpe-hermes`, Branch `agent/hermes-deepseek-r2` und `echo 8093 > .port`.

Der Symlink unter `.claude/skills` sorgt dafür, dass Claude Code den Skill automatisch findet. Hermes liest `.agents/skills` als projektbezogene Skills. Damit alle drei den Skill sicher lesen, nennt ihn der Start-Prompt zusätzlich ausdrücklich.

## 4. Gleiche Bedingungen herstellen

**Globale Codex-Regeln während des Laufs ausblenden:**

```bash
mv ~/.codex/AGENTS.md ~/.codex/AGENTS.md.bak
```

**Playwright-MCP in allen drei Werkzeugen eintragen**, mit derselben festen Version und denselben Optionen. `--headless` startet den Browser unsichtbar, `--isolated` ist nötig, damit drei Agenten gleichzeitig einen Browser nutzen können.

Claude Code, im Ordner `~/Projekte/wpe-claude`:

```bash
claude mcp add playwright -- npx -y @playwright/mcp@0.0.84 --headless --isolated
```

Codex, in die Datei `~/.codex/config.toml` anhängen:

```bash
cat >> ~/.codex/config.toml <<'EOF'

[mcp_servers.playwright]
command = "npx"
args = ["-y", "@playwright/mcp@0.0.84", "--headless", "--isolated"]
EOF
```

Hermes, in `~/.hermes/config.yaml` unter `mcp_servers:` ergänzen (falls der Abschnitt schon existiert, nur den Eintrag `playwright` darunter einfügen):

```yaml
mcp_servers:
  playwright:
    command: "npx"
    args: ["-y", "@playwright/mcp@0.0.84", "--headless", "--isolated"]
```

Außerdem für Hermes im Ordner `~/Projekte/wpe-hermes` die Projekt-Skills freigeben:

```bash
hermes skills trust
```

Beim Start von Hermes stand in Runde 1 „Skills toolset disabled“. Das Skills-Werkzeug muss für Runde 2 aktiv sein; die Einstellung findest du über `hermes tools` bzw. `/help`.

**Kurztest vor dem eigentlichen Lauf**, jeweils in einer *separaten* Wegwerf-Session: Frag „Welche MCP-Werkzeuge und Skills stehen dir zur Verfügung?“ und beende die Session wieder. Danach erst die echte, frische Session starten.

## 5. Durchführung

Modelle und Einstellungen wie in Runde 1: GPT-6.2-Sol high, Claude Opus 5.5, deepseek-flash. Freigabemodus von Anfang an so einstellen wie am Ende von Runde 1 (Codex: nur bei riskanten Aktionen fragen).

Start-Prompt, für alle drei wörtlich gleich:

```text
Lies AGENTS.md, README.md, TASK-R2.md und den Skill unter .agents/skills/wpe-design/SKILL.md vollständig. Setze danach TASK-R2.md vollständig und selbstständig um. Für die visuelle Prüfung steht dir ein Browser über das Playwright-MCP zur Verfügung. Frag nur bei echten Blockern nach. Wenn du fertig bist, lege AGENT_REPORT_R2.md an, committe alles auf dem aktuellen Branch und gib mir eine kurze Zusammenfassung.
```

Erlaubte Standardantworten, Zeitmessung und Nachlauf-Prompt zum Pushen wie in Runde 1 (`EXPERIMENT.md`, Abschnitt 4). Ausnahme: Erlaubt ein Agent nach Rückfrage, den Playwright-Browser zu installieren, zählt das als Freigabe, nicht als Eingriff.

## 6. Danach

1. Messwerte erfassen wie in Runde 1 (Zeit, Tokens, Kosten, Freigaben, Commits, Tests).
2. Globale Codex-Regeln zurückholen:

   ```bash
   mv ~/.codex/AGENTS.md.bak ~/.codex/AGENTS.md
   ```

3. Die drei Runde-2-Stände auf den Ports 8081–8083 starten (wie in Abschnitt 2, jeweils im Worktree) und die Screenshots erzeugen:

   ```bash
   cd ~/Projekte/wann-passt-es/docs/agentic-experiment/tools
   node screenshots.mjs r2 codex=8081 claude=8082 hermes=8083
   ```

4. Ordner `docs/agentic-experiment/screenshots/r1` und `r2` zippen und zur Auswertung hochladen, Messwerte dazuschreiben.

## 7. Messwerte Runde 2

| Messgröße | Codex | Claude Code | Hermes + DeepSeek |
| --- | --- | --- | --- |
| Arbeitszeit | | | |
| Tokens Input / Cache / Output | | | |
| Kosten | | | |
| Eingriffe / Freigaben | | | |
| Commits | | | |
| Tests gesamt / bestanden / übersprungen | | | |
| MCP tatsächlich genutzt (ja/nein, wofür) | | | |
| Skill tatsächlich gelesen (laut Report/Verlauf) | | | |
| R1–R12 erfüllt | | | |
