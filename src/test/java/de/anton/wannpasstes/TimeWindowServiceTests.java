package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeWindowServiceTests {

    private final TimeWindowService service = new TimeWindowService();

    private final OffsetDateTime start =
            OffsetDateTime.parse("2026-10-25T14:00:00+01:00");

    @Test
    void mergesAdjacentWindowsRegardlessOfInputOrder() {
        TimeWindow first = new TimeWindow(
                start,
                start.plusHours(1));

        TimeWindow second = new TimeWindow(
                start.plusHours(1),
                start.plusHours(2));

        List<TimeWindow> result =
                service.mergeWindows(List.of(second, first));

        assertEquals(
                List.of(new TimeWindow(start, start.plusHours(2))),
                result);
    }

    @Test
    void keepsWindowsSeparateWhenThereIsAGap() {
        TimeWindow first = new TimeWindow(
                start,
                start.plusHours(1));

        TimeWindow second = new TimeWindow(
                start.plusHours(2),
                start.plusHours(3));

        List<TimeWindow> result =
                service.mergeWindows(List.of(first, second));

        assertEquals(List.of(first, second), result);
    }

    @Test
    void usesEndPrecipitationAndMergesSuitableIntervals() {
        List<WeatherHour> hours = List.of(
                new WeatherHour(18.0, start, 12.0, 5.0, 0),
                new WeatherHour(19.0, start.plusHours(1), 15.0, 0.0, 0),
                new WeatherHour(20.0, start.plusHours(2), 10.0, 0.2, 0));

        List<TimeWindow> result = service.findSuitableWindows(hours, TemperaturePreference.MILD, WindPreference.WEAK, PrecipitationPreference.LIGHT_DRIZZLE);

        assertEquals(
                List.of(new TimeWindow(start, start.plusHours(2))),
                result);
    }

    @Test
    void rejectsIntervalWhenEndPrecipitationExceedsLimit() {
        List<WeatherHour> hours = List.of(
                new WeatherHour(18.0, start, 12.0, 0.0, 0),
                new WeatherHour(19.0, start.plusHours(1), 15.0, 1.0, 0));

        List<TimeWindow> result = service.findSuitableWindows(hours, TemperaturePreference.MILD, WindPreference.WEAK, PrecipitationPreference.LIGHT_DRIZZLE);

        assertEquals(List.of(), result);
    }

    @Test
    void doesNotCreateIntervalAcrossMissingHourlyData() {
        List<WeatherHour> hours = List.of(
                new WeatherHour(18.0, start, 12.0, 0.0, 0),
                new WeatherHour(19.0, start.plusHours(2), 15.0, 0.0, 0));

        List<TimeWindow> result = service.findSuitableWindows(hours, TemperaturePreference.MILD, WindPreference.WEAK, PrecipitationPreference.LIGHT_DRIZZLE);

        assertEquals(List.of(), result);
    }
    @Test
    void appliesSelectedWeatherPreferences() {
        List<WeatherHour> hours = List.of(
                new WeatherHour(28.0, start, 18.0, 0.0, 61),
                new WeatherHour(28.0, start.plusHours(1), 18.0, 0.5, 61));

        List<TimeWindow> expected =
                List.of(new TimeWindow(start, start.plusHours(1)));

        assertEquals(expected, service.findSuitableWindows(
                hours,
                TemperaturePreference.WARM,
                WindPreference.MODERATE,
                PrecipitationPreference.LIGHT_RAIN));

        assertEquals(List.of(), service.findSuitableWindows(
                hours,
                TemperaturePreference.MILD,
                WindPreference.MODERATE,
                PrecipitationPreference.LIGHT_RAIN));

        assertEquals(List.of(), service.findSuitableWindows(
                hours,
                TemperaturePreference.WARM,
                WindPreference.WEAK,
                PrecipitationPreference.LIGHT_RAIN));

        assertEquals(List.of(), service.findSuitableWindows(
                hours,
                TemperaturePreference.WARM,
                WindPreference.MODERATE,
                PrecipitationPreference.DRY));

        assertEquals(expected, service.findSuitableWindows(
                hours,
                TemperaturePreference.ANY,
                WindPreference.ANY,
                PrecipitationPreference.ANY));
    }
}
