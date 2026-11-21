package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultViewTests {

    private final ZoneId zone = ZoneId.of("Europe/Berlin");
    private final SearchPeriod period = new SearchPeriod(LocalDate.parse("2026-11-22"), LocalDate.parse("2026-11-23"));
    private final OffsetDateTime dayOne = OffsetDateTime.parse("2026-11-22T00:00+01:00");

    @Test
    void groupsWindowsByStartDayWithDateTimeAndDuration() {
        List<TimeWindow> windows = List.of(
                new TimeWindow(dayOne.plusHours(34), dayOne.plusHours(36)),
                new TimeWindow(dayOne.plusHours(10), dayOne.plusHours(12).plusMinutes(0)));

        ResultView view = ResultView.of(windows, period, zone, dayOne);

        assertEquals(2, view.windowCount());
        assertEquals("Sonntag, 22. November", view.groups().get(0).label());
        assertEquals("10:00", view.groups().get(0).windows().get(0).startTime());
        assertEquals("12:00", view.groups().get(0).windows().get(0).endTime());
        assertEquals("2 Std.", view.groups().get(0).windows().get(0).durationLabel());
        assertEquals("Montag, 23. November", view.groups().get(1).label());
        assertEquals("4 Std.", view.totalDurationLabel());
    }

    @Test
    void showsWindowAcrossMidnightOnceWithEndDay() {
        TimeWindow window = new TimeWindow(dayOne.plusHours(22), dayOne.plusHours(26));

        ResultView view = ResultView.of(List.of(window), period, zone, dayOne);

        assertEquals(1, view.groups().size());
        ResultView.WindowView shown = view.groups().get(0).windows().get(0);
        assertTrue(shown.spansMidnight());
        assertEquals("22:00", shown.startTime());
        assertEquals("02:00", shown.endTime());
        assertEquals("Mo., 23.11.", shown.endDayLabel());
        assertEquals(1, view.timeline().segments().size());
    }

    @Test
    void showsWindowEndingAtMidnightAsTwentyFourHundred() {
        TimeWindow window = new TimeWindow(dayOne.plusHours(20), dayOne.plusHours(24));

        ResultView.WindowView shown = ResultView.of(List.of(window), period, zone, dayOne)
                .groups().get(0).windows().get(0);

        assertFalse(shown.spansMidnight());
        assertEquals("24:00", shown.endTime());
    }

    @Test
    void positionsTimelineSegmentsRelativeToThePeriod() {
        TimeWindow window = new TimeWindow(dayOne.plusHours(12), dayOne.plusHours(24));

        ResultView view = ResultView.of(List.of(window), period, zone, dayOne.plusHours(6));

        assertEquals("25.000", view.timeline().segments().get(0).offsetPercent());
        assertEquals("25.000", view.timeline().segments().get(0).widthPercent());
        assertEquals("12.500", view.timeline().pastWidthPercent());
        assertEquals(2, view.timeline().days().size());
    }

    @Test
    void formatsDurations() {
        assertEquals("45 Min.", ResultView.formatDuration(45));
        assertEquals("3 Std.", ResultView.formatDuration(180));
        assertEquals("1 Std. 30 Min.", ResultView.formatDuration(90));
    }
}
