package de.anton.wannpasstes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestClientException;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainpageController {

    private final WeatherClient weatherClient;

    public MainpageController(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    @GetMapping("/")
    public String mainpage(Model model) {
        List<WeatherHour> suitableHours = new ArrayList<>();

        try {
            List<WeatherHour> hours = weatherClient.fetchForecast(
                    52.52,
                    13.405,
                    ZoneId.of("Europe/Berlin"));

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