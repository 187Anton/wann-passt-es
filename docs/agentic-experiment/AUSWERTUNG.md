# Auswertung: drei Coding-Agenten, eine Aufgabe

Durchgeführt am 21.11.2026. Aufbau, Start-Prompt und Regeln stehen in [`EXPERIMENT.md`](EXPERIMENT.md), die Aufgabe in `TASK.md` auf dem Branch `agentic/baseline` (Tag `agentic-baseline`).

> **Runde 2** (Design-Skill und visuelle Selbstkontrolle) ist am Ende dieses Dokuments ausgewertet. Kernbefund: Der Skill hat den Einheitslook verhindert, aber nach Urteil des Nutzers ein unstimmigeres Design erzeugt als Runde 1, weil er überwiegend aus Verboten bestand.
>
> **Runde 2b** (Community-Skill und positives Zielbild) folgt ganz am Ende. Kernbefund: Mit einem konkreten Designsystem wurden alle drei Oberflächen stimmig und nach Urteil des Nutzers schöner als Runde 1, gleichzeitig aber einander sehr ähnlich.
>
> **Runde 3** (gegenseitiges Review) und das **Gesamtfazit** stehen ganz am Ende. Kernbefund: Alle 19 Befunde der drei Reviews ließen sich bestätigen, keiner war falsch. Ein wichtiger Befund wurde aber in einem Review übersehen.

## Kurzfassung

Alle drei Agenten haben die komplette Aufgabe mit vier Arbeitspaketen und 33 Akzeptanzkriterien in einem einzigen autonomen Durchlauf umgesetzt, ohne Nachfragen. Alle Testsuiten sind grün, und die Selbstauskünfte zu den Tests stimmten in der Gegenprobe exakt. Bei den Kriterien liegen die Kandidaten fast gleichauf. Die eigentlichen Unterschiede zeigen sich bei Kosten, Tiefe, Testumfang, Code-Umfang und Frontend:

| | Codex | Claude Code | Hermes Agent |
| --- | --- | --- | --- |
| Modell | GPT-6.2-Sol (reasoning high) | Claude Opus 5.5 | DeepSeek Flash |
| Arbeitszeit | 22 min 14 s | 25 min 6 s | **15 min 56 s** |
| Kosten | 1,39 $ (API-Gegenwert) | 7,64 $ (API-Gegenwert) | **0,16 $ (echte Abrechnung)** |
| Kriterien erfüllt | 33 / 33 | 33 / 33 | 32 / 33 (1 teilweise) |
| Tests (gesamt) | 64 | **119** | 80 |
| Geänderte Zeilen | **+1.796 / −483** | +4.464 / −316 | +3.397 / −388 |
| Stärke | schlankster, robustester Code | gründlichste Umsetzung, bestes Frontend | schnellste und günstigste Umsetzung |
| Schwäche | nur 1 Commit, schlichtestes Frontend | teuerster Lauf, größter Code-Umfang | Kontrastmangel bei Eingabefeldern |

## 1. Rahmenbedingungen

- **Ausgangsstand:** `agentic-baseline` (Commit `edc1902`) mit 42 Tests, davon 39 bestanden und 3 übersprungen.
- **Durchführung:** Alle drei Läufe parallel in eigenen Git-Worktrees, jeweils in einer frischen Session mit identischem Start-Prompt.
- **Autonomie:** Claude Code lief durchgehend im „auto mode“, Hermes im Standardmodus. Codex startete im Modus „bei allem fragen“ und wurde nach den ersten Rückfragen auf „nur bei riskanten Aktionen fragen“ umgestellt; laut `/status` lief es danach als „Workspace (Approve for me)“.
- **Eingriffe** (eigene Nachrichten nach dem Start-Prompt): bei keinem Kandidaten.
- **Freigaben:** Codex etwa 5, alle zu Beginn und alle zur Frage, ob ein Befehl ausgeführt werden darf. Danach keine weiteren. Claude und Hermes: keine.
- **Hinweis zur Fairness:** Die Umstellung des Freigabemodus bei Codex ist streng genommen ein Eingriff in die Konfiguration, kein inhaltlicher. Die Wartezeit auf die ersten Freigaben steckt in der Arbeitszeit von Codex und benachteiligt es leicht.
- **Push:** nach Ende der Zeitmessung durch einen separaten Nachlauf-Prompt.

## 2. Messwerte

| Messgröße | Codex | Claude Code | Hermes + DeepSeek |
| --- | --- | --- | --- |
| Werkzeug | Codex CLI v0.171.0 | Claude Code | Hermes Agent v0.23.3+382.g52b5271 |
| Modell / Einstellung | gpt-6.2-sol, reasoning high | claude-opus-5-5 | deepseek-flash |
| Fertig um | 15:39 | 15:42 | 15:33 |
| Arbeitszeit laut Tool | 22:14 min | 25:06 min (API-Zeit 23:06 min) | 15:56 min |
| Input-Tokens | 168.416 | 214 | 105.107 |
| Cache-Read | 5.529.216 | 11,9 Mio. | (in Prompt enthalten, 99,3 % Cache-Trefferquote) |
| Cache-Write | – | 256,8k | – |
| Output-Tokens | 49.743 (davon 14.603 Reasoning) | 159,9k | 127.296 (davon 65.910 Reasoning) |
| Tokens gesamt | 5.747.375 | ca. 12,3 Mio. | 15.284.266 (Abrechnung; Session-Statistik: 14.543.187) |
| API-Aufrufe | – | 61 | 123 (Abrechnung; Session: 103) |
| Kosten | 1,39 $ | 7,64 $ | 0,16 $ |
| Kostenquelle | Schätzung ccusage | Schätzung Claude Code | DeepSeek-Abrechnung |
| Tatsächlich bezahlt | über ChatGPT-Plus-Abo | über Claude-Abo | 0,16 $ |
| Commits | **1** | 8 | 5 |
| Geänderte Dateien | 34 | 42 | 29 |
| Zeilen Java (main) | +543 / −178 | +1.190 / −59 | +674 / −105 |
| Zeilen Tests | +612 / −56 | +1.456 / −15 | +936 / −32 |
| Zeilen Templates/CSS/JS | +587 / −238 | +1.677 / −230 | +1.612 / −241 |
| Tests gesamt / bestanden / übersprungen | 64 / 61 / 3 | 119 / 116 / 3 | 80 / 77 / 3 |
| Neue Tests gegenüber Baseline | +22 | +77 | +38 |
| Neue Abhängigkeiten | keine | keine | keine |

Die Testzahlen stammen aus der eigenen Gegenprobe über IntelliJ und decken sich mit den Angaben in den `AGENT_REPORT.md`-Dateien.

Der Unterschied bei Hermes zwischen Session-Statistik und Abrechnung kommt vom Push-Nachlauf und von internen Hilfsaufrufen. Maßgeblich ist die Abrechnung.

## 3. Akzeptanzkriterien

Legende: ✅ erfüllt · 🟡 teilweise · ❌ nicht erfüllt. Geprüft wurde per Code-Review (stichprobenartig), über vorhandene Tests, eigene Testgegenprobe und Screenshots. Die Anwendung wurde nicht in jedem Detail manuell durchgeklickt.

