package de.anton.wannpasstes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MainpageRangeTests {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-21T09:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 21);

    private final WeatherClient weatherClient = mock(WeatherClient.class);
    private final GeocodingClient geocodingClient = mock(GeocodingClient.class);
    private final MainpageController controller = new MainpageController(
            weatherClient, new TimeWindowService(), geocodingClient, CLOCK);

    @BeforeEach
    void stubLocation() {
        when(geocodingClient.getLocation(1)).thenReturn(
                new GeocodingResponse.Location(1, "Testort", 50.0, 8.0,
                        ZONE.getId(), "Deutschland", "Testregion"));
    }

    @Test
    void searchesTodayWhenNoDatesAreGiven() {
        when(weatherClient.fetchForecast(50.0, 8.0, ZONE)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = render(null, null, null, 60, model);

        assertEquals("mainpage", view);
        verify(weatherClient).fetchForecast(50.0, 8.0, ZONE);
        assertEquals(TODAY, model.get("startDate"));
        assertEquals(TODAY, model.get("endDate"));
        assertEquals("21.11.2026", model.get("rangeLabel"));
        assertNull(model.get("weatherError"));
    }

    @Test
    void rejectsAnEndDateBeforeTheStartDateWithoutCallingTheWeatherApi() {
        ExtendedModelMap model = new ExtendedModelMap();
        String view = render(LocalDate.of(2026, 11, 24), LocalDate.of(2026, 11, 22),
                null, 60, model);

        assertEquals("mainpage", view);
        verify(weatherClient, never()).fetchForecast(anyDouble(), anyDouble(), any());
        String error = (String) model.get("weatherError");
        assertTrue(error.contains("Enddatum"), error);
        assertTrue(error.contains("vor dem Startdatum"), error);
        assertEquals(List.of(), model.get("suitableWindows"));
    }

    @Test
    void rejectsDatesOutsideTheForecastRangeWithoutCallingTheWeatherApi() {
        ExtendedModelMap model = new ExtendedModelMap();
        render(TODAY.minusDays(1), TODAY, null, 60, model);

        assertTrue(((String) model.get("weatherError"))
                .contains("Bitte wähle einen Zeitraum zwischen"));

        ExtendedModelMap laterModel = new ExtendedModelMap();
        render(TODAY.plusDays(6), TODAY.plusDays(8), null, 60, laterModel);

        assertTrue(((String) laterModel.get("weatherError"))
                .contains("Bitte wähle einen Zeitraum zwischen"));
        verify(weatherClient, never()).fetchForecast(anyDouble(), anyDouble(), any());
    }

    @Test
    void returnsWindowsOfAllDaysChronologically() {
        when(weatherClient.fetchForecast(50.0, 8.0, ZONE))
                .thenReturn(happyHours(
                        LocalDate.of(2026, 11, 22), 10, 3,
                        LocalDate.of(2026, 11, 23), 10, 3,
                        LocalDate.of(2026, 11, 24), 10, 3));

        ExtendedModelMap model = new ExtendedModelMap();
        render(LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 24), null, 120, model);

        List<TimeWindow> windows = windows(model);
        assertEquals(2, windows.size());
        assertEquals(OffsetDateTime.parse("2026-11-22T10:00:00+01:00"), windows.get(0).start());
        assertEquals(OffsetDateTime.parse("2026-11-22T12:00:00+01:00"), windows.get(0).end());
        assertEquals(OffsetDateTime.parse("2026-11-24T10:00:00+01:00"), windows.get(1).start());
        assertEquals(120, windows.get(0).durationMinutes());
    }

    @Test
    void keepsAWindowThatRunsAcrossMidnightAsOneWindow() {
        when(weatherClient.fetchForecast(50.0, 8.0, ZONE))
                .thenReturn(happyHours(LocalDate.of(2026, 11, 21), 22, 5));

        ExtendedModelMap model = new ExtendedModelMap();
        render(TODAY, TODAY.plusDays(1), null, 240, model);

        List<TimeWindow> windows = windows(model);
        assertEquals(1, windows.size(), String.valueOf(model.get("weatherError")));
        assertEquals(OffsetDateTime.parse("2026-11-21T22:00:00+01:00"), windows.get(0).start());
        assertEquals(OffsetDateTime.parse("2026-11-22T02:00:00+01:00"), windows.get(0).end());
        assertEquals(240, windows.get(0).durationMinutes());
    }

    @Test
    void legacySearchDateSearchesExactlyThatDay() {
        when(weatherClient.fetchForecast(50.0, 8.0, ZONE))
                .thenReturn(happyHours(
                        LocalDate.of(2026, 11, 22), 10, 3,
                        LocalDate.of(2026, 11, 24), 10, 3));

        ExtendedModelMap model = new ExtendedModelMap();
        render(null, null, LocalDate.of(2026, 11, 24), 120, model);

        assertEquals(LocalDate.of(2026, 11, 24), model.get("startDate"));
        assertEquals(LocalDate.of(2026, 11, 24), model.get("endDate"));
        List<TimeWindow> windows = windows(model);
        assertEquals(1, windows.size(), String.valueOf(model.get("weatherError")));
        assertEquals(OffsetDateTime.parse("2026-11-24T10:00:00+01:00"), windows.get(0).start());
    }

    private String render(LocalDate start, LocalDate end, LocalDate legacySearchDate,
                          int minDurationMinutes, ExtendedModelMap model) {
        return controller.mainpage(1, minDurationMinutes,
                PrecipitationPreference.LIGHT_DRIZZLE, TemperaturePreference.MILD,
                WindPreference.WEAK, Activity.WALKING,
                start, end, legacySearchDate, model);
    }

    @SuppressWarnings("unchecked")
    private List<TimeWindow> windows(ExtendedModelMap model) {
        return (List<TimeWindow>) model.get("suitableWindows");
    }

    /**
     * Builds hourly values starting at {@code hour} on each given date. Hour
     * blocks are made suitable for MILD/WEAK/DRY except on 23 November.
     */
    private List<WeatherHour> happyHours(Object... dayAndHourPairs) {
        List<WeatherHour> hours = new ArrayList<>();
        for (int i = 0; i < dayAndHourPairs.length; i += 3) {
            LocalDate date = (LocalDate) dayAndHourPairs[i];
            int startHour = (Integer) dayAndHourPairs[i + 1];
            int count = (Integer) dayAndHourPairs[i + 2];
            boolean rainyAndWindy = date.equals(LocalDate.of(2026, 11, 23));
            for (int h = 0; h < count; h++) {
                WeatherHour hour = new WeatherHour(
                        18.0,
                        date.atStartOfDay(ZONE).plusHours(startHour + h).toOffsetDateTime(),
                        rainyAndWindy ? 40.0 : 10.0,
                        0.0,
                        0);
                hours.add(hour);
            }
        }
        return hours;
    }
}
