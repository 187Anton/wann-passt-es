package de.anton.wannpasstes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LocationSearchController {

    private final GeocodingClient geocodingClient;

    public LocationSearchController(GeocodingClient geocodingClient) {
        this.geocodingClient = geocodingClient;
    }


    @GetMapping("/locations")
    public String locations(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "activity", required = false) Activity activity,
            @RequestParam(name = "minDurationMinutes", required = false) Integer minDurationMinutes,
            @RequestParam(name = "temperaturePreference", required = false)
            TemperaturePreference temperaturePreference,
            @RequestParam(name = "windPreference", required = false)
            WindPreference windPreference,
            @RequestParam(name = "precipitationPreference", required = false)
            PrecipitationPreference precipitationPreference,
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate,
            @RequestParam(name = "profileId", required = false) Long profileId,
            Model model) {

        String searchTerm = query.strip();
        boolean searchPerformed = searchTerm.length() >= 2;
        List<GeocodingResponse.Location> found = List.of();

        if (searchPerformed) {
            try {
                found = geocodingClient.search(searchTerm);
            } catch (RestClientException exception) {
                model.addAttribute("locationError", "Die Ortssuche ist gerade nicht verfügbar. "
                        + "Bitte versuche es später noch einmal.");
            }
        }

        // Each hit links back to the page the search was started from, so the
        // current planning selection survives the location change.
        List<LocationOption> options = new ArrayList<>();
        for (GeocodingResponse.Location location : found == null ? List.<GeocodingResponse.Location>of() : found) {
            options.add(new LocationOption(location, returnUrlFor(location, profileId,
                    activity, minDurationMinutes, temperaturePreference,
                    windPreference, precipitationPreference, startDate, endDate)));
        }

        model.addAttribute("locations", options);
        model.addAttribute("searchPerformed", searchPerformed);
        model.addAttribute("query", searchTerm);
        model.addAttribute("activity", activity);
        model.addAttribute("minDurationMinutes", minDurationMinutes);
        model.addAttribute("temperaturePreference", temperaturePreference);
        model.addAttribute("windPreference", windPreference);
        model.addAttribute("precipitationPreference", precipitationPreference);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("profileId", profileId);
        return "locations";
    }

    private String returnUrlFor(
            GeocodingResponse.Location location,
            Long profileId,
            Activity activity,
            Integer minDurationMinutes,
            TemperaturePreference temperaturePreference,
            WindPreference windPreference,
            PrecipitationPreference precipitationPreference,
            LocalDate startDate,
            LocalDate endDate) {

        if (profileId != null) {
            return UriComponentsBuilder.fromPath("/profiles/{id}/edit")
                    .queryParam("locationId", location.id())
                    .queryParam("locationName", location.name())
                    .buildAndExpand(profileId)
                    .encode()
                    .toUriString();
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/")
                .queryParam("locationId", location.id());

        if (activity != null) {
            builder.queryParam("activity", activity.name());
        }
        if (minDurationMinutes != null) {
            builder.queryParam("minDurationMinutes", minDurationMinutes);
        }
        if (temperaturePreference != null) {
            builder.queryParam("temperaturePreference", temperaturePreference.name());
        }
        if (windPreference != null) {
            builder.queryParam("windPreference", windPreference.name());
        }
        if (precipitationPreference != null) {
            builder.queryParam("precipitationPreference", precipitationPreference.name());
        }
        if (startDate != null) {
            builder.queryParam("startDate", startDate.toString());
        }
        if (endDate != null) {
            builder.queryParam("endDate", endDate.toString());
        }

        return builder.encode().build().toUriString();
    }

    /** A search hit together with the link that hands this location back. */
    public record LocationOption(GeocodingResponse.Location location, String url) {
    }
}