| ID | Kriterium (kurz) | Codex | Claude | Hermes |
| --- | --- | --- | --- | --- |
| A1 | Ortsname gespeichert und angezeigt | ✅ | ✅ | ✅ |
| A2 | Altprofile ohne Namen nutzbar | ✅ | ✅ | ✅ |
| A3 | Namen eindeutig (trim, case-insensitiv) | ✅ | ✅ | ✅ |
| A4 | Duplikat abgelehnt, Auswahl bleibt | ✅ | ✅ | ✅ |
| A5 | Profil bearbeitbar | ✅ | ✅ | ✅ |
| A6 | gleiche Regeln beim Bearbeiten | ✅ | ✅ | ✅ |
| A7 | Erfolgsmeldung, Änderung übersteht Neustart | ✅ | ✅ | ✅ ¹ |
| A8 | unbekanntes Profil → verständliche Meldung | ✅ | ✅ | ✅ |
| B1 | Start- und Enddatum wählbar | ✅ | ✅ | ✅ |
| B2 | ohne Angabe nur heute | ✅ | ✅ | ✅ |
| B3 | ungültiger Zeitraum → Meldung, keine Abfrage | ✅ | ✅ | ✅ |
| B4 | alle Fenster mit Datum, Zeit, Dauer, sortiert | ✅ | ✅ | ✅ |
| B5 | Fenster über Mitternacht bleibt eins | ✅ | ✅ | ✅ |
| B6 | alter Parameter `searchDate` funktioniert | ✅ | ✅ | ✅ |
| B7 | Tests für Zeitraumlogik | ✅ | ✅ | ✅ |
| C1 | freundliche 4xx-Seite bei ungültigen Parametern | ✅ | ✅ | ✅ |
| C2 | gestaltete 404- und 500-Seite | ✅ | ✅ | ✅ |
| C3 | robuste Verarbeitung inkonsistenter Wetterdaten | ✅ | ✅ | ✅ |
| C4 | Ortswechsel behält Auswahl | ✅ | ✅ | ✅ |
| C5 | Tests für C1, C3, C4 | ✅ | ✅ | ✅ |
| D1 | gemeinsames Layout und Navigation | ✅ | ✅ | ✅ |
| D2 | ab 360 px ohne horizontales Scrollen | ✅ | ✅ | ✅ |
| D3 | Labels, Tastaturbedienung, Fokus | ✅ | ✅ | ✅ |
| D4 | WCAG-AA-Kontrast für Text und Bedienelemente | ✅ | ✅ | 🟡 ² |
| D5 | Kernfunktionen ohne JavaScript | ✅ | ✅ | ✅ |
| D6 | Erfolgs-, Fehler- und Leerzustände gestaltet | ✅ | ✅ | ✅ |
| D7 | Quellenangaben sichtbar | ✅ | ✅ | ✅ |
| D8 | Browser-Szenario angepasst | ✅ | ✅ | ✅ |
| G1 | `./mvnw test` grün | ✅ | ✅ | ✅ |
| G2 | neue Tests; Bestandstests nur bei Verhaltensänderung | ✅ | ✅ ³ | ✅ |
| G3 | README aktualisiert | ✅ | ✅ | ✅ |
| G4 | `AGENT_REPORT.md` vorhanden | ✅ | ✅ | ✅ |
| G5 | alles committet, nichts gepusht | ✅ ⁴ | ✅ | ✅ |
| **Summe** | | **33** | **33** | **32 + 1 🟡** |

¹ Hermes hat die Persistenz nach einem Neustart nicht automatisiert getestet. Codex und Claude haben dafür jeweils einen eigenen Test mit Dateidatenbank geschrieben.
² Die Rahmen der Eingabefelder haben auf Weiß nur einen Kontrast von 2,19:1, verlangt sind für Bedienelemente 3:1. Hermes hat laut Report nur Textkontraste berechnet und das Kriterium deshalb zu Unrecht als erfüllt gemeldet. Zum Vergleich: Codex 4,48:1, Claude 3,66:1 (eigene Nachrechnung).
³ Claude hat keine Bestandstests geändert. Stattdessen hat es alte Controller-Methoden als zusätzliche Überladungen behalten, damit die alten Tests weiter passen. Das erfüllt das Kriterium formal, erzeugt aber unnötigen Code (siehe Abschnitt 4).
⁴ Erfüllt, aber in nur einem Commit, obwohl `AGENTS.md` nachvollziehbare Schritte verlangt.

**Erkenntnis:** Die Akzeptanzkriterien waren für diese Modellgeneration zu grob, um die Kandidaten zu trennen. Fast alles war für alle erfüllbar. Für künftige Vergleiche brauchen sie entweder vorab geschriebene, für die Agenten unsichtbare Abnahmetests oder schwierigere Anforderungen.

## 4. Code-Qualität

Bewertung 1–5 (5 = sehr gut), auf Basis einer stichprobenartigen Review der zentralen Klassen und Templates.

| Kriterium | Codex | Claude | Hermes |
| --- | --- | --- | --- |
| Lesbarkeit | 5 | 4 | 4 |
| Passung zur bestehenden Architektur | 4 | 3 | 4 |
| Testqualität und -umfang | 3 | 5 | 4 |
| Keine unnötigen oder überraschenden Änderungen | 4 | 3 | 4 |
| Fehlerbehandlung | 4 | 5 | 4 |
| Nachvollziehbare Commits | 1 | 5 | 4 |

**Codex** schreibt mit Abstand am kompaktesten. Die Planungslogik im `MainpageController` ist von 121 auf 95 Zeilen geschrumpft, obwohl sie jetzt Zeiträume beherrscht, und die Eingaben sind sauber in `PlanningSelection` und `ProfileForm` gebündelt. Als einziger Kandidat sichert Codex die Namenseindeutigkeit auch in der Datenbank ab, über eine normalisierte Spalte `name_key` mit Unique-Constraint und pessimistischem Lock beim Bearbeiten. Damit ist die Regel auch bei parallelen Anfragen garantiert. Alte Duplikate benennt eine Start-Routine automatisch um, etwa zu „Profil (2)“. Das ist technisch elegant, verändert aber ungefragt Nutzerdaten, ist immerhin dokumentiert. Die Kehrseiten: die wenigsten neuen Tests, alles in einem Commit, und das Speichern eines Profils braucht jetzt die Orts-API.

**Claude** liefert die gründlichste Umsetzung: 77 neue Tests, darunter echte Migrations- und Neustarttests sowie parametrisierte Fehlerfälle. Dazu kommen saubere Commits nach Paketen, unterschiedliche Statuscodes (400, 404, 503) und ausdrücklich abgeschaltete technische Fehlerdetails. Der Preis ist der größte Code-Umfang mit +1.190 Zeilen Java und neuen Klassen wie `ResultView`, `SearchPeriod` und einem separaten `SearchProfileEditController`. Fragwürdig ist die Entscheidung, alte Methodensignaturen als Überladungen zu behalten, nur um keine Tests anpassen zu müssen. `TASK.md` hätte das Anpassen ausdrücklich erlaubt. Die Eindeutigkeit prüft Claude nur in der Anwendung und benennt die Race Condition offen als Restrisiko.

**Hermes** liefert eine solide Mitte. Gut gelöst sind die injizierbare Uhr (`Clock`) für deterministische Tests und der globale Binder für leere Datumsfelder. Die Eindeutigkeit prüft auch Hermes nur in der Anwendung, mit der gleichen Begründung wie Claude. Der `SearchProfileController` ist mit rund 300 Zeilen allerdings recht groß geworden. Der Agent räumt selbst ein, die Gestaltung nie visuell geprüft zu haben, und genau dort liegen seine Schwächen (siehe Abschnitt 5).

**Ehrlichkeit der Reports:** Alle drei Reports sind detailliert und bei den Testzahlen korrekt. Hermes überschätzt D4. Codex schreibt, Alt-Links und neue Zeitraumbedingungen seien „automatisiert geprüft“, und das stimmt. Claude und Hermes benennen ihre Restrisiken am offensten.

## 5. Frontend

Screenshots: [`screenshots/`](screenshots/), direkte Gegenüberstellungen in [`screenshots/vergleich/`](screenshots/vergleich/). Die Desktop-Aufnahmen (1280 px) wurden mit identischen Eingaben gemacht: Berlin, 21.–22.11., Temperatur egal, mäßiger Wind, leichter Regen. Die Mobil-Aufnahmen (360 px) zeigen bei allen drei den Leerzustand, weil die Eingaben dort nicht identisch bzw. nicht erneut abgeschickt waren. Für D6 ist das sogar nützlich.

Bewertung 1–5:

| Kriterium | Codex | Claude | Hermes |
| --- | --- | --- | --- |
| Gesamteindruck | 3 | 5 | 4 |
| Verständlichkeit des Formulars | 4 | 4 | 4 |
| Darstellung der Ergebnisse | 3 | 5 | 3 |
| Mobilansicht | 4 | 4 | 4 |
| Konsistenz über alle Seiten | 4 | 5 | 4 |

**Codex:** Ein ruhiges Grün mit sehr großer Überschrift und zweispaltigem Desktop-Layout (Formular links, Ergebnisse rechts). Funktional und konsistent, aber das schlichteste der drei Frontends, mit nur rund 210 Zeilen CSS. Die Ergebnisse sind reine Textkarten. Die Dauer erscheint als „1860 Min.“, das Ende als „bis 23.11.2026, 00:00“. Das ist korrekt, aber unhandlich zu lesen.

**Claude:** Das ausgearbeitetste Frontend, mit Blau als Aktionsfarbe und Grün für passende Fenster. Über den Ergebnissen stehen drei Kennzahlen (Anzahl, längstes Fenster, insgesamt), darunter eine Zeitleiste über den ganzen Zeitraum mit schraffierten vergangenen Stunden. Fenster über Mitternacht bekommen ein Badge („über Mitternacht“), die Dauer steht in Stunden, und das Ende wird als „24:00“ angezeigt. Der Leerzustand gibt konkrete Tipps. Die Dropdowns zeigen die Grenzwerte gleich mit an, etwa „bis 30 km/h“. Kleine Schwächen: Diese langen Optionstexte werden bei 360 px abgeschnitten, und die zweigeteilte Eingabe mit Ortssuche und Planung ist etwas komplexer.

**Farbwelten im Vergleich** (aus den CSS-Variablen der Branches):

| | Hintergrund | Hauptfarbe für Aktionen | Wirkung |
| --- | --- | --- | --- |
| Codex | `#f4f7f2`, fast weiß mit Hauch Grün | `#185740`, dunkles Waldgrün | ruhig, grün |
| Claude | `#f3f6fa`, fast weiß mit kühlem Blaugrau | `#1a56a8`, kräftiges Blau; Grün nur für passende Fenster | frisch, kühl |
| Hermes | `#f5f4ef`, warmes Off-White/Beige | `#0f5f57`, Petrol (Blaugrün) | warm, grün |

**Eigene Einschätzung:** Codex und Hermes sehen sich auffallend ähnlich: heller, leicht getönter Hintergrund, weiße Karten mit runden Ecken, abgerundete Buttons und ein dunkles Grün bzw. Petrol als Akzent. Das entspricht dem Look, den KI-Modelle seit einiger Zeit standardmäßig erzeugen, wenn man ihnen keine Gestaltungsvorgaben macht. Weil wir bewusst keine Design-Skills oder Stilvorgaben mitgegeben haben, ist das wenig überraschend. Claude wirkt durch die kühle Blau-Palette frischer und hebt sich ab. Bei Aufbau, Kartenform, Buttons und Positionierung sind sich aber auch hier alle drei sehr ähnlich. Den deutlichsten Unterschied machen bei Claude die zusätzlichen Elemente: die Zeitleiste, die Kennzahlen und die kleinen Symbole, etwa die Regenwolke im Leerzustand. Bei Hermes fällt sofort negativ auf, dass die passenden Zeitfenster ganz unten unter dem Formular stehen.

**Hermes:** Eine warme, hochwertig wirkende Optik mit Petrol-Akzent, Pill-Navigation, Breadcrumbs und Fieldsets. Die persönlichere Überschrift („Wann passt es für Spazieren gehen?“) ist ein netter Einfall. Auf dem Desktop stehen die Ergebnisse allerdings unter dem langen Formular und sind erst nach Scrollen sichtbar. Auf der Bearbeiten-Seite sind die Auswahlfelder für Wind und Niederschlag bei 1280 px zu schmal, ihr Text wird abgeschnitten („Höchstens mäßiger W…“). Dazu kommt der zu schwache Kontrast der Feldrahmen. Diese Fehler hätte eine visuelle Kontrolle sofort gezeigt.

## 6. Kosten-Nutzen

| | Codex | Claude | Hermes |
| --- | --- | --- | --- |
| Kosten pro erfülltem Kriterium | 0,04 $ | 0,23 $ | 0,005 $ |
| Kosten pro neuem Test | 0,06 $ | 0,10 $ | 0,004 $ |
| Tokens pro Minute Arbeitszeit | ca. 258k | ca. 490k | ca. 959k |

Hermes mit DeepSeek Flash hat die meisten Tokens verbraucht und trotzdem am wenigsten gekostet. Das liegt an niedrigen Grundpreisen und einer Cache-Trefferquote von 99,3 %. Die Gesamtzahl der Tokens sagt also wenig über die Kosten aus. Entscheidend sind Modellpreis und Caching. Claude war etwa 48-mal so teuer wie Hermes und etwa 5,5-mal so teuer wie Codex. Dafür gab es das beste Frontend und die meisten Tests.

## 7. Fazit

- **Codex** ist die Wahl, wenn schlanker, wartbarer Code und Robustheit wichtiger sind als Umfang und Optik. Für den Lernprozess anderer ist der eine Riesen-Commit allerdings ein klarer Nachteil.
- **Claude Code mit Opus** liefert das vollständigste, am besten getestete Ergebnis mit dem stärksten Frontend. Es neigt aber zu mehr Code als nötig und ist der teuerste Lauf.
- **Hermes mit DeepSeek Flash** ist die größte Überraschung: fast gleicher Funktionsumfang in der kürzesten Zeit, für 16 Cent. Die Schwächen liegen genau dort, wo man ohne visuelle Kontrolle hinschauen müsste, nämlich bei der Gestaltung.

Hätte ich einen Branch auszuwählen, würde ich `agent/claude-opus` übernehmen, wegen Testabdeckung und Frontend. Die Datenbank-Absicherung der Namenseindeutigkeit von Codex würde ich mitnehmen. Diese Einschätzung hat aber eine Befangenheit (siehe unten).

## 8. Grenzen der Aussagekraft

