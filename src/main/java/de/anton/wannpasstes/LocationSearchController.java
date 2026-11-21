package de.anton.wannpasstes;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Location search. The page optionally receives the current selection of the
 * planning page or of a profile being edited ("context") and passes it on, so
 * that choosing a location keeps activity, minimum duration, weather wishes
 * and period (or the unsaved profile form values).
 */
@Controller
public class LocationSearchController {

    private final GeocodingClient geocodingClient;

    public LocationSearchController(GeocodingClient geocodingClient) {
        this.geocodingClient = geocodingClient;
    }

    /** A search result together with the link that selects it. */
    public record LocationChoice(GeocodingResponse.Location location, String details, String href) {
    }

    @GetMapping("/locations")
    public String locations(
            @RequestParam(name = "query", defaultValue = "") String query,
            @RequestParam(name = "locationId", required = false) Integer locationId,
            @RequestParam(name = "locationName", required = false) String locationName,
            @RequestParam(name = "activity", required = false) Activity activity,
            @RequestParam(name = "minDurationMinutes", required = false) Integer minDurationMinutes,
            @RequestParam(name = "temperaturePreference", required = false)
            TemperaturePreference temperaturePreference,
            @RequestParam(name = "windPreference", required = false) WindPreference windPreference,
            @RequestParam(name = "precipitationPreference", required = false)
            PrecipitationPreference precipitationPreference,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "profileId", required = false) Long profileId,
            @RequestParam(name = "profileName", required = false) String profileName,
            Model model) {

        boolean editingProfile = profileId != null && profileId > 0;

        // Selection that is carried through the search, without the location itself.
        Map<String, String> context = new LinkedHashMap<>();
        if (editingProfile) {
            put(context, "profileId", profileId);
            put(context, "profileName", profileName);
        }
        put(context, "activity", activity == null ? null : activity.name());
        put(context, "minDurationMinutes", minDurationMinutes);
        put(context, "temperaturePreference", temperaturePreference == null ? null : temperaturePreference.name());
        put(context, "windPreference", windPreference == null ? null : windPreference.name());
        put(context, "precipitationPreference",
                precipitationPreference == null ? null : precipitationPreference.name());
        if (!editingProfile) {
            put(context, "startDate", startDate);
            put(context, "endDate", endDate);
        }

        String targetPath = editingProfile ? "/profiles/" + profileId + "/edit" : "/";

        String searchTerm = query.strip();
        boolean searchPerformed = searchTerm.length() >= 2;
        List<GeocodingResponse.Location> locations = List.of();

        if (searchPerformed) {
            try {
                locations = geocodingClient.search(searchTerm);
            } catch (RestClientException exception) { model.addAttribute("locationError", "Die Ortssuche ist gerade nicht verfügbar. " + "Bitte versuche es später noch einmal.");
            }
        }

        List<LocationChoice> choices = locations.stream()
                .map(location -> new LocationChoice(location,
                        details(location),
                        link(targetPath, context, location.id(), editingProfile ? location.name() : null)))
                .toList();

        // Hidden fields of the search form: context plus the current location for the back link.
        Map<String, String> hiddenFields = new LinkedHashMap<>(context);
        put(hiddenFields, "locationId", locationId);
        put(hiddenFields, "locationName", locationName);

        model.addAttribute("locations", locations);
        model.addAttribute("choices", choices);
        model.addAttribute("searchPerformed", searchPerformed);
        model.addAttribute("query", searchTerm);
        model.addAttribute("hiddenFields", hiddenFields);
        model.addAttribute("editingProfile", editingProfile);
        model.addAttribute("hasContext", !context.isEmpty() || locationId != null);
        model.addAttribute("backHref", link(targetPath, context, locationId, editingProfile ? locationName : null));
        return "locations";
    }

    private static String details(GeocodingResponse.Location location) {
        return java.util.stream.Stream.of(location.region(), location.country())
                .filter(part -> part != null && !part.isBlank())
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private static void put(Map<String, String> map, String name, Object value) {
        if (value != null && !value.toString().isBlank()) {
            map.put(name, value.toString());
        }
    }

    private static String link(String path, Map<String, String> context, Integer locationId, String locationName) {
        Map<String, String> params = new LinkedHashMap<>();
        put(params, "locationId", locationId);
        put(params, "locationName", locationName);
        context.forEach((name, value) -> {
            if (!"profileId".equals(name)) {
                params.put(name, value);
            }
        });

        // Values are passed as URI variables so that reserved characters such as
        // "+" or "&" in user input are encoded strictly.
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
        List<String> values = new java.util.ArrayList<>();
        params.forEach((name, value) -> {
            builder.queryParam(name, "{v" + values.size() + "}");
            values.add(value);
        });
        return builder.encode().buildAndExpand(values.toArray()).toUriString();
    }
}
