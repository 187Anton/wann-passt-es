package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class WeatherClientFailureTests {

    @Test
    void propagatesServiceUnavailableWithoutCallingRealWeatherApi() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://weather.example");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        WeatherClient client = new WeatherClient(builder.build());

        server.expect(queryParam("forecast_days", "7"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThrows(HttpServerErrorException.ServiceUnavailable.class,
                () -> client.fetchForecast(52.5, 13.4, ZoneId.of("Europe/Berlin")));
        server.verify();
    }
}
