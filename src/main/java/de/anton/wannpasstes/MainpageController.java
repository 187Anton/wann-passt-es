package de.anton.wannpasstes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClientException;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.time.OffsetDateTime;

@Controller
public class MainpageController {

    private final WeatherClient weatherClient;
    private final TimeWindowService timeWindowService;

    public MainpageController(WeatherClient weatherClient, TimeWindowService timeWindowService) {
        this.weatherClient = weatherClient;
        this.timeWindowService = timeWindowService;
    }

    @GetMapping("/")
    public String mainpage(Model model) {
        List<TimeWindow> suitableWindows = List.of();
        ZoneId zone = ZoneId.of("Europe/Berlin");

        try {
            List<WeatherHour> suitableHours = new ArrayList<>(weatherClient.fetchForecast(52.52, 13.405, zone));

            OffsetDateTime now = OffsetDateTime.now(zone);
            hours.removeIf(hour -> hour.getTime().isBefore(now));

            suitableWindows = timeWindowService.findSuitableWindows(hours, 10.0,25.0,20.0,0.2)
            for (WeatherHour hour : hours) {
                if (hour.matches(10.0, 25.0, 20.0, 0.2)) {
                    suitableHours.add(hour);
                }
            }
        } catch (RestClientException exception) {
            model.addAttribute(
                    "weatherError",
                    "Die Wetterdaten konnten nicht geladen werden. " + "Bitte versuche es später erneut.");
        }

        model.addAttribute("suitableHours", suitableHours);

        return "mainpage";
    }
}