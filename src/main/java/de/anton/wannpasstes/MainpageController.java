package de.anton.wannpasstes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.ArrayList;

import java.time.OffsetDateTime;

@Controller
public class MainpageController {

        @GetMapping("/")
    public String mainpage(Model model) {
            OffsetDateTime start =
                    OffsetDateTime.parse("2026-10-25T14:00:00+01:00");

            List<WeatherHour> hours = List.of(
                    new WeatherHour(18.0, start, 12.0, 0.0, 0),
                    new WeatherHour(19.0, start.plusHours(1), 25.0, 0.0, 0),
                    new WeatherHour(20.0, start.plusHours(2), 15.0, 0.0, 0),
                    new WeatherHour(17.0, start.plusHours(3), 10.0, 0.1, 0)
            );

            List<WeatherHour> suitableHours = new ArrayList<>();

            for (WeatherHour hour : hours) {
                if (hour.matches(10.0, 25.0, 20.0,0.2)) {
                    suitableHours.add(hour);
                }
            }
            model.addAttribute("suitableHours", suitableHours);

            return "mainpage";
        }
}
