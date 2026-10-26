package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeocodingClientTests {

    @Test
    void convertsSearchResponseToLocations() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://geocoding-api.open-meteo.com");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        GeocodingClient client = new GeocodingClient(builder.build());

        String json = """
                {
                  "results": [
                    {
                      "id": 123,
                      "name": "Testort",
                      "latitude": 52.5,
                      "longitude": 13.4,
                      "timezone": "Europe/Berlin",
                      "country": "Deutschland",
                      "admin1": "Testregion"
                    }
                  ]
                }
                """;

        server.expect(queryParam("name", "Testort"))
                .andExpect(queryParam("language", "de"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<GeocodingResponse.Location> results =
                client.search(" Testort ");

        GeocodingResponse.Location expected =
                new GeocodingResponse.Location(
                        123,
                        "Testort",
                        52.5,
                        13.4,
                        "Europe/Berlin",
                        "Deutschland",
                        "Testregion");

        assertEquals(List.of(expected), results);

        server.verify();
    }

    @Test
    void loadsLocationById() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://geocoding-api.open-meteo.com");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        GeocodingClient client = new GeocodingClient(builder.build());

        String json = """
            {
              "id": 123,
              "name": "Testort",
              "latitude": 52.5,
              "longitude": 13.4,
              "timezone": "Europe/Berlin",
              "country": "Deutschland",
              "admin1": "Testregion"
            }
            """;

        server.expect(queryParam("id", "123"))
                .andExpect(queryParam("language", "de"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        GeocodingResponse.Location result = client.getLocation(123);

        GeocodingResponse.Location expected =
                new GeocodingResponse.Location(
                        123,
                        "Testort",
                        52.5,
                        13.4,
                        "Europe/Berlin",
                        "Deutschland",
                        "Testregion");

        assertEquals(expected, result);
        server.verify();
    }
}