package de.anton.wannpasstes;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class WeatherClient {

    private final RestClient restClient;

    public WeatherClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();


        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        restClient = RestClient.builder()
                .baseUrl("https://api.open-meteo.com")
                .requestFactory(requestFactory)
                .build();
    }
    WeatherClient(RestClient restclient) {
        this.restClient = restclient; }

    public List<WeatherHour> fetchForecast(
            double latitude, double longitude, ZoneId zone) {

        OpenMeteoResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("hourly", "temperature_2m,wind_speed_10m," + "precipitation,weather_code")                        .queryParam("temperature_unit", "celsius")
                        .queryParam("wind_speed_unit", "kmh")
                        .queryParam("precipitation_unit", "mm")
                        .queryParam("timeformat", "unixtime")
                        .queryParam("timezone", zone.getId())
                        .queryParam("forecast_days", 7)
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);

        if (response == null ||response.hourly() == null) {
            throw new RestClientException("Weather API returned no hourly data");
        }
        OpenMeteoResponse.Hourly hourly = response.hourly();
        List<WeatherHour> hours = new ArrayList<>();

        for (int i = 0; i < hourly.time().size(); i++) {
            OffsetDateTime time = Instant
                    .ofEpochSecond(hourly.time().get(i))  //Eindeutiger Zeitpunkt
                    .atZone(zone) //Ortszeit einschließlich Zeitumstellung
                    .toOffsetDateTime(); //Typ des WeatherHour Feldes

            WeatherHour hour = new WeatherHour(
                    hourly.temperatureCelsius().get(i),
                    time,
                    hourly.windKmh().get(i),
                    hourly.precipitationMm().get(i),
                    hourly.weatherCode().get(i));

            hours.add(hour);
        }
        return hours;

    }

}
