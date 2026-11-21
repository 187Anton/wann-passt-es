# TASK.md – Ausbau von „Wann passt es?“

Dieser Auftrag erweitert die bestehende erste Version um vier Arbeitspakete. Lies vorher `AGENTS.md` und `README.md`. Alle Pakete gehören zum Auftrag; die Reihenfolge der Umsetzung ist dir überlassen.

Wo dieser Auftrag etwas offenlässt (URL-Struktur, Klassenaufteilung, Gestaltung), entscheidest du selbst und dokumentierst die Entscheidung in `AGENT_REPORT.md`.

## Rahmenbedingungen

- Technik bleibt: Spring Boot, Spring MVC, Thymeleaf, JPA, H2. Kein separates Frontend-Framework (kein React, Vue, Angular o. Ä.).
- Frontend nur mit eigenem CSS und optional Vanilla-JavaScript unter `src/main/resources/static/`. **Keine** CSS-/JS-Bibliotheken, keine CDNs, keine externen Fonts, kein npm oder anderer Build-Schritt.
- Die Anwendung startet weiterhin mit `./mvnw spring-boot:run`, die Tests laufen mit `./mvnw test`.
- Bestehende Funktionen bleiben erhalten, sofern dieser Auftrag sie nicht ausdrücklich ändert.
- Die Wetterparameter (Temperatur-, Wind-, Niederschlagsgrenzen) und die Zeitfensterlogik pro Stundenintervall bleiben fachlich unverändert. `weatherCode` wird weiterhin nicht ausgewertet.
- Bestehende Profile in einer vorhandenen lokalen Datenbank dürfen durch Schemaänderungen nicht verloren gehen oder die Anwendung am Start hindern.

---

## Paket A – Suchprofile verbessern

**Ziel:** Profile sind verständlich, eindeutig und bearbeitbar.

| ID | Akzeptanzkriterium |
| --- | --- |
| A1 | Beim Speichern eines Profils wird zusätzlich zur Orts-ID der Ortsname gespeichert. Die Profilübersicht zeigt den Ortsnamen statt (oder deutlich vor) der Orts-ID. |
| A2 | Ältere Profile ohne gespeicherten Ortsnamen werden weiterhin angezeigt und bleiben benutzbar (sinnvoller Platzhalter oder nachträgliche Ermittlung). |
| A3 | Profilnamen sind eindeutig. Der Vergleich ignoriert führende/nachfolgende Leerzeichen und Groß-/Kleinschreibung. |
| A4 | Ein doppelter Name beim Speichern wird mit einer verständlichen Fehlermeldung abgelehnt; die bisherige Auswahl bleibt dabei erhalten (wie heute bei einem leeren Namen). |
| A5 | Ein Profil lässt sich bearbeiten: Name, Ort, Aktivität, Mindestdauer und alle drei Wetterwünsche. Die Bearbeitung ist aus der Profilübersicht erreichbar. |
| A6 | Beim Bearbeiten gelten dieselben Regeln wie beim Anlegen (1–100 Zeichen nach Trimmen, eindeutiger Name; das Profil selbst darf seinen Namen behalten). Fehler werden verständlich angezeigt, die Eingaben gehen nicht verloren. |
| A7 | Nach erfolgreichem Bearbeiten erscheint eine Erfolgsmeldung; die Änderung ist nach einem Neustart der Anwendung noch vorhanden. |
| A8 | Der Aufruf eines nicht existierenden Profils (Bearbeiten oder Löschen) führt zu einer verständlichen Meldung statt zu einem Serverfehler. |

## Paket B – Mehrtägiger Suchzeitraum

**Ziel:** Statt eines einzelnen Tags lässt sich ein Zeitraum innerhalb der sieben Vorhersagetage durchsuchen.

| ID | Akzeptanzkriterium |
| --- | --- |
| B1 | Auf der Planungsseite lassen sich ein Start- und ein Enddatum wählen. Erlaubt ist jeweils heute bis heute + 6 Tage (in der Zeitzone des gewählten Orts). |
| B2 | Ohne Angabe wird wie bisher nur der heutige Tag durchsucht. |
| B3 | Ein Enddatum vor dem Startdatum oder Daten außerhalb des erlaubten Bereichs führen zu einer verständlichen Meldung; es wird dann keine Wetterabfrage ausgeführt. |
| B4 | Es werden alle passenden Zeitfenster im gesamten Zeitraum angezeigt, jeweils mit Datum, Uhrzeit und Dauer, chronologisch sortiert. Vergangene Stunden werden wie bisher ignoriert. |
| B5 | Ein Zeitfenster, das ohne Lücke über Mitternacht reicht, wird als **ein** zusammenhängendes Fenster dargestellt, nicht in zwei Hälften geteilt. Die Mindestdauer bezieht sich auf das zusammenhängende Fenster. |
| B6 | Bestehende Links mit dem bisherigen Parameter `searchDate` funktionieren weiter und durchsuchen genau diesen einen Tag. |
| B7 | Die Logik ist durch automatisierte Tests abgedeckt, mindestens: Standardfall, ungültiger Zeitraum, mehrere Tage, Fenster über Mitternacht. |

## Paket C – Robustheit und Fehlerbehandlung

**Ziel:** Fehlerhafte Eingaben und Daten führen zu verständlichen Meldungen, nie zu einer technischen Fehlerseite.

| ID | Akzeptanzkriterium |
| --- | --- |
| C1 | Ungültige Request-Parameter (z. B. unbekannter Enum-Wert, Text statt Zahl, Mindestdauer 0 oder negativ, ungültiges Datum) führen zu einer gestalteten, deutschsprachigen Fehlerseite oder -meldung mit Weg zurück zur Planung – nicht zur Spring-„Whitelabel Error Page“ oder einem Stacktrace. Der HTTP-Status ist passend (4xx). |
| C2 | Unbekannte URLs zeigen eine gestaltete 404-Seite. Unerwartete Serverfehler zeigen eine gestaltete Fehlerseite ohne technische Details. |
| C3 | Unvollständige oder inkonsistente Wetterdaten (unterschiedlich lange Stundenlisten, fehlende Listen, `null`-Werte) führen nicht zu einem Absturz. Unbrauchbare Stunden werden übersprungen oder es erscheint eine verständliche Meldung. |
| C4 | Beim Ortswechsel über die Ortssuche bleiben Aktivität, Mindestdauer, Wetterwünsche und Zeitraum erhalten. |
| C5 | C1, C3 und C4 sind durch automatisierte Tests abgedeckt. |

## Paket D – Frontend

**Ziel:** Die bisher ungestalteten Seiten erhalten ein durchgängiges, modernes Erscheinungsbild. Die konkrete Gestaltung (Farben, Typografie, Layout, Darstellung der Ergebnisse) ist bewusst dir überlassen.

**Gestaltungsprinzipien**

- Einheitliches Erscheinungsbild auf allen Seiten, inklusive gemeinsamer Kopfzeile/Navigation zwischen Planung, Ortssuche und Profilen.
- Klar, ruhig und aufgeräumt; das Formular soll ohne Erklärung verständlich sein.
- Die Ergebnisse sollen auf einen Blick erfassbar sein – nicht nur als nackte Textliste.
- Zustände sind sichtbar unterscheidbar: Erfolg, Fehler, „keine passenden Zeitfenster“, Laden/Eingaben fehlerhaft.
- Mobilfreundlich und barrierearm.

| ID | Akzeptanzkriterium |
| --- | --- |
| D1 | Alle Seiten (Planung, Ortssuche, Profile, Profil bearbeiten, Fehlerseiten) nutzen ein gemeinsames Layout bzw. gemeinsame Stile und eine gemeinsame Navigation. |
| D2 | Die Seiten sind ab 360 px Breite ohne horizontales Scrollen nutzbar und auf Desktop-Breite sinnvoll angeordnet. |
| D3 | Jedes Eingabefeld hat ein sichtbares, verknüpftes Label; die Bedienung per Tastatur ist möglich und der Fokus ist sichtbar. |
| D4 | Text und Bedienelemente erfüllen mindestens WCAG-AA-Kontrast. |
| D5 | Kernfunktionen (planen, Ort suchen, Profil speichern/verwenden/bearbeiten/löschen) funktionieren auch ohne JavaScript. JavaScript darf die Bedienung nur verbessern. |
| D6 | Erfolgs-, Fehler- und Leerzustände sind klar gestaltet; „keine passenden Zeitfenster“ wird freundlich erklärt. |
| D7 | Die Quellenangaben zu Open-Meteo/GeoNames bleiben sichtbar. |
| D8 | Das optionale Browser-Szenario `src/test/browser/profile-flow.mjs` wird an geänderte Seiten angepasst, sodass es den Ablauf Speichern–Verwenden–Löschen weiterhin abbildet. (Ausführung optional, siehe `README.md`.) |

---

## Allgemeine Abnahme

| ID | Akzeptanzkriterium |
| --- | --- |
| G1 | `./mvnw test` läuft vollständig grün. |
| G2 | Neues Verhalten ist durch neue Tests abgedeckt; bestehende Tests wurden nur bei bewusst geändertem Verhalten angepasst. |
| G3 | `README.md` ist an den neuen Funktionsumfang angepasst (Funktionen und bekannte Grenzen). |
| G4 | `AGENT_REPORT.md` ist gemäß `AGENTS.md` angelegt. |
| G5 | Alle Änderungen sind auf dem aktuellen Branch committet; nichts wurde gepusht. |