- **Je ein Durchlauf pro Kandidat.** Unterschiede können auch Zufall sein. Eine Wiederholung würde Streuung sichtbar machen.
- **Verglichen werden Werkzeug und Modell zusammen**, nicht das Modell allein. Die Modelle gehören außerdem verschiedenen Preisklassen an (Flash als günstiges Modell gegen zwei Premium-Modelle).
- **Codex hatte zusätzlichen Kontext:** Es hat neben der Projekt-`AGENTS.md` eine globale `~/.codex/AGENTS.md` aus früheren Projekten geladen. Sie enthält allgemeine Arbeitsregeln: auf Deutsch antworten und Begriffe anfängergerecht erklären, vor Änderungen Kontext lesen, Fehlerursachen belegen, nur das Beauftragte ändern und *kleine, nachvollziehbare Änderungen* bevorzugen, Tests ausführen, Risiken nennen, vor wesentlicher Auftragserweiterung nachfragen. Das meiste deckt sich mit der Projekt-`AGENTS.md`. Zwei Regeln könnten das Ergebnis aber beeinflusst haben: „nur das Beauftragte ändern“ und „kleine Änderungen bevorzugen“ passen auffällig gut zum schlanksten Code-Umfang und zum schlichtesten Frontend von Codex. Ein ursächlicher Zusammenhang lässt sich aus einem einzigen Lauf nicht belegen. Für eine faire Wiederholung sollte die Datei während des Laufs umbenannt werden. Kurios: Trotz der Regel zu nachvollziehbaren Änderungen hat Codex alles in einem einzigen Commit abgelegt.
- **Parallele Ausführung** auf einem Rechner. Die Arbeitszeiten sind deshalb untereinander vergleichbar, aber nicht absolut.
- **Kostenangaben** für Codex und Claude sind hochgerechnete API-Preise, für DeepSeek ist es die echte Abrechnung.
- **Befangenheit der Auswertung:** Die Bewertung wurde mit Unterstützung von Claude erstellt, also einem Modell aus derselben Familie wie einer der Kandidaten. Die messbaren Teile (Tests, Zeiten, Kosten, Kontraste, Zeilen) sind davon unberührt. Die Noten in den Abschnitten 4 und 5 sollten aber von einer unabhängigen Person oder einem anderen Modell gegengelesen werden.
- **Die Code-Review war stichprobenartig**, nicht vollständig.

## 9. Lessons Learned

1. **Git-Worktrees** eignen sich hervorragend für parallele Agentenläufe auf demselben Ausgangsstand. Ein eigener Baseline-Branch mit Tag macht den Vergleich nachvollziehbar.
2. **Eine gemeinsame `AGENTS.md`**, plus `CLAUDE.md` als Verweis darauf, sorgt dafür, dass alle Agenten denselben Kontext haben.
3. **Akzeptanzkriterien allein trennen moderne Agenten kaum.** Versteckte Abnahmetests und eine visuelle Prüfung sind aussagekräftiger.
4. **Billig ist nicht schlecht:** Das günstigste Modell lieferte fast denselben Funktionsumfang. Die Unterschiede lagen in Feinschliff und Tiefe.
5. **Token-Messung ist bei jedem Werkzeug anders:** `/status`, `/usage`, `ccusage`, Anbieter-Dashboard. Die Quelle sollte man immer mit dokumentieren.
6. **Ohne Gestaltungsvorgaben entsteht ein generischer KI-Look.** Zwei von drei Frontends sind kaum zu unterscheiden. Wer ein eigenständiges Design will, muss es vorgeben, etwa über einen Design-Skill oder ein Designsystem.
7. **Setup-Stolpersteine:** zsh interpretiert `#`-Kommentare im Terminal nicht, und ein Befehl, der stillschweigend fehlschlägt, kann trotzdem einen falschen Tag erzeugen. Deshalb nach jedem Schritt kontrollieren.

## 10. Mögliche Runde 2

Zwei Erweiterungen bieten sich an, die direkt an die Schwächen aus Runde 1 anknüpfen:

- **Design-Skill gegen den Einheitslook:** Allen drei Agenten denselben Skill mit Gestaltungsrichtlinien mitgeben und prüfen, ob die Frontends dadurch eigenständiger werden und sich stärker unterscheiden.
- **Visuelle Selbstkontrolle über einen Browser-MCP-Server:** Besonders für Hermes interessant. Prüfen, ob abgeschnittene Auswahlfelder, die Position der Ergebnisse und der Kontrast (D4) dann selbst erkannt und behoben werden, und wie sich Zeit und Kosten dabei verändern.

---

# Runde 2: Design-Skill und visuelle Selbstkontrolle

Durchgeführt am 21.11.2026, Aufbau in [`RUNDE-2.md`](RUNDE-2.md). Jeder Agent hat auf seinem eigenen Ergebnis aus Runde 1 nur das Frontend neu gestaltet. Alle drei hatten denselben Design-Skill (`.agents/skills/wpe-design/SKILL.md`), dieselbe Aufgabe (`TASK-R2.md`), denselben Prompt und dasselbe Playwright-MCP. Die globale Codex-`AGENTS.md` war diesmal ausgeblendet. Screenshots vorher und nachher, mit identischen Eingaben automatisch erzeugt: [`screenshots/r1/`](screenshots/r1/) und [`screenshots/r2/`](screenshots/r2/), jeweils mit `index.html`. Gegenüberstellungen liegen in [`screenshots/vergleich-r2/`](screenshots/vergleich-r2/).

## R2.1 Messwerte

| Messgröße | Codex | Claude Code | Hermes + DeepSeek |
| --- | --- | --- | --- |
| Modell | gpt-6.2-sol, reasoning high | claude-opus-5-5 | deepseek-flash |
| Arbeitszeit | 24:00 min (R1: 22:14) | 19:02 min (R1: 25:06) | 15:20 min (R1: 15:56) |
| Input-Tokens | 124.737 | 138 | 141.166 |
| Cache-Read / Cache-Write | 4.391.168 / – | 11,9 Mio. / 226,1k | (im Prompt enthalten, 19.036.142 Prompt-Tokens) |
| Output-Tokens (davon Reasoning) | 27.523 (6.943) | 101,3k | 113.474 (53.621) |
| Tokens gesamt | 4.543.428 | ca. 12,2 Mio. | 19.149.616 |
| API-Aufrufe | – | 67 | 124 |
| Kosten | 0,96 $ (Schätzung ccusage; R1: 1,39 $) | 6,22 $ (Schätzung Claude Code; R1: 7,64 $) | 0,16 $ (echte Abrechnung; R1: 0,16 $) |
| Eingriffe / Freigaben | 0 / 0 | 0 / 0 | 0 / 0 |
| Commits (ohne Aufgaben-Commit) | 3 (R1: 1) | 4 | 6 |
| Geänderte Zeilen | +657 / −247 | +1.367 / −686 | +1.555 / −847 |
| Tests laut Report | 64 (61 bestanden, 3 übersprungen) | 122 mit aktiviertem Browser-Test (2 übersprungen) | 80 (77 bestanden, 3 übersprungen) |

Hinweis: Bei Claude zeigte `/usage`, dass der Skill `wpe-design` 41 % des Verbrauchs ausmachte. Das 5-Stunden-Kontingent des Abos stand nach dem Lauf bei 96 %. Die Testzahlen der Runde 2 sind Selbstauskünfte und nicht unabhängig gegengeprüft.

## R2.2 Das Playwright-MCP ist bei allen drei nicht gestartet

Der MCP-Server erwartet standardmäßig Google Chrome. Auf dem Rechner war nur das Chromium von Playwright installiert. Das ist ein Fehler im Versuchsaufbau: Beim Eintragen hätte `--browser chromium` mitgegeben werden müssen.

Bemerkenswert ist, dass **alle drei Agenten das Problem selbstständig erkannt und umgangen haben**:

- **Codex** hat ein eigenes Playwright-MCP mit dem installierten Chromium gestartet und darüber Screenshots und DOM-Messungen erzeugt.
- **Claude** hat die Playwright-Bibliothek in einem temporären Ordner installiert (nicht im Projekt), damit Screenshots aufgenommen und sie sich angesehen.
- **Hermes** hat die Playwright-Node-API genutzt. Weil `deepseek-flash` vermutlich keine Bilder verarbeiten kann, hat es die Screenshots zusätzlich in eine **Helligkeitskarte als Text** umgerechnet, um Lage und Größe von Elementen „sehen“ zu können. Es räumt offen ein, dass feine ästhetische Fragen damit nur eingeschränkt beurteilbar sind.

Gemessen wurde damit weniger die MCP-Nutzung als die Fähigkeit zur **visuellen Selbstkontrolle**. Die hatten am Ende alle drei.

## R2.3 Kriterien R1–R12

| ID | Kriterium | Codex | Claude | Hermes |
| --- | --- | --- | --- | --- |
| R1 | `DESIGN.md` vollständig, mit Selbstprüfung | ✅ | ✅ | ✅ |
| R2 | Schriften nach Vorgabe | ✅ | ✅ | ✅ |
| R3 | Hintergrund reines Weiß | ✅ | ✅ | ✅ |
| R4 | eine Akzentfarbe, außerhalb 90–200°, ≥ 40° Abstand | ✅ Kobaltblau `#243ca6` (229°, 71° Abstand) | ✅ gebranntes Orange `#b54708` (22°, 167° Abstand) | ✅ gebranntes Orange `#c2410c` (17°, 157° Abstand) |
| R5 | keine verbotenen Muster | ✅ | ✅ | ✅ |
| R6 | erstes Ergebnis bei 1280 × 800 ohne Scrollen sichtbar | ✅ | ✅ | ✅ (R1: ❌) |
| R7 | kein horizontales Scrollen, kein abgeschnittener Text | ✅ | ✅ | ✅ (R1: abgeschnittene Felder) |
| R8 | Kontraste inkl. Bedienelemente ≥ 3:1 | ✅ | ✅ | ✅ (R1: 2,19:1, jetzt ca. 3,9:1) |
| R9 | visuelle Selbstkontrolle dokumentiert | ✅ | ✅ | ✅ |
| R10 | Runde-1-Kriterien weiter erfüllt, Tests grün | ✅ laut Report | ✅ laut Report | ✅ laut Report |
| R11 | `AGENT_REPORT_R2.md` vollständig | ✅ | ✅ | ✅ |
| R12 | nachvollziehbare Commits, nicht gepusht | ✅ | ✅ | ✅ |

Geprüft: R2–R5 im CSS der Branches (Eckenradien, Verläufe, Versalien, Schriftfamilien, Mittelpunkte und Pfeile in den Templates), R4 und die Hauptkontraste nachgerechnet, R6 und R7 über die automatischen Screenshots bei 1280 × 800 und die Überlauf-Messung des Skripts (0 von 42 Seiten mit horizontalem Scrollen). R10 beruht auf den Reports.

## R2.4 Ergebnis der Gestaltung

**Der generische Look ist bei allen drei verschwunden.** Statt getöntem Hintergrund mit weißen Karten und Grün haben jetzt alle reines Weiß, Serifen-Überschriften, Linien statt Karten und eine eigene Akzentfarbe. Die Ergebnisse stehen überall ganz oben bzw. neben dem Formular.

- **Codex:** Leitidee „Stundenplan fürs Draußensein“. Ein kräftig kobaltblaues Ergebnisfeld zeigt Beginn und Ende sehr groß. Die Dauer wird weiterhin in Minuten angegeben („1740 Min.“). Mobil stehen die Ergebnisse vor dem Formular. Das Design ist das schlichteste, aber jetzt klar eigenständig. Nebenbei: Codex hat diesmal in drei nachvollziehbaren Commits gearbeitet statt in einem.
- **Claude:** Leitidee „dunkle Wand aus Stunden, in der Fenster aufgehen“. Ein dunkles Zeitband mit orange leuchtenden Fenstern, Kennzahlen und eine Formularspalte rechts mit Radiobuttons samt Grenzwerten („bis 30 km/h“). Das ist das ausgearbeitetste Ergebnis, mobil allerdings eine lange Seite. Selbst gefunden und behoben hat Claude unter anderem eine abgeschnittene Auswahlliste, ein umbrechendes Mitternachtsfenster und fehlende Fokusringe an Datumsfeldern.
- **Hermes:** Leitidee „Nachtfenster“. Ein dunkles Ergebnisfeld mit 24-Stunden-Band, in dem das Fenster als oranger Balken sitzt. **Alle drei Mängel aus Runde 1 sind behoben**: Ergebnisse sichtbar ohne Scrollen, keine abgeschnittenen Auswahlfelder, ausreichender Rahmenkontrast. Schwäche: Das Band zeigt nur einen Tag, ein Fenster über Mitternacht ist dort nur bis 24 Uhr zu sehen.

**Unerwartet: Claude und Hermes sind konzeptionell zusammengelaufen.** Beide haben unabhängig voneinander ein dunkles Zeitband mit warm-orangen „Fenstern“ und fast dieselbe Akzentfarbe gewählt (`#b54708` und `#c2410c`). Wahrscheinlich hat die Formulierung im Skill das gefördert: „Zeit lässt sich räumlich darstellen“, „das richtige Zeitfenster finden“. Claude hatte außerdem schon in Runde 1 eine Zeitleiste. Codex hat als einziger eine andere Richtung gewählt. **Ein Skill verhindert also den Standard-Look, erzeugt aber eine neue, skill-bedingte Ähnlichkeit.** Wer verschiedene Ergebnisse will, sollte im Skill keine Bilder oder Metaphern vorgeben.

Bewertung durch Claude, 1–5 (Runde 1 in Klammern):

| Kriterium | Codex | Claude | Hermes |
| --- | --- | --- | --- |
| Gesamteindruck | 4 (3) | 5 (5) | 4 (4) |
| Eigenständigkeit gegenüber Standard-KI-Look | 4 (2) | 5 (3) | 4 (2) |
| Darstellung der Ergebnisse | 4 (3) | 5 (5) | 4 (3) |
| Mobilansicht | 4 (4) | 4 (4) | 4 (4) |
| Konsistenz über alle Seiten | 4 (4) | 5 (5) | 4 (4) |

Diese Noten sind wie in Runde 1 subjektiv und von einem Modell aus derselben Familie wie einer der Kandidaten vergeben (siehe Abschnitt 8).

### Einschätzung des Nutzers (Gegenprobe)

Beim eigenen Durchklicken aller drei Anwendungen fiel das Urteil deutlich schlechter aus als die Noten oben: **Keine der drei Versionen wirkt stimmig, alle sehen „out of place“ aus, und Runde 1 sah insgesamt besser aus.** Die Aufgabe wurde formal erfüllt, das Ergebnis überzeugt optisch aber nicht. Im Einzelnen:

- **Für alle drei:** Der reine weiße Hintergrund sticht stark ins Auge und macht alle Kontraste härter. Es fehlt ein Gleichgewicht zwischen der Darstellung der Ergebnisse und den Auswahlmöglichkeiten. Die Suchprofil-Seiten sind dagegen bei allen drei gut gelungen.
- **Codex:** Das sehr kräftige Blau springt sofort ins Auge. Zusammen mit dem eckigen Ergebniskasten wirkt der Block deplatziert, als gehöre er nicht auf die Seite. Es gibt kein Gleichgewicht.
- **Claude:** Das Design war schon in Runde 1 gut. Jetzt steht der Zeitstrahl stärker im Fokus, und es gibt Links zum Anpassen der Suche (Zeitraum verlängern, Mindestdauer verkürzen, Wetterwünsche lockern). Die Auswahl von Temperatur, Wind und Niederschlag über Optionsfelder statt Dropdowns ist sehr gut gelöst, weil man mit einem Klick wählt. Insgesamt ist der Kontrast aber auch hier zu hoch, und Ergebnisdarstellung und Auswahl stehen nicht im Gleichgewicht.
- **Hermes:** Dasselbe Problem wie bei Codex. Das dunkle Ergebnisfeld ist viel zu groß und zu kontrastreich und wirkt dadurch fremd auf der Seite. Die Linien neben den Überschriften „Zeitraum“, „Aktivität“ und „Wetterwünsche“ wirken merkwürdig.

