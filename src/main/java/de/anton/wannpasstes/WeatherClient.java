package de.anton.wannpasstes;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        if (response == null || response.hourly() == null || response.hourly().time() == null) {
            throw new RestClientException("Weather API returned no hourly data");
        }

        OpenMeteoResponse.Hourly hourly = response.hourly();
        List<WeatherHour> hours = new ArrayList<>();
        Set<Long> timestamps = new HashSet<>();

        for (int i = 0; i < hourly.time().size(); i++) {
            Long timestamp = hourly.time().get(i);
            Double temperature = valueAt(hourly.temperatureCelsius(), i);
            Double wind = valueAt(hourly.windKmh(), i);
            Double precipitation = valueAt(hourly.precipitationMm(), i);
            if (timestamp == null || !isFinite(temperature) || !isFinite(wind) || !isFinite(precipitation)
                    || !timestamps.add(timestamp)) {
                continue;
            }

            try {
                OffsetDateTime time = Instant.ofEpochSecond(timestamp).atZone(zone).toOffsetDateTime();
                Integer weatherCode = valueAt(hourly.weatherCode(), i);
                hours.add(new WeatherHour(temperature, time, wind, precipitation, weatherCode == null ? -1 : weatherCode));
            } catch (DateTimeException exception) {
                // An invalid timestamp cannot represent a forecast hour.
            }
        }

        if (hours.isEmpty()) {
            throw new RestClientException("Weather API returned no usable forecast hours");
        }
        return hours;
    }

    private static <T> T valueAt(List<T> values, int index) {
        return values == null || index >= values.size() ? null : values.get(index);
    }

    private static boolean isFinite(Double value) {
        return value != null && Double.isFinite(value);
    }
}
