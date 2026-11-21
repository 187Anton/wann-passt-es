package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeWindowBoundaryTests {

    private final TimeWindowService service = new TimeWindowService();
    private final OffsetDateTime start =
            OffsetDateTime.parse("2026-11-08T10:00:00+01:00");

    @ParameterizedTest(name = "MILD at {0} degrees: suitable={1}")
    @CsvSource({"14.9, false", "15.0, true", "20.0, true",
            "25.0, true", "25.1, false"})
    void checksBothTemperatureBoundaries(double temperature, boolean suitable) {
        // Arrange: two measurements describe one hour.
        List<WeatherHour> hours = List.of(
                new WeatherHour(temperature, start, 10, 0, 0),
                new WeatherHour(temperature, start.plusHours(1), 10, 0, 0));

        // Act: use the real business logic without a Spring context.
        List<TimeWindow> result = service.findSuitableWindows(hours,
                TemperaturePreference.MILD, WindPreference.WEAK,
                PrecipitationPreference.DRY);

        // Assert: the configured limits are inclusive.
        List<TimeWindow> expected = suitable
                ? List.of(new TimeWindow(start, start.plusHours(1))) : List.of();
        assertEquals(expected, result);
    }

    @Test
    void rejectsAWindowWithoutPositiveDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> new TimeWindow(start, start));
    }
}
