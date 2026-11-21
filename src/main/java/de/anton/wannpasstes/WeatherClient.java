package de.anton.wannpasstes;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
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
                        .queryParam("hourly", "temperature_2m,wind_speed_10m,precipitation,weather_code")
                        .queryParam("temperature_unit", "celsius")
                        .queryParam("wind_speed_unit", "kmh")
                        .queryParam("precipitation_unit", "mm")
                        .queryParam("timeformat", "unixtime")
                        .queryParam("timezone", zone.getId())
                        .queryParam("forecast_days", 7)
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);

        if (response == null || response.hourly() == null) {
            throw new RestClientException("Weather API returned no hourly data");
        }

        OpenMeteoResponse.Hourly hourly = response.hourly();
        List<Long> times = hourly.time() == null ? List.of() : hourly.time();
        List<WeatherHour> hours = new ArrayList<>();

        // The API normally returns equally long lists. If they are not, the
        // entries we cannot fully resolve are skipped instead of failing the
        // whole request.
        for (int i = 0; i < times.size(); i++) {
            Long epochSecond = times.get(i); //Eindeutiger Zeitpunkt
            Double temperatureCelsius = valueAt(hourly.temperatureCelsius(), i);
            Double windKmh = valueAt(hourly.windKmh(), i);
            Double precipitationMm = valueAt(hourly.precipitationMm(), i);
            Integer weatherCode = valueAt(hourly.weatherCode(), i);

            if (epochSecond == null || temperatureCelsius == null || windKmh == null
                    || precipitationMm == null || weatherCode == null) {
                continue;
            }

            hours.add(new WeatherHour(
                    temperatureCelsius,
                    Instant.ofEpochSecond(epochSecond)
                            .atZone(zone) //Ortszeit einschließlich Zeitumstellung
                            .toOffsetDateTime(), //Typ des WeatherHour Feldes
                    windKmh,
                    precipitationMm,
                    weatherCode));
        }

        return hours;

    }

    private static <T> T valueAt(List<T> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

}
