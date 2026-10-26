package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherClientTests {

    @Test
    void convertsApiResponseToWeatherHours() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.open-meteo.com");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        WeatherClient client = new WeatherClient(builder.build());

        OffsetDateTime expectedTime =
                OffsetDateTime.parse("2026-10-25T14:00:00+01:00");

        String json = """
                {
                  "hourly": {
                    "time": [%d],
                    "temperature_2m": [18.0],
                    "wind_speed_10m": [12.0],
                    "precipitation": [0.2],
                    "weather_code": [3]
                  }
                }
                """.formatted(expectedTime.toEpochSecond());

        server.expect(queryParam(
                        "hourly",
                        "temperature_2m,wind_speed_10m,precipitation,weather_code"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<WeatherHour> hours = client.fetchForecast(
                52.52,
                13.405,
                ZoneId.of("Europe/Berlin"));

        server.expect(queryParam(
                        "hourly",
                        "temperature_2m,wind_speed_10m,precipitation,weather_code"))
                .andExpect(queryParam("forecast_days", "7"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        assertEquals(1, hours.size());

        WeatherHour hour = hours.get(0);

        assertEquals(expectedTime, hour.getTime());
        assertEquals(18.0, hour.getTemperatureCelsius());
        assertEquals(12.0, hour.getWindKmh());
        assertEquals(0.2, hour.getPrecipitationMm());
        assertEquals(3, hour.getWeatherCode());

        server.verify();
    }
}