package de.anton.wannpasstes;

import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainpageController {

    private final WeatherClient weatherClient;
    private final TimeWindowService timeWindowService;
    private final GeocodingClient geocodingClient;

    public MainpageController(
            WeatherClient weatherClient,
            TimeWindowService timeWindowService,
            GeocodingClient geocodingClient) {

        this.weatherClient = weatherClient;
        this.timeWindowService = timeWindowService;
        this.geocodingClient = geocodingClient;
    }

    @GetMapping("/")
    public String mainpage(
            @RequestParam(name = "locationId", defaultValue = "2950159")
            @Min(1) int locationId,
            @RequestParam(name = "minDurationMinutes", defaultValue = "60")
            @Min(1) int minDurationMinutes,
            Model model) {

        List<TimeWindow> suitableWindows = List.of();
        String locationName = "den ausgewählten Ort";

        try {
            GeocodingResponse.Location location =
                    geocodingClient.getLocation(locationId);

            locationName = location.name();
            ZoneId zone = ZoneId.of(location.timezone());

            List<WeatherHour> hours = new ArrayList<>(
                    weatherClient.fetchForecast(
                            location.latitude(),
                            location.longitude(),
                            zone));

            OffsetDateTime now = OffsetDateTime.now(zone);
            hours.removeIf(hour -> hour.getTime().isBefore(now));

            suitableWindows = new ArrayList<>(
                    timeWindowService.findSuitableWindows(
                            hours, 10.0, 25.0, 20.0, PrecipitationPreference.LIGHT_DRIZZLE));

            suitableWindows.removeIf(
                    window -> window.durationMinutes() < minDurationMinutes);

        } catch (RestClientException exception) {
            model.addAttribute(
                    "weatherError",
                    "Der Ort oder die Wetterdaten konnten nicht geladen werden. "
                            + "Bitte versuche es später erneut.");
        }

        model.addAttribute("suitableWindows", suitableWindows);
        model.addAttribute("minDurationMinutes", minDurationMinutes);
        model.addAttribute("locationId", locationId);
        model.addAttribute("locationName", locationName);

        return "mainpage";
    }
}