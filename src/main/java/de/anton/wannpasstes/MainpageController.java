package de.anton.wannpasstes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.RestClientException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
    public String mainpage(@Valid @ModelAttribute("selection") PlanningSelection selection,
                           HttpServletResponse response, Model model) {
        model.addAttribute("suitableWindows", List.of());
        model.addAttribute("locationName", "Ausgewählter Ort");
        LocalDate start = selection.startDate();
        LocalDate end = selection.endDate();

        if (start == null && end == null) {
            start = selection.searchDate();
            end = selection.searchDate();
        }

        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        try {
            GeocodingResponse.Location location = geocodingClient.getLocation(selection.locationId());
            model.addAttribute("locationName", location.name());
            if (location.timezone() == null) {
                throw new RestClientException("Geocoding API returned no timezone");
            }
            ZoneId zone = ZoneId.of(location.timezone());
            OffsetDateTime now = OffsetDateTime.now(zone);
            LocalDate today = now.toLocalDate();
            LocalDate lastDate = today.plusDays(6);
            start = start == null ? today : start;
            end = end == null ? start : end;

            model.addAttribute("minSearchDate", today);
            model.addAttribute("maxSearchDate", lastDate);
            model.addAttribute("startDate", start);
            model.addAttribute("endDate", end);

            if (start.isBefore(today) || end.isAfter(lastDate) || end.isBefore(start)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                model.addAttribute("dateError", "Bitte wähle einen Zeitraum von heute bis in sechs Tagen. "
                        + "Das Enddatum darf nicht vor dem Startdatum liegen.");
                return "mainpage";
            }

            OffsetDateTime rangeStart = start.atStartOfDay(zone).toOffsetDateTime();
            OffsetDateTime rangeEnd = end.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
            List<WeatherHour> hours = weatherClient.fetchForecast(location.latitude(), location.longitude(), zone)
                    .stream()
                    .filter(hour -> !hour.getTime().isBefore(now)
                            && !hour.getTime().isBefore(rangeStart) && !hour.getTime().isAfter(rangeEnd))
                    .toList();

            List<TimeWindow> suitableWindows = timeWindowService.findSuitableWindows(hours,
                            selection.temperaturePreference(), selection.windPreference(), selection.precipitationPreference())
                    .stream()
                    .filter(window -> window.durationMinutes() >= selection.minDurationMinutes())
                    .toList();
            model.addAttribute("suitableWindows", suitableWindows);
        } catch (RestClientException | DateTimeException exception) {
            model.addAttribute("weatherError", "Der Ort oder die Wetterdaten konnten nicht geladen werden. "
                    + "Bitte versuche es später erneut.");
        }

        return "mainpage";
    }
}
