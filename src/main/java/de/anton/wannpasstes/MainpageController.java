package de.anton.wannpasstes;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.OffsetDateTime;

@Controller
public class MainpageController {

        @GetMapping("/")
    public String mainpage(Model model) {
            WeatherHour hour = new WeatherHour(18.0, OffsetDateTime.parse("2026-10-25T14:00:00+01:00"), 12.0, 0.0, 0);
            boolean suitable = hour.matches(10.0, 25.0, 20.0, 0.2);

            model.addAttribute("weatherHour", hour);
            model.addAttribute("suitable", suitable);
            return "mainpage";
        }
}
