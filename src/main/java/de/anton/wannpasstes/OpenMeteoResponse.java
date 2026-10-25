package de.anton.wannpasstes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(Hourly hourly) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hourly(
            List<Long> time,

            @JsonProperty("temperature_2m")
            List<Double> temperatureCelsius,

            @JsonProperty("wind_speed_10m")
            List<Double> windKmh,

            @JsonProperty("precipitation")
            List<Double> precipitationMm,

            @JsonProperty("weather_code")
            List<Integer> weatherCode
    ) {
    }
}