**Ursache, soweit erkennbar:** Der Design-Skill hat überwiegend festgelegt, was die Agenten **nicht** tun dürfen: keinen getönten Hintergrund, keine Karten, kein Grün, keine Verläufe, eine Akzentfarbe mit großem Abstand zur alten. Dazu kamen Pflichtvorgaben wie reines Weiß und Serifenüberschriften. Die Agenten haben diese Regeln vollständig erfüllt, aber „nicht generisch“ ist nicht dasselbe wie „schön“. Herausgekommen sind regelkonforme Einzelentscheidungen, die nicht zusammenpassen: harter Weißgrund, ein einzelnes sehr kräftiges Element (blauer Kasten bzw. dunkles Band) und kein ausgleichendes Gesamtbild. Der Skill hat also den Einheitslook verhindert, aber kein harmonisches Design hervorgebracht. Die objektiv prüfbaren Kriterien R1–R12 konnten das nicht erfassen, weil sie ebenfalls fast nur Regeln abfragen und kein Gesamturteil.

**Artefakte in den Screenshots:** Die roten Meldungen „Profilname gibt es bereits“ auf den Planungsseiten stammen vom Screenshot-Skript, das bei jedem Lauf erneut ein „Testprofil“ angelegt hat. Das ist kein Fehler der Agenten, zeigt aber nebenbei, dass alle drei Duplikate sauber abfangen. Das Skript ist inzwischen korrigiert. In der 360-px-Aufnahme von Codex zeigt das Formular außerdem andere Wetterwünsche als die tatsächlich abgefragten Ergebnisse. Ob das ein Darstellungsfehler oder ebenfalls ein Skript-Artefakt ist, ist ungeklärt und sollte manuell geprüft werden.

## R2.5 Fazit Runde 2

1. **Ein Design-Skill wirkt stark, aber nicht automatisch gut.** Alle festen Vorgaben wurden von allen drei eingehalten, und der Einheitslook war weg. Optisch waren die Ergebnisse nach Urteil des Nutzers aber schlechter als in Runde 1.
2. **Verbote erzeugen kein schönes Design.** Ein Skill, der vor allem sagt, was nicht erlaubt ist, führt zu regelkonformen, aber unstimmigen Ergebnissen. Ein Skill sollte positiv beschreiben, wie das Ziel aussieht, idealerweise anhand konkreter Vorbilder, und das Gleichgewicht der Seite als eigenes Kriterium enthalten.
3. **Visuelle Selbstkontrolle behebt konkrete Fehler.** Am deutlichsten bei Hermes: Alle in Runde 1 gefundenen konkreten Gestaltungsfehler sind behoben. Für ein stimmiges Gesamtbild reicht das allein aber nicht.
4. **Robuste Agenten umgehen Werkzeugprobleme selbst.** Das fehlerhaft eingerichtete MCP hat keinen Lauf verhindert.
5. **Skills können Ergebnisse angleichen.** Bildhafte Formulierungen im Skill werden offenbar wörtlich genommen.
6. **Kosten blieben stabil:** Hermes erneut 0,16 $, Codex rund 1 $, Claude rund 6 $. Hermes war erneut der schnellste Lauf.

## R2.6 Lessons Learned für den Aufbau

- Design-Vorgaben positiv formulieren (Zielbild, Vorbilder, Palette, Stimmung) statt überwiegend Verbote aufzuzählen, und ein Gesamturteil eines Menschen in die Abnahme aufnehmen. Messbare Regeln allein sagen nichts darüber, ob eine Oberfläche gefällt.
- Playwright-MCP immer mit `--browser chromium` eintragen oder Google Chrome installieren, und vorher mit einem echten Werkzeugaufruf testen, nicht nur mit der Werkzeugliste.
- Das Screenshot-Skript darf keine Zustände (wie doppelte Profile) erzeugen, die auf den Bildern sichtbar werden.
- Laufende Anwendungen der Agenten nach dem Lauf beenden, sonst blockiert die H2-Datei den nächsten Start.

---

# Runde 2b: Community-Skill und positives Zielbild

Durchgeführt am 22.11.2026, Aufbau und alle Messwerte in [`RUNDE-2B.md`](RUNDE-2B.md). Anlass war das Ergebnis von Runde 2: Der Verbots-Skill hatte regelkonforme, aber unstimmige Oberflächen erzeugt. In Runde 2b starteten alle drei Agenten wieder **vom Stand aus Runde 1** und bekamen den erprobten Community-Skill `interface-design` (MIT, unverändert) sowie ein gemeinsames, positiv beschriebenes Designsystem `.interface-design/system.md` in der Richtung „Himmel & Schiefer“: exakte Palette, Systemschrift mit Größen, Abstandsskala, zwei Radien, Seitenaufbau, Darstellung der Zeitfenster und eine Prüffrage zum Gleichgewicht. Die Richtung stammt aus einer ChatGPT-Recherche und wurde vom Nutzer ausgewählt. Das Playwright-MCP war diesmal korrekt eingerichtet (`--browser chromium`) und vor dem Lauf mit einem echten Aufruf getestet. Screenshots: [`screenshots/r2b/`](screenshots/r2b/), Gegenüberstellungen in [`screenshots/vergleich-r2b/`](screenshots/vergleich-r2b/).

## R2b.1 Messwerte (Kurzfassung)

| | Codex | Claude Code | Hermes + DeepSeek |
| --- | --- | --- | --- |
| Arbeitszeit | 31:00 min | 17:08 min | ca. 31 min |
| Tokens gesamt | 6,78 Mio. | ca. 17,3 Mio. | 74,3 Mio. |
| Kosten | 1,35 $ | 7,39 $ | 0,39 $ |
| MCP genutzt | ja | ja | ja |
| Geänderte Zeilen | +757 / −261 | +1.789 / −1.001 | +2.150 / −686 |

Hermes hat gegenüber Runde 2 fast viermal so viele Tokens verbraucht (74 statt 19 Mio.) und kostete trotzdem nur 0,39 $, rund ein Neunzehntel von Claude. Claude war mit 17 Minuten der schnellste Lauf.

## R2b.2 Prüfung der Vorgaben

Im CSS aller drei Branches stehen alle neun Farbwerte aus `system.md` als Variablen, es gibt genau zwei Eckenradien (bei Hermes zusätzlich 50 % für runde Punkte), die vorgegebene Systemschrift und keine Verläufe. Die automatische Messung des Screenshot-Skripts ergab auf 0 von 42 Seiten horizontales Scrollen. Alle drei Agenten melden Z1–Z12 als erfüllt und haben alle sieben Seiten bei 360 und 1280 px über das Playwright-MCP geprüft und dabei selbst gefundene Probleme behoben.

Unterschiede in der Arbeitsweise:

