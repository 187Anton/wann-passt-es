package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Incomplete or inconsistent weather data must not crash the application (C3).
 */
class WeatherClientRobustnessTests {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final OffsetDateTime START = OffsetDateTime.parse("2026-11-22T10:00:00+01:00");

    private static long epoch(int hourOffset) {
        return START.plusHours(hourOffset).toEpochSecond();
    }

    @Test
    void toleratesListsOfDifferentLength() {
        OpenMeteoResponse.Hourly hourly = new OpenMeteoResponse.Hourly(
                List.of(epoch(0), epoch(1), epoch(2)),
                List.of(18.0, 19.0, 20.0),
                List.of(5.0, 6.0),          // one wind value missing
                List.of(0.0, 0.0, 0.0),
                List.of(1));                // weather codes are not evaluated

        List<WeatherHour> hours = WeatherClient.toWeatherHours(hourly, ZONE);

        assertEquals(2, hours.size());
        assertEquals(START.plusHours(1), hours.get(1).getTime());
        assertEquals(WeatherClient.UNKNOWN_WEATHER_CODE, hours.get(1).getWeatherCode());
    }

    @Test
    void skipsHoursWithNullValues() {
        OpenMeteoResponse.Hourly hourly = new OpenMeteoResponse.Hourly(
                Arrays.asList(epoch(0), null, epoch(2), epoch(3)),
                Arrays.asList(18.0, 19.0, null, 21.0),
                Arrays.asList(5.0, 5.0, 5.0, 5.0),
                Arrays.asList(0.0, 0.0, 0.0, null),
                null);

        List<WeatherHour> hours = WeatherClient.toWeatherHours(hourly, ZONE);

        assertEquals(1, hours.size());
        assertEquals(START, hours.getFirst().getTime());
    }

    @Test
    void returnsNoHoursWhenListsAreMissing() {
        assertEquals(List.of(), WeatherClient.toWeatherHours(
                new OpenMeteoResponse.Hourly(null, List.of(1.0), List.of(1.0), List.of(0.0), List.of(0)), ZONE));
        assertEquals(List.of(), WeatherClient.toWeatherHours(
                new OpenMeteoResponse.Hourly(List.of(epoch(0)), null, null, null, null), ZONE));
    }

    @Test
    void parsesIncompleteApiResponseWithoutException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.open-meteo.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        WeatherClient client = new WeatherClient(builder.build());

        String json = """
                {
                  "hourly": {
                    "time": [%d, %d, %d],
                    "temperature_2m": [18.0, null, 20.0],
                    "wind_speed_10m": [5.0, 5.0, 5.0, 7.0],
                    "precipitation": [0.0, 0.0, 0.1]
                  }
                }
                """.formatted(epoch(0), epoch(1), epoch(2));

        server.expect(queryParam("forecast_days", "7"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<WeatherHour> hours = client.fetchForecast(52.52, 13.405, ZONE);

        assertEquals(List.of(START, START.plusHours(2)), hours.stream().map(WeatherHour::getTime).toList());
        server.verify();
    }

    @Test
    void inconsistentDataLeadsToNoWindowsInsteadOfACrash() {
        OpenMeteoResponse.Hourly hourly = new OpenMeteoResponse.Hourly(
                List.of(epoch(0), epoch(1), epoch(2), epoch(3)),
                Arrays.asList(18.0, null, 18.0, 18.0),
                List.of(5.0, 5.0, 5.0, 5.0),
                List.of(0.0, 0.0, 0.0, 0.0),
                List.of());

        List<WeatherHour> hours = WeatherClient.toWeatherHours(hourly, ZONE);
        List<TimeWindow> windows = new TimeWindowService().findSuitableWindows(hours,
                TemperaturePreference.MILD, WindPreference.WEAK, PrecipitationPreference.DRY);

        // the hour with the missing temperature interrupts the window
        assertEquals(List.of(new TimeWindow(START.plusHours(2), START.plusHours(3))), windows);
    }
}
