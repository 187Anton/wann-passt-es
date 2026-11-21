package de.anton.wannpasstes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /** Marker for a missing weather code; the code is loaded but not evaluated. */
    static final int UNKNOWN_WEATHER_CODE = -1;

    private static final Logger log = LoggerFactory.getLogger(WeatherClient.class);

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
        return toWeatherHours(response.hourly(), zone);
    }

    /**
     * Converts the column-oriented hourly data into weather hours.
     * Hours with a missing time, temperature, wind or precipitation value are
     * skipped instead of failing the whole request. Lists of different length
     * are tolerated: indexes beyond a list's end count as missing values.
     */
    static List<WeatherHour> toWeatherHours(OpenMeteoResponse.Hourly hourly, ZoneId zone) {
        List<Long> times = hourly.time();
        if (times == null) {
            log.warn("Weather API response contains no time list");
            return List.of();
        }

        List<WeatherHour> hours = new ArrayList<>();
        int skipped = 0;

        for (int i = 0; i < times.size(); i++) {
            Long epochSecond = times.get(i);
            Double temperature = valueAt(hourly.temperatureCelsius(), i);
            Double wind = valueAt(hourly.windKmh(), i);
            Double precipitation = valueAt(hourly.precipitationMm(), i);

            if (epochSecond == null || !isUsable(temperature) || !isUsable(wind) || !isUsable(precipitation)) {
                skipped++;
                continue;
            }

            OffsetDateTime time = Instant
                    .ofEpochSecond(epochSecond)  //Eindeutiger Zeitpunkt
                    .atZone(zone) //Ortszeit einschließlich Zeitumstellung
                    .toOffsetDateTime(); //Typ des WeatherHour Feldes

            Integer weatherCode = valueAt(hourly.weatherCode(), i);

            hours.add(new WeatherHour(
                    temperature,
                    time,
                    wind,
                    precipitation,
                    weatherCode == null ? UNKNOWN_WEATHER_CODE : weatherCode));
        }

        if (skipped > 0) {
            log.warn("Skipped {} of {} hourly weather entries with missing values", skipped, times.size());
        }
        return hours;
    }

    private static <T> T valueAt(List<T> values, int index) {
        if (values == null || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    private static boolean isUsable(Double value) {
        return value != null && Double.isFinite(value);
    }

}
