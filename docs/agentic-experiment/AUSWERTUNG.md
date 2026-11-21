# Auswertung: drei Coding-Agenten, eine Aufgabe

Durchgeführt am 21.11.2026. Aufbau, Start-Prompt und Regeln stehen in [`EXPERIMENT.md`](EXPERIMENT.md), die Aufgabe in `TASK.md` auf dem Branch `agentic/baseline` (Tag `agentic-baseline`).

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