- **Codex** arbeitete mit simulierten Orts- und Wetterdaten und einer Datenbank im Speicher. Das ist reproduzierbar und lässt keine Testdaten zurück.
- **Claude** prüfte mit echten Wetterdaten und ergänzte eine automatische Prüfung auf Überlauf und abgeschnittenen Text. Es nennt offen drei Abweichungen: eine minimale Controller-Anpassung, damit die Wetterdetails Daten bekommen, eine Tab-Reihenfolge, die am Desktop zuerst zu den Ergebnissen springt, und einen Eckenradius von 0 an den Mitternachtskanten.
- **Hermes** kann mit `deepseek-flash` keine Bilder ansehen. Es hat die Screenshots deshalb wie in Runde 2 pixelweise ausgewertet (Flächenfarben, Textzeilen, Bandpositionen) und mit DOM-Messungen abgeglichen. Nach dem Abschluss meldete Hermes zusätzlich eine „Bildprüfung“, die verspätet zurückkam, und ergänzte daraufhin seinen Bericht. **Die Hermes-Logs des Laufs belegen, dass dabei kein anderes Modell im Spiel war:** Alle Hilfsaufrufe liefen über `deepseek-flash` direkt bei DeepSeek, darunter 41 Freigabeprüfungen für Befehle („Auxiliary approval“) und zwei Aufrufe des Bildwerkzeugs („Vision auto-detect: using main provider deepseek (deepseek-flash)“). Hermes hatte also kein zusätzliches Bildmodell, und alle Kosten stecken in den 0,39 $ der DeepSeek-Abrechnung. Wie viel `deepseek-flash` bei diesen zwei Bildaufrufen tatsächlich erkennen konnte, ist offen; laut Bericht beruhte die Prüfung im Wesentlichen auf der Pixelauswertung.

## R2b.3 Bewertung durch den Nutzer

| Kriterium (1–5) | Codex | Claude | Hermes |
| --- | --- | --- | --- |
| Gesamteindruck | 2,8 | 3,4 | **3,7** |
| Gleichgewicht | 3,6 | 4 | 4 |
| Ergebnisse schnell erfassbar | 3 | **4,5** | 4 |
| Formular angenehm bedienbar | 4 | 4 | 4 |
| Mobilansicht | 4,2 | 4 | **4,5** |
| Passt zum Zielbild | 4 | 4 | 4 |
| Schöner als Runde 1? | ja | ja | ja |
| Mittelwert | 3,6 | 4,0 | 4,0 |

**Alle drei Versionen sind nach Urteil des Nutzers schöner als Runde 1**, in Runde 2 war es keine. Hermes hat den besten Gesamteindruck und die beste Mobilansicht, Claude die am schnellsten erfassbaren Ergebnisse. Codex liegt beim Gesamteindruck und bei der Ergebnisdarstellung zurück.

Freitext des Nutzers (sinngemäß): Die Anwendung hat wenig Funktion und damit wenig darzustellen, nur eine Eingabe und eine Ausgabe. Die Gestaltungsmöglichkeiten sind dadurch begrenzt, im Grunde geht es um ein Farbdesign, und das wurde jetzt getroffen. Die Eingabe ist einfach durchzuführen, ansonsten gibt es nichts mehr auszusetzen.

## R2b.4 Beobachtungen

**Die drei Ergebnisse sehen sich jetzt sehr ähnlich.** Gleiche Farben, gleiche Schrift, gleicher Aufbau mit Suchspalte links und Tageszeilen auf einer 0–24-Uhr-Skala rechts. Das ist gewollt: Ein konkretes Designsystem legt fast alles fest, die Agenten setzen es nur noch um. Unterschiede zeigen sich in Details: wie die Fenster beschriftet sind, wie die Wetterwünsche angeordnet sind (bei Codex als umrandete Kacheln, bei Claude und Hermes als ruhige Optionsliste) und wie ausführlich die Zusatzangaben ausfallen. Bei einer so kleinen Anwendung mit nur Eingabe und Ausgabe bleibt, wie der Nutzer anmerkt, ohnehin vor allem das Farbdesign als Gestaltungsspielraum.

**Vergleich der drei Design-Ansätze:**

| | Runde 1 | Runde 2 | Runde 2b |
| --- | --- | --- | --- |
| Vorgabe | keine | Verbots-Skill (eigener) | Community-Skill und positives Zielbild |
| Ergebnis | generischer KI-Look, Codex und Hermes fast gleich | eigenständig, aber unstimmig; Claude und Hermes konzeptionell gleich | stimmig, alle drei sehr ähnlich |
| Urteil des Nutzers | Ausgangspunkt | schlechter als Runde 1 | alle drei schöner als Runde 1 |

## R2b.5 Fazit Runde 2b

1. **Ein positives Zielbild wirkt, Verbote nicht.** Mit konkreter Palette, Maßen und Aufbau wurden alle drei Oberflächen stimmig und nach Urteil des Nutzers schöner als in Runde 1.
2. **Je genauer die Vorgabe, desto austauschbarer die Agenten beim Design.** Die gestalterische Leistung lag hier zum großen Teil im Designsystem, nicht bei den Agenten. Unterschiede blieben in Details, Gründlichkeit und Dokumentation.
3. **Der Mensch bleibt Teil der Abnahme.** Erst das Gesamturteil des Nutzers hat in Runde 2 das Problem sichtbar gemacht und in Runde 2b die Verbesserung bestätigt. Die messbaren Kriterien waren in beiden Runden erfüllt.
4. **Werkzeuge vor dem Lauf echt testen.** Mit korrekt eingerichtetem und vorab erprobtem MCP haben alle drei das vorgesehene Werkzeug benutzt.
5. **Kosten:** Hermes 0,39 $, Codex 1,35 $, Claude 7,39 $. Bei gleicher Nutzerbewertung von Hermes und Claude (Mittelwert 4,0) ist Hermes auch in dieser Runde das mit Abstand günstigste Ergebnis.

---

# Runde 3: Gegenseitiges Review

Durchgeführt am 22.11.2026, Aufbau und Messwerte in [`RUNDE-3.md`](RUNDE-3.md). Jeder Agent begutachtete den Endstand eines anderen (Runde 1 plus Runde 2b) im Ringtausch, gegen den Tag `agentic-baseline` und ohne den geprüften Code zu verändern. Die Reviews liegen als `REVIEW-R3.md` auf den Branches `agent/codex-r3`, `agent/claude-opus-r3` und `agent/hermes-deepseek-r3`.

## R3.1 Messwerte und Urteile

| | Codex prüft Claude | Claude prüft Hermes | Hermes prüft Codex |
| --- | --- | --- | --- |
| Arbeitszeit | 8:37 min | 5:35 min | ca. 13 min |
| Kosten | 0,88 $ | 2,97 $ | 0,10 $ |
| Freigaben | 3 | 0 | 0 |
| Tests im Ziel-Branch selbst ausgeführt | 123, davon 120 bestanden, 3 übersprungen | 89, davon 86 bestanden, 3 übersprungen | 66, davon 63 bestanden, 3 übersprungen |
| Gesamturteil | nicht merge-fähig | merge-fähig nach kleinen Änderungen | merge-fähig nach kleinen Änderungen |
| Befunde (hoch / mittel / niedrig) | 1 / 4 / 1 | 0 / 4 / 4 | 1 / 0 / 4 |

Alle drei haben den Ziel-Branch wie verlangt in einem temporären Worktree geprüft, die Tests tatsächlich ausgeführt, die Anwendung gestartet, mit dem Playwright-MCP bei 360 und 1280 px geprüft und den temporären Worktree wieder entfernt. Keiner hat den Ziel-Branch verändert.

## R3.2 Prüfung der Befunde

Alle 19 Befunde wurden anschließend im Code der Ziel-Branches nachgeprüft:

- **Kein einziger Befund war falsch.** Jeder Befund ließ sich im Code bestätigen oder ist mit dem Code zumindest stimmig. Nicht selbst nachgestellt wurden zwei Befunde, die nur im Browser sichtbar werden: Codex' Befund zu überlangen Ortsnamen und Hermes' Befund zur Tastaturreihenfolge. Bei beiden ist die verursachende Stelle im Code vorhanden.
- **Die Agenten haben sauber zwischen Nachweis und Vermutung getrennt.** Claude kennzeichnete einen Befund ausdrücklich als Vermutung. Bei einem anderen war der Nachweis ungenau: Es gibt durchaus Tests auf „Fortsetzung“, allerdings nur für die Datenklasse und nicht für das gerenderte HTML. Der eigentliche Punkt, dass die Ergebnisansicht nie gerendert getestet wird, stimmt.
- **Nachweise auf hohem Niveau:** Codex feuerte zwölf parallele Speicheranfragen ab und stellte die Zeitumstellung mit einem Java-Probelauf nach. Claude stellte zwei Logikfehler mit `jshell` direkt gegen die kompilierten Klassen nach und probierte rund 20 Eingaben per `curl` durch. Hermes setzte für einen Test einen Ortsnamen per H2-Shell auf `NULL` und prüfte die Fokusreihenfolge mit 30 Tabulator-Schritten.

**Die wichtigsten bestätigten Befunde je Branch:**

- **Claude-Branch (geprüft von Codex):** Profilnamen sind nur im Code, nicht in der Datenbank eindeutig. Gleichzeitige Speichervorgänge erzeugen deshalb Duplikate. Ohne JavaScript gehen beim Ortswechsel die aktuellen Eingaben verloren. Die doppelte Stunde bei der Zeitumstellung erscheint im Band mit Breite 0, weil mit Uhrzeit statt mit Zeitpunkt gerechnet wird. Die klebende Suchleiste verdeckt mobil den Fokus.
- **Hermes-Branch (geprüft von Claude):** Ein Enddatum ohne Startdatum wird stillschweigend verworfen. Ein fehlender `weather_code` löscht ganze Stunden, obwohl der Wert gar nicht ausgewertet wird. Die Wetterdetails zählen den Niederschlag der Stunde vor dem Fenster mit. Beim Ortswechsel im Profil-Editor gehen Änderungen verloren.
- **Codex-Branch (geprüft von Hermes):** Am Desktop ist das gesamte Planungsformular per Tastatur nicht erreichbar, weil die neue CSS-Eigenschaft `reading-flow` mit `tabindex="-1"` am Formular kollidiert. Die übrigen Befunde sind klein: zu schwache Trennlinien, ein Fenster am Zeitraumende wird mit dem Folgetag beschriftet, gleichlautende Bedienelemente in der Ortssuche, eine Testlücke.

**Ein Befund wurde übersehen:** Der Hermes-Branch hat dasselbe Problem wie der Claude-Branch, nämlich keine Eindeutigkeit der Profilnamen in der Datenbank. Claude hat das in seinem Review nicht bemerkt und die Namensprüfung von Hermes sogar zur Übernahme empfohlen. Nur der Codex-Branch sichert die Eindeutigkeit über eine eindeutige Datenbankspalte ab. Hermes hob genau das in seinem Review als Stärke hervor.

## R3.3 Beobachtungen

1. **Die Strenge der Urteile hängt an einem einzigen Befund.** Codex urteilte „nicht merge-fähig“, die anderen „merge-fähig nach kleinen Änderungen“. Der Unterschied liegt fast nur am Doppelnamen-Problem, das Codex als einziger gezielt testete, vermutlich weil es dasselbe Problem in Runde 1 selbst gelöst hatte. Inhaltlich hätte dasselbe Urteil auch den Hermes-Branch getroffen.
2. **Die Reviews ergänzen sich.** Jeder Agent fand Fehler, die der Autor in seinem eigenen Bericht nicht genannt hatte. Kein Branch ist ohne Befunde mittlerer oder hoher Schwere. Ein einzelnes Review ist also nützlich, aber keine Garantie, wie der übersehene Befund zeigt.
3. **Reviews sind billiger als Umsetzungen.** 5 bis 13 Minuten und zusammen knapp 4 $ für drei gründliche Reviews mit 19 belegten Befunden. Hermes lieferte für 10 Cent ein Review auf demselben Niveau wie die anderen.
4. **Viele Befunde betreffen Ränder, die keine Aufgabe ausdrücklich verlangt hatte:** Nebenläufigkeit, Bedienung ohne JavaScript, Zeitumstellung, Tastaturbedienung am Desktop. Genau dort helfen Reviews am meisten.

## R3.4 Empfehlung für den endgültigen Stand

Kein Branch ist ohne Änderungen übernahmefähig. Sinnvoll wäre ein letzter Schritt „Zusammenführen“: einen Branch als Grundlage wählen, die Datenbank-Eindeutigkeit aus dem Codex-Branch übernehmen und die bestätigten Befunde mittlerer und hoher Schwere beheben, jeweils mit Regressionstest. Für die Wahl der Grundlage sprechen:

- **Claude:** die meisten Tests (123), beste Ergebnisdarstellung in der Nutzerbewertung, Mittelwert 4,0.
- **Hermes:** bester Gesamteindruck und beste Mobilansicht in der Nutzerbewertung, Mittelwert 4,0, saubere Darstellungsschicht.
- **Codex:** fachlich am robustesten (Datenbank-Eindeutigkeit, Zeitumstellung getestet, vollständig ohne JavaScript nutzbar), aber die schwächste Nutzerbewertung (3,6) und der Tastaturfehler am Desktop.

# Gesamtfazit des Experiments

| | Codex | Claude Code | Hermes + DeepSeek |
| --- | --- | --- | --- |
| Kosten gesamt (R1, R2, R2b, R3) | ca. 4,59 $ | ca. 24,22 $ | ca. 0,81 $ |
| Stärke | robuste Fachlogik, schlanker Code, Datenbank-Eindeutigkeit | Gründlichkeit, Testumfang, offene Dokumentation | Preis-Leistung, solide Ergebnisse, beste Nutzerwertung im Design |
| Schwäche | Freigaben nötig, schwächste Gestaltung | höchste Kosten, Kontingent schnell erschöpft | hoher Tokenverbrauch, sieht keine Bilder |

1. **Alle drei Agenten haben jede Aufgabe ohne Eingriff vollständig erledigt.** Die Unterschiede liegen in Gründlichkeit, Robustheit an den Rändern und Kosten, nicht in der Grundfähigkeit.
2. **Der Preisunterschied ist enorm, der Qualitätsunterschied nicht.** Hermes mit DeepSeek kostete über alle Runden rund ein Dreißigstel von Claude, bei vergleichbarer Nutzerbewertung und gleich belastbaren Reviews.
3. **Die Vorgaben entscheiden mehr als das Modell.** Ohne Designvorgabe entstand Einheitslook, mit Verboten Unstimmigkeit, mit positivem Zielbild ein stimmiges, aber austauschbares Ergebnis. Gute Aufgaben, Skills und Werkzeuge haben das Ergebnis stärker geprägt als die Wahl des Agenten.
4. **Der Mensch bleibt unverzichtbar:** als Auftraggeber, der Ziele positiv beschreibt, als Gutachter des Gesamteindrucks und als letzte Instanz über Reviews, die sich gegenseitig ergänzen, aber auch etwas übersehen.
