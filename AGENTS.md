# AGENTS.md – Arbeitsregeln für Coding-Agenten

Diese Datei gilt für jeden Coding-Agenten, der in diesem Repository arbeitet. Lies außerdem `README.md` (Projekt, Start, Tests, bekannte Grenzen) und – falls vorhanden – `TASK.md` (aktueller Auftrag) vollständig, bevor du etwas änderst.

## Projekt in Kürze

„Wann passt es?“ ist eine lokale Spring-Boot-Webanwendung, die wetterbasiert passende Zeitfenster für Outdoor-Aktivitäten berechnet. Stack: Java 21, Spring Boot 4.1, Spring MVC + Thymeleaf, Bean Validation, Spring Data JPA, H2, Maven-Wrapper. Alles liegt im Package `de.anton.wannpasstes`.

## Befehle

| Zweck | Befehl |
| --- | --- |
| Tests | `./mvnw test` |
| Anwendung starten | `./mvnw spring-boot:run` (Port 8080; anderer Port: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`) |
| Kompilieren ohne Tests | `./mvnw -q compile` |

## Arbeitsweise

- Arbeite **selbstständig**: Setze den Auftrag aus `TASK.md` vollständig um, ohne für einzelne Zwischenschritte nachzufragen. Frage nur bei echten Blockern nach (z. B. fehlende Werkzeuge, widersprüchliche Anforderungen, die sich nicht sinnvoll auflösen lassen).
- Triffst du eine Entscheidung, die `TASK.md` offenlässt, wähle eine vernünftige Lösung und dokumentiere sie im Abschlussbericht.
- Verschaffe dir zuerst einen Überblick über den bestehenden Code und folge seinem Stil und seiner Struktur.
- Arbeite in nachvollziehbaren Schritten und committe auf dem aktuellen Branch mit aussagekräftigen englischen Commit-Nachrichten.
- **Nicht** pushen, keine Branches wechseln, keine anderen Branches oder Worktrees verändern, kein `git reset --hard` oder Force-Operationen.
- `AGENTS.md`, `CLAUDE.md` und `TASK.md` nicht verändern.

## Konventionen

- Codebezeichner, Codekommentare und Commit-Nachrichten auf Englisch.
- Texte der Benutzeroberfläche und Projektdokumentation auf Deutsch.
- Bestehende Tests nur ändern, wenn sich das geprüfte Verhalten bewusst ändert; das im Abschlussbericht begründen.
- Neue Abhängigkeiten in `pom.xml` nur, wenn sie wirklich nötig sind, und dann im Abschlussbericht begründen.
- Keine echten Secrets, Tokens oder personenbezogenen Daten; Testdaten sind synthetisch.
- Tests rufen keine Live-APIs auf; externe Dienste werden simuliert (wie in den bestehenden Tests).
- `./data/` (lokale Datenbank) und `target/` nicht committen.

## Verifikation

- Führe `./mvnw test` vor dem Abschluss tatsächlich aus. Die Suite muss grün sein.
- Melde Tests nur als bestanden, wenn du sie wirklich ausgeführt hast. Unterscheide klar zwischen ausgeführten Prüfungen, reiner Codeprüfung und Vermutungen.
- Wenn du die Anwendung im Browser oder per HTTP prüfen kannst, tu das und beschreibe, was du geprüft hast.

## Abschlussbericht

Lege am Ende `AGENT_REPORT.md` im Projektwurzelverzeichnis an und committe sie. Inhalt:

1. Umgesetzte Punkte, zugeordnet zu den Akzeptanzkriterien aus `TASK.md` (erfüllt / teilweise / nicht erfüllt).
2. Eigene Entscheidungen und Annahmen, inklusive Begründung.
3. Gestaltungskonzept des Frontends in wenigen Sätzen.
4. Ausgeführte Prüfungen mit Ergebnis (Anzahl Tests, bestanden/fehlgeschlagen/übersprungen).
5. Bekannte Lücken, Risiken und Vorschläge für nächste Schritte.
