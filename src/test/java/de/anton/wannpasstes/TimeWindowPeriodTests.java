package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Window search over a period of several days (package B).
 */
class TimeWindowPeriodTests {

    private final TimeWindowService service = new TimeWindowService();

    private final OffsetDateTime dayOne = OffsetDateTime.parse("2026-11-22T00:00+01:00");
    private final OffsetDateTime periodEnd = dayOne.plusDays(3);

    /** Hourly series over three days; hours in {@code goodHours} (offsets from day one) are suitable. */
    private List<WeatherHour> series(List<Integer> goodHours) {
        List<WeatherHour> hours = new ArrayList<>();
        for (int i = 0; i <= 72; i++) {
            boolean good = goodHours.contains(i);
            hours.add(new WeatherHour(good ? 18.0 : 2.0, dayOne.plusHours(i), 5.0, 0.0, 0));
        }
        return hours;
    }

    private List<TimeWindow> search(List<WeatherHour> hours, OffsetDateTime from, int minDuration) {
        return service.findSuitableWindows(hours, from, periodEnd,
                TemperaturePreference.MILD, WindPreference.WEAK, PrecipitationPreference.DRY, minDuration);
    }

    @Test
    void findsWindowsOnSeveralDaysInChronologicalOrder() {
        // day one 10-12, day three 14-17
        List<WeatherHour> hours = series(List.of(10, 11, 12, 62, 63, 64, 65));

        assertEquals(List.of(
                        new TimeWindow(dayOne.plusHours(10), dayOne.plusHours(12)),
                        new TimeWindow(dayOne.plusHours(62), dayOne.plusHours(65))),
                search(hours, dayOne, 60));
    }

    @Test
    void keepsWindowAcrossMidnightAsOneWindow() {
        // day one 22:00 to day two 02:00
        List<WeatherHour> hours = series(List.of(22, 23, 24, 25, 26));

        assertEquals(List.of(new TimeWindow(dayOne.plusHours(22), dayOne.plusHours(26))),
                search(hours, dayOne, 60));
    }

    @Test
    void appliesMinimumDurationToTheWholeWindowAcrossMidnight() {
        // two hours before and two hours after midnight; each half alone is too short
        List<WeatherHour> hours = series(List.of(22, 23, 24, 25, 26));

        assertEquals(List.of(new TimeWindow(dayOne.plusHours(22), dayOne.plusHours(26))),
                search(hours, dayOne, 240));
        assertEquals(List.of(), search(hours, dayOne, 241));
    }

    @Test
    void ignoresHoursBeforeTheStartOfTheSearch() {
        List<WeatherHour> hours = series(List.of(8, 9, 10, 11, 12));
        OffsetDateTime now = dayOne.plusHours(10).plusMinutes(15);

        // the window starts with the first hourly value after "now"
        assertEquals(List.of(new TimeWindow(dayOne.plusHours(11), dayOne.plusHours(12))),
                search(hours, now, 60));
    }

    @Test
    void cutsWindowsAtTheEndOfThePeriod() {
        List<WeatherHour> hours = series(List.of(70, 71, 72));
        // the series ends exactly at the period end (midnight after day three)
        assertEquals(List.of(new TimeWindow(dayOne.plusHours(70), dayOne.plusHours(72))),
                search(hours, dayOne, 60));

        List<WeatherHour> longer = new ArrayList<>(hours);
        longer.add(new WeatherHour(18.0, dayOne.plusHours(73), 5.0, 0.0, 0));
        assertEquals(List.of(new TimeWindow(dayOne.plusHours(70), dayOne.plusHours(72))),
                search(longer, dayOne, 60));
    }
}
