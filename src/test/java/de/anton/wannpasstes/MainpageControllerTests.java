package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class MainpageControllerTests {

    @Test
    void keepsOnlyWindowsMeetingMinimumDuration() {
        WeatherClient weatherClient = mock(WeatherClient.class);
        GeocodingClient geocodingClient = mock(GeocodingClient.class);

        when(geocodingClient.getLocation(2950159))
                .thenReturn(new GeocodingResponse.Location(
                        2950159,
                        "Berlin",
                        52.52,
                        13.405,
                        "Europe/Berlin",
                        "Deutschland",
                        "Berlin"));

        MainpageController controller = new MainpageController(
                weatherClient,
                new TimeWindowService(),
                geocodingClient);

        ZoneId zone = ZoneId.of("Europe/Berlin");
        OffsetDateTime start = OffsetDateTime.now(zone).plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        List<WeatherHour> hours = List.of(
                new WeatherHour(18.0, start, 12.0, 0.0, 0),
                new WeatherHour(18.0, start.plusHours(1), 12.0, 0.0, 0),
                new WeatherHour(18.0, start.plusHours(2), 30.0, 0.0, 0),
                new WeatherHour(18.0, start.plusHours(3), 12.0, 0.0, 0),
                new WeatherHour(18.0, start.plusHours(4), 12.0, 0.0, 0),
                new WeatherHour(18.0, start.plusHours(5), 12.0, 0.0, 0)
        );

        when(weatherClient.fetchForecast(52.52, 13.405, zone))
                .thenReturn(hours);

        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.mainpage(2950159, 120, PrecipitationPreference.LIGHT_DRIZZLE, TemperaturePreference.MILD, WindPreference.WEAK, Activity.WALKING, start.toLocalDate(), model);

        TimeWindow expected = new TimeWindow(
                start.plusHours(3),
                start.plusHours(5));

        assertEquals("mainpage", view);
        assertEquals(List.of(expected), model.get("suitableWindows"));
        assertEquals(120, model.get("minDurationMinutes"));
    }
    @Test
    void usesCoordinatesAndTimezoneOfSelectedLocation() {
        WeatherClient weatherClient = mock(WeatherClient.class);
        GeocodingClient geocodingClient = mock(GeocodingClient.class);

        ZoneId zone = ZoneId.of("America/New_York");

        when(geocodingClient.getLocation(123))
                .thenReturn(new GeocodingResponse.Location(
                        123,
                        "Testort",
                        40.7,
                        -74.0,
                        zone.getId(),
                        "Testland",
                        "Testregion"));

        when(weatherClient.fetchForecast(40.7, -74.0, zone))
                .thenReturn(List.of());

        MainpageController controller = new MainpageController(
                weatherClient,
                new TimeWindowService(),
                geocodingClient);

        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.mainpage(123, 60, PrecipitationPreference.LIGHT_DRIZZLE, TemperaturePreference.MILD, WindPreference.WEAK, Activity.CYCLING, null, model);

        verify(weatherClient).fetchForecast(40.7, -74.0, zone);

        assertEquals("mainpage", view);
        assertEquals(123, model.get("locationId"));
        assertEquals("Testort", model.get("locationName"));
        assertEquals(TemperaturePreference.MILD, model.get("temperaturePreference"));
        assertEquals(WindPreference.WEAK, model.get("windPreference"));
        assertEquals(PrecipitationPreference.LIGHT_DRIZZLE, model.get("precipitationPreference"));
        assertEquals(Activity.CYCLING, model.get("activity"));
        assertEquals(List.of(Activity.values()), List.of((Activity[]) model.get("activities")));
    }
}