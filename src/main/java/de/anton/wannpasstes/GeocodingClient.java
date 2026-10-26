package de.anton.wannpasstes;


import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.time.Duration;

@Component
public class GeocodingClient {

    private final RestClient restClient;

    public GeocodingClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        restClient = RestClient.builder().baseUrl("https://geocoding-api.open-meteo.com").requestFactory(requestFactory).build();
    }

    GeocodingClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<GeocodingResponse.Location> search(String name) {
        if (name == null || name.strip().length() < 2) {
            return List.of();
    }

    GeocodingResponse response =restClient.get().uri(uriBuilder -> uriBuilder.path("/v1/search").queryParam("name", name.strip()).queryParam("count", 5).queryParam("language", "de").queryParam("format", "json").build()).retrieve().body(GeocodingResponse.class);

    if (response == null) {
        throw new RestClientException("Geocoding API returned an empty response");

    }
    return response.results();
    }
}
