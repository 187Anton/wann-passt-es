package de.anton.wannpasstes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Controller
public class LocationSearchController {

    private final GeocodingClient geocodingClient;

    public LocationSearchController(GeocodingClient geocodingClient) {
        this.geocodingClient = geocodingClient;
    }


    @GetMapping("/locations")
    public String locations(@RequestParam(name = "query", defaultValue = "") String query, Model model) {

        String searchTerm = query.strip();
        boolean searchPerformed = searchTerm.length() > 2;
        List<GeocodingResponse.Location> locations = List.of();

        if (searchPerformed) {
            try {
                locations = geocodingClient.search(searchTerm);
            } catch (RestClientException exception) { model.addAttribute("locationError", "Die Ortssuche ist gerade nicht verfügbar. " + "Bitte versuche es später noch einmal.");
            }
        }

        model.addAttribute("locations", locations);
        model.addAttribute("searchPerformed", searchPerformed);
        model.addAttribute("query", searchTerm);
        return "locations";
    }
}
