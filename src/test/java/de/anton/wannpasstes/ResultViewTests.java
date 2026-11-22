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
        assertEquals("So 22:00 – Mo 02:00", shown.rangeLabel());

        // Shown in both day rows, but listed (and labelled) only once.
        ResultView.DayRow sunday = view.days().get(0);
        ResultView.DayRow monday = view.days().get(1);
        assertEquals(List.of(shown), sunday.windows());
        assertEquals(List.of(shown), monday.continuations());
        assertTrue(monday.windows().isEmpty());
        assertTrue(sunday.segments().get(0).continuesAfter());
        assertTrue(monday.segments().get(0).continuesBefore());
        assertEquals("91.667", sunday.segments().get(0).offsetPercent());
        assertEquals("8.333", sunday.segments().get(0).widthPercent());
        assertEquals("0.000", monday.segments().get(0).offsetPercent());
        assertEquals("8.333", monday.segments().get(0).widthPercent());
    }

    @Test
    void labelsMultiDayWindowEndingAtMidnightWithBothDays() {
        SearchPeriod threeDays = new SearchPeriod(LocalDate.parse("2026-11-22"), LocalDate.parse("2026-11-24"));
        TimeWindow window = new TimeWindow(dayOne.plusHours(19), dayOne.plusHours(48));

        ResultView view = ResultView.of(List.of(window), threeDays, zone, dayOne);

        assertEquals("So 19:00 – Di 00:00 · 29 Std.",
                view.groups().get(0).windows().get(0).rangeLabel() + " · "
                        + view.groups().get(0).windows().get(0).durationLabel());
        // Sunday and Monday are covered, Tuesday 00:00 is only the end point.
        assertEquals(1, view.days().get(0).segments().size());
        assertEquals(1, view.days().get(1).segments().size());
        assertTrue(view.days().get(2).segments().isEmpty());
        assertTrue(view.days().get(2).isEmpty());
    }

    @Test
    void summarisesWeatherWithinTheWindow() {
        List<WeatherHour> hours = List.of(
                new WeatherHour(14.0, dayOne.plusHours(9), 20.0, 0.5, 0),
                new WeatherHour(16.4, dayOne.plusHours(10), 8.0, 0.4, 0),
                new WeatherHour(18.6, dayOne.plusHours(11), 11.0, 0.0, 0),
                new WeatherHour(17.0, dayOne.plusHours(12), 12.0, 0.1, 0),
                new WeatherHour(9.0, dayOne.plusHours(13), 30.0, 2.0, 0));
        TimeWindow window = new TimeWindow(dayOne.plusHours(10), dayOne.plusHours(12));

        ResultView.WindowView shown = ResultView.of(List.of(window), period, zone, dayOne, hours)
                .groups().get(0).windows().get(0);

        assertTrue(shown.hasWeather());
        assertEquals("16 bis 19 °C", shown.weather().temperature());
        assertEquals("8 bis 12 km/h", shown.weather().wind());
        // Precipitation of 10:00 describes the hour before the window and is not included.
        assertEquals("0,0 bis 0,1 mm/h", shown.weather().precipitation());
    }

    @Test
    void omitsWeatherDetailsWithoutHourlyData() {
        TimeWindow window = new TimeWindow(dayOne.plusHours(10), dayOne.plusHours(12));

        ResultView.WindowView shown = ResultView.of(List.of(window), period, zone, dayOne)
                .groups().get(0).windows().get(0);

        assertFalse(shown.hasWeather());
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
    void positionsSegmentsOnTheDayScaleAndMarksPastHoursOfToday() {
        TimeWindow window = new TimeWindow(dayOne.plusHours(12), dayOne.plusHours(24));

        ResultView view = ResultView.of(List.of(window), period, zone, dayOne.plusHours(6));

        assertEquals(2, view.days().size());
        ResultView.DayRow today = view.days().get(0);
        assertTrue(today.today());
        assertEquals("So", today.weekday());
        assertEquals("22.11.", today.dateLabel());
        assertEquals("50.000", today.segments().get(0).offsetPercent());
        assertEquals("50.000", today.segments().get(0).widthPercent());
        assertFalse(today.segments().get(0).continuesAfter());
        assertEquals("25.000", today.pastPercent());

        ResultView.DayRow tomorrow = view.days().get(1);
        assertFalse(tomorrow.today());
        assertEquals(null, tomorrow.pastPercent());
        assertTrue(tomorrow.isEmpty());
        assertEquals("So 22.11. – Mo 23.11.", view.periodLabel());
    }

    @Test
    void formatsDurations() {
        assertEquals("45 Min.", ResultView.formatDuration(45));
        assertEquals("3 Std.", ResultView.formatDuration(180));
        assertEquals("1 Std. 30 Min.", ResultView.formatDuration(90));
    }
}
