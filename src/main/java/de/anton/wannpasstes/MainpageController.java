package de.anton.wannpasstes;

import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

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
            @RequestParam(name = "precipitationPreference",
                    defaultValue = "LIGHT_DRIZZLE")
            PrecipitationPreference precipitationPreference,
            @RequestParam(name = "temperaturePreference",
                    defaultValue = "MILD")
            TemperaturePreference temperaturePreference,
            @RequestParam(name = "windPreference",
                    defaultValue = "WEAK")
            WindPreference windPreference,
            @RequestParam(name = "activity", defaultValue = "WALKING")
            Activity activity,
            @RequestParam(name = "searchDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate searchDate,
            Model model) {

        List<TimeWindow> suitableWindows = List.of();
        String locationName = "den ausgewählten Ort";
        LocalDate selectedDate = searchDate;

        try {
            GeocodingResponse.Location location =
                    geocodingClient.getLocation(locationId);

            locationName = location.name();
            ZoneId zone = ZoneId.of(location.timezone());

            OffsetDateTime now = OffsetDateTime.now(zone);
            LocalDate today = now.toLocalDate();
            LocalDate lastDate = today.plusDays(6);

            if (selectedDate == null) {
                selectedDate = today;
            }

            model.addAttribute("minSearchDate", today.toString());
            model.addAttribute("maxSearchDate", lastDate.toString());

            if (selectedDate.isBefore(today) || selectedDate.isAfter(lastDate)) {
                model.addAttribute("weatherError", "Bitte wähle ein Datum zwischen " + today + " und " + lastDate);
            } else {
                OffsetDateTime dayStart = selectedDate.atStartOfDay(zone).toOffsetDateTime();
                OffsetDateTime dayEnd = selectedDate.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

                List<WeatherHour> hours = new ArrayList<>(
                        weatherClient.fetchForecast(
                                location.latitude(),
                                location.longitude(),
                                zone));

                hours.removeIf(hour -> hour.getTime().isBefore(now) || hour.getTime().isAfter(dayEnd) || hour.getTime().isBefore(dayStart));

                suitableWindows = new ArrayList<>(
                        timeWindowService.findSuitableWindows(
                                hours, temperaturePreference, windPreference, precipitationPreference));

                suitableWindows.removeIf(window -> window.durationMinutes() < minDurationMinutes);
            }

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
        model.addAttribute("precipitationPreference", precipitationPreference);
        model.addAttribute("precipitationPreferences", PrecipitationPreference.values());
        model.addAttribute("temperaturePreference", temperaturePreference);
        model.addAttribute("temperaturePreferences", TemperaturePreference.values());
        model.addAttribute("windPreference", windPreference);
        model.addAttribute("windPreferences", WindPreference.values());
        model.addAttribute("activity", activity);
        model.addAttribute("activities", Activity.values());
        model.addAttribute("searchDate", selectedDate == null ? "" : selectedDate.toString());

        return "mainpage";
    }
}