package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WeatherHourTests {

    @Test
    void matchesWhenWeatherIsWithinLimits() {
        WeatherHour hour = new WeatherHour(18.0, OffsetDateTime.parse("2026-10-25T14:00:00+01:00"),12.0, 0.0,0);

        assertTrue(hour.matches(10.0, 25.0, 20.0,0.2));
    }

    @Test
    void matchesWhenWeatherIsAtUpperLimit() {
        WeatherHour hour = new WeatherHour(25.0, OffsetDateTime.parse("2026-10-25T14:00:00+01:00"),20.0, 0.2,0);
        assertTrue(hour.matches(10.0, 25.0, 20.0,0.2));
    }

    @Test
    void doesNotMatchWhenWindExceedsLimit() {
        WeatherHour hour = new WeatherHour(18.0, OffsetDateTime.parse("2026-10-25T14:00:00+01:00"),21.0, 0.0,0);
        assertFalse(hour.matches(10.0, 25.0, 20.0,0.2));
    }
}
