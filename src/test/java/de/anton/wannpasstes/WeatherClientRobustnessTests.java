package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherClientRobustnessTests {

    private final RestClient.Builder builder = RestClient.builder()
            .baseUrl("https://api.open-meteo.com");
    private final MockRestServiceServer server =
            MockRestServiceServer.bindTo(builder).build();
    private final WeatherClient client = new WeatherClient(builder.build());

    @Test
    void skipsHoursWithMissingValuesInsteadOfFailing() {
        String json = """
                {
                  "hourly": {
                    "time": [%d, %d, %d],
                    "temperature_2m": [18.0, null, 19.0],
                    "wind_speed_10m": [12.0, 12.0, 12.0],
                    "precipitation": [0.0, 0.0, 0.0],
                    "weather_code": [3, 3, 3]
                  }
                }
                """.formatted(
                epoch("2026-11-22T10:00:00+01:00"),
                epoch("2026-11-22T11:00:00+01:00"),
                epoch("2026-11-22T12:00:00+01:00"));

        server.expect(queryParam("forecast_days", "7"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<WeatherHour> hours = client.fetchForecast(52.5, 13.4,
                ZoneId.of("Europe/Berlin"));

        assertEquals(2, hours.size());
        assertEquals(18.0, hours.get(0).getTemperatureCelsius());
        assertEquals(19.0, hours.get(1).getTemperatureCelsius());
        server.verify();
    }

    @Test
    void stopsAtTheShortestValueList() {
        String json = """
                {
                  "hourly": {
                    "time": [%d, %d, %d],
                    "temperature_2m": [18.0, 18.5],
                    "wind_speed_10m": [12.0, 12.0, 12.0],
                    "precipitation": [0.0, 0.0, 0.0],
                    "weather_code": [3, 3, 3]
                  }
                }
                """.formatted(
                epoch("2026-11-22T10:00:00+01:00"),
                epoch("2026-11-22T11:00:00+01:00"),
                epoch("2026-11-22T12:00:00+01:00"));

        server.expect(queryParam("forecast_days", "7"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        assertEquals(2, client.fetchForecast(52.5, 13.4,
                ZoneId.of("Europe/Berlin")).size());
        server.verify();
    }

    @Test
    void toleratesCompletelyMissingLists() {
        String json = """
                {
                  "hourly": {
                    "time": [%d],
                    "temperature_2m": null
                  }
                }
                """.formatted(epoch("2026-11-22T10:00:00+01:00"));

        server.expect(queryParam("forecast_days", "7"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        assertTrue(client.fetchForecast(52.5, 13.4,
                ZoneId.of("Europe/Berlin")).isEmpty());
        server.verify();
    }

    @Test
    void toleratesAnEmptyHourlyResponse() {
        String json = """
                { "hourly": { "time": [], "temperature_2m": [], "wind_speed_10m": [],
                  "precipitation": [], "weather_code": [] } }
                """;

        server.expect(queryParam("forecast_days", "7"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        assertTrue(client.fetchForecast(52.5, 13.4,
                ZoneId.of("Europe/Berlin")).isEmpty());
        server.verify();
    }

    private long epoch(String isoDateTime) {
        return java.time.OffsetDateTime.parse(isoDateTime).toEpochSecond();
    }
}
