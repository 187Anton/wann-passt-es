package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherClientDataTests {

    @Test
    void skipsMalformedRowsWithoutMisaligningTheRemainingHours() {
        WeatherClient client = clientFor("""
                {"hourly":{
                  "time":[1760000400,1760004000,null,1760011200,1760014800,1760018400,1760022000],
                  "temperature_2m":[18,null,19,20,21,22,23],
                  "wind_speed_10m":[10,10,10,11,null,12],
                  "precipitation":[0,0,0,0.1,0,0],
                  "weather_code":[3]
                }}
                """);

        var hours = client.fetchForecast(52.5, 13.4, ZoneOffset.UTC);
        assertEquals(3, hours.size());
        assertEquals(3, hours.getFirst().getWeatherCode());
        assertEquals(1760011200L, hours.get(1).getTime().toEpochSecond());
        assertEquals(20, hours.get(1).getTemperatureCelsius());
        assertEquals(0.1, hours.get(1).getPrecipitationMm());
        assertEquals(-1, hours.get(1).getWeatherCode());
        assertEquals(22, hours.getLast().getTemperatureCelsius());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"hourly\":{}}", "{\"hourly\":{\"time\":[1760000400]}}"})
    void reportsAnUnusableForecastWhenARequiredListIsMissing(String json) {
        assertThrows(RestClientException.class, () -> clientFor(json).fetchForecast(52.5, 13.4, ZoneOffset.UTC));
    }

    private WeatherClient clientFor(String json) {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://weather.example");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(queryParam("forecast_days", "7")).andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
        return new WeatherClient(builder.build());
    }
}
