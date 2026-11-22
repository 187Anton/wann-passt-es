package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanningTimelineTests {

    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

    @Test
    void leavesTheMissingClockHourUnpaintedInSpring() {
        LocalDate date = LocalDate.of(2026, 3, 29);
        OffsetDateTime start = date.atTime(1, 0).atZone(BERLIN).toOffsetDateTime();
        OffsetDateTime end = start.plusHours(2).atZoneSameInstant(BERLIN).toOffsetDateTime();
        var days = PlanningTimeline.create(date, date.plusDays(1), start.minusMinutes(15), BERLIN,
                List.of(new TimeWindow(start, end)), List.of(hour(start), hour(end)));

        assertEquals(45, days.getFirst().pastMinutes());
        assertEquals(List.of(new PlanningTimeline.Band(60, 120), new PlanningTimeline.Band(180, 240)),
                days.getFirst().bands());
        assertEquals("2 Std.", days.getFirst().entries().getFirst().duration());
        assertEquals(0, days.getLast().pastMinutes());
        assertEquals(List.of(), days.getLast().bands());
    }

    @Test
    void repeatedClockHourKeepsBothOffsetsAndTheElapsedDuration() {
        LocalDate date = LocalDate.of(2026, 10, 25);
        OffsetDateTime start = date.atTime(2, 0).atZone(BERLIN).toOffsetDateTime();
        OffsetDateTime end = start.plusHours(1).atZoneSameInstant(BERLIN).toOffsetDateTime();
        var day = PlanningTimeline.create(date, date, start.minusHours(1), BERLIN,
                List.of(new TimeWindow(start, end)), List.of(hour(start), hour(end))).getFirst();

        assertEquals(List.of(new PlanningTimeline.Band(120, 180)), day.bands());
        var entry = day.entries().getFirst();
        assertEquals("02:00 (UTC+02:00)", entry.startLabel());
        assertEquals("02:00 (UTC+01:00)", entry.endLabel());
        assertEquals("1 Std.", entry.duration());
    }

    private static WeatherHour hour(OffsetDateTime time) {
        return new WeatherHour(18, time, 10, 0, 0);
    }
}
