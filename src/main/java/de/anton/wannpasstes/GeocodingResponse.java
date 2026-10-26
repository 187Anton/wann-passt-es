package de.anton.wannpasstes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingResponse(List<Location> results) {

    public GeocodingResponse {
        results = results == null ? List.of() : List.copyOf(results);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(int id, String name, double latitude, double longitude, String timezone, String country, @JsonProperty("admin1") String region) {
    }
}
