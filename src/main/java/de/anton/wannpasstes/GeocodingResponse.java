package de.anton.wannpasstes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingResponse(List<Location> results) {

    public GeocodingResponse {
        results = results == null ? List.of() : results.stream().filter(Objects::nonNull).filter(Location::hasName).toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(int id, String name, double latitude, double longitude, String timezone, String country, @JsonProperty("admin1") String region) {
        boolean hasName() {
            return id > 0 && name != null && !name.isBlank() && name.length() <= 255;
        }
    }
}
