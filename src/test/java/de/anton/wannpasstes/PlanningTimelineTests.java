package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Presentation layout only: the filtering rules stay in {@link TimeWindowService}.
 */
class PlanningTimelineTests {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final LocalDate SATURDAY = LocalDate.of(2026, 11, 21);
    private static final LocalDate SUNDAY = SATURDAY.plusDays(1);
    private static final OffsetDateTime NOW =
            OffsetDateTime.parse("2026-11-21T09:00:00+01:00");

    @Test
    void placesAWindowOnTheSharedDayScale() {
        TimeWindow window = window("2026-11-21T10:00:00+01:00", "2026-11-21T12:00:00+01:00");

        List<PlanningTimeline.Day> days = PlanningTimeline.build(
                List.of(window), SATURDAY, SATURDAY, ZONE, List.of(), NOW);

        assertEquals(1, days.size());
        PlanningTimeline.Day day = days.get(0);
        assertTrue(day.today());
        assertEquals("Samstag", day.name());
        assertEquals("21.11.", day.dateText());
        assertEquals(1, day.segments().size());

        PlanningTimeline.Segment segment = day.segments().get(0);
        assertEquals("left:41.667%;width:8.333%", segment.style());
        assertEquals("10:00–12:00", segment.timeText());
        assertEquals("2 Std.", segment.durationText());
        assertNull(segment.note());
        assertFalse(segment.continuesFromPrevious());
        assertFalse(segment.continuesNext());
    }

    @Test
    void showsAWindowAcrossMidnightInBothDayRows() {
        TimeWindow window = window("2026-11-21T22:00:00+01:00", "2026-11-22T02:00:00+01:00");

        List<PlanningTimeline.Day> days = PlanningTimeline.build(
                List.of(window), SATURDAY, SUNDAY, ZONE, List.of(), NOW);

        assertEquals(2, days.size());

        PlanningTimeline.Segment first = days.get(0).segments().get(0);
        assertEquals("left:91.667%;width:8.333%", first.style());
        assertEquals("Sa 22:00 – So 02:00", first.timeText());
        assertEquals("4 Std.", first.durationText());
        assertTrue(first.continuesNext());
        assertFalse(first.continuesFromPrevious());
        assertNull(first.note());

        PlanningTimeline.Segment second = days.get(1).segments().get(0);
        assertEquals("left:0.000%;width:8.333%", second.style());
        assertEquals("Fortsetzung", second.timeText());
        assertNull(second.durationText());
        assertTrue(second.continuesFromPrevious());
        assertFalse(second.continuesNext());
        assertEquals("bis So 02:00", second.note());
    }

    @Test
    void labelsAWindowEndingAtMidnightAsTwentyFour() {
        TimeWindow window = window("2026-11-21T19:00:00+01:00", "2026-11-22T00:00:00+01:00");

        List<PlanningTimeline.Day> days = PlanningTimeline.build(
                List.of(window), SATURDAY, SUNDAY, ZONE, List.of(), NOW);

        PlanningTimeline.Segment segment = days.get(0).segments().get(0);
        assertEquals("19:00–24:00", segment.timeText());
        assertEquals("5 Std.", segment.durationText());
        assertFalse(segment.continuesNext());
        assertTrue(days.get(1).segments().isEmpty());
    }

    @Test
    void labelsALongWindowWithItsRealEndDay() {
        TimeWindow window = window("2026-11-21T19:00:00+01:00", "2026-11-23T00:00:00+01:00");

        List<PlanningTimeline.Day> days = PlanningTimeline.build(
                List.of(window), SATURDAY, SATURDAY.plusDays(2), ZONE, List.of(), NOW);

        assertEquals(3, days.size());
        PlanningTimeline.Segment first = days.get(0).segments().get(0);
        assertEquals("Sa 19:00 – Mo 00:00", first.timeText());
        assertEquals("29 Std.", first.durationText());
        assertTrue(first.continuesNext());

        assertEquals("Fortsetzung", days.get(1).segments().get(0).timeText());
        assertEquals("bis Mo 00:00", days.get(1).segments().get(0).note());
        assertTrue(days.get(2).segments().isEmpty());
    }

    @Test
    void marksOnlyThePastHoursOfToday() {
        TimeWindow window = window("2026-11-21T18:00:00+01:00", "2026-11-21T20:00:00+01:00");
        OffsetDateTime noon = OffsetDateTime.parse("2026-11-21T12:00:00+01:00");

        List<PlanningTimeline.Day> days = PlanningTimeline.build(
                List.of(window), SATURDAY, SUNDAY, ZONE, List.of(), noon);

        assertEquals("width:50.000%", days.get(0).pastStyle());
        assertEquals("left:50.000%", days.get(0).nowStyle());
        assertNull(days.get(1).pastStyle());
        assertNull(days.get(1).nowStyle());
        assertFalse(days.get(1).today());
    }

    @Test
    void summarisesTheWeatherWithinTheWindow() {
        TimeWindow window = window("2026-11-21T10:00:00+01:00", "2026-11-21T12:00:00+01:00");
        List<WeatherHour> hours = List.of(
                hour("2026-11-21T10:00:00+01:00", 12.0, 5.0, 0.0),
                hour("2026-11-21T11:00:00+01:00", 18.0, 12.0, 0.3),
                hour("2026-11-21T12:00:00+01:00", 15.0, 8.0, 0.0),
                hour("2026-11-21T13:00:00+01:00", 30.0, 40.0, 1.0));

        PlanningTimeline.Day day = PlanningTimeline.build(
                List.of(window), SATURDAY, SATURDAY, ZONE, hours, NOW).get(0);

        PlanningTimeline.WindowDetails details = day.segments().get(0).details();
        assertEquals("12–18 °C", details.temperature());
        assertEquals("bis 12 km/h", details.wind());
        assertEquals("bis 0,3 mm/h", details.precipitation());
    }

    @Test
    void separatesSeveralWindowsOfOneDay() {
        List<TimeWindow> windows = List.of(
                window("2026-11-21T08:00:00+01:00", "2026-11-21T09:00:00+01:00"),
                window("2026-11-21T17:00:00+01:00", "2026-11-21T20:30:00+01:00"));

        PlanningTimeline.Day day = PlanningTimeline.build(
                windows, SATURDAY, SATURDAY, ZONE, List.of(), NOW).get(0);

        assertEquals(2, day.segments().size());
        assertEquals("08:00–09:00", day.segments().get(0).timeText());
        assertEquals("17:00–20:30", day.segments().get(1).timeText());
        assertEquals("3 Std. 30 Min.", day.segments().get(1).durationText());
    }

    @Test
    void formatsDurationsInHoursAndMinutes() {
        assertEquals("45 Min.", PlanningTimeline.formatDuration(45));
        assertEquals("1 Std.", PlanningTimeline.formatDuration(60));
        assertEquals("2 Std. 30 Min.", PlanningTimeline.formatDuration(150));
        assertEquals("29 Std.", PlanningTimeline.formatDuration(29 * 60));
    }

    @Test
    void returnsNoRowsWithoutARange() {
        assertEquals(List.of(), PlanningTimeline.build(
                List.of(window("2026-11-21T10:00:00+01:00", "2026-11-21T11:00:00+01:00")),
                null, null, ZONE, List.of(), NOW));
    }

    private static TimeWindow window(String start, String end) {
        return new TimeWindow(OffsetDateTime.parse(start), OffsetDateTime.parse(end));
    }

    private static WeatherHour hour(String time, double temperature, double wind, double precipitation) {
        return new WeatherHour(temperature, OffsetDateTime.parse(time), wind, precipitation, 0);
    }
}
