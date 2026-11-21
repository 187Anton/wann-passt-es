package de.anton.wannpasstes;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    public String locations(@RequestParam(name = "query", defaultValue = "") String query,
                            @Valid @ModelAttribute("selection") PlanningSelection selection,
                            Model model) {
        String searchTerm = query.strip();
        List<GeocodingResponse.Location> locations = List.of();

        if (searchTerm.length() >= 2) {
            try {
                locations = geocodingClient.search(searchTerm);
            } catch (RestClientException exception) {
                model.addAttribute("locationError", "Die Ortssuche ist gerade nicht verfügbar. "
                        + "Bitte versuche es später noch einmal.");
            }
        } else if (!query.isEmpty()) {
            model.addAttribute("locationError", "Bitte gib mindestens zwei Zeichen ein.");
        }

        model.addAttribute("locations", locations);
        model.addAttribute("selectionParameters", selection.parameters());
        model.addAttribute("searchPerformed", searchTerm.length() >= 2);
        model.addAttribute("query", searchTerm);
        return "locations";
    }
}
