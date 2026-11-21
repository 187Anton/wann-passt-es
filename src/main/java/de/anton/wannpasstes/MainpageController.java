package de.anton.wannpasstes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
public class MainpageController {

    static final int DEFAULT_LOCATION_ID = 2950159;

    private static final DateTimeFormatter GERMAN_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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

    /**
     * Single-day search with the legacy {@code searchDate} parameter.
     */
    public String mainpage(
            int locationId,
            int minDurationMinutes,
            PrecipitationPreference precipitationPreference,
            TemperaturePreference temperaturePreference,
            WindPreference windPreference,
            Activity activity,
            LocalDate searchDate,
            Model model) {

        return mainpage(locationId, minDurationMinutes, precipitationPreference, temperaturePreference,
                windPreference, activity, searchDate, null, null, model, null);
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
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            Model model,
            HttpServletResponse response) {

        List<TimeWindow> suitableWindows = List.of();
        String locationName = "den ausgewählten Ort";
        String locationDetails = "";
        boolean searchPerformed = false;
        String selectedStart = startDate != null ? startDate.toString()
                : searchDate != null && endDate == null ? searchDate.toString() : "";
        String selectedEnd = endDate != null ? endDate.toString()
                : searchDate != null && startDate == null ? searchDate.toString() : "";

        try {
            GeocodingResponse.Location location =
                    geocodingClient.getLocation(locationId);

            locationName = location.name() == null ? "Ort " + locationId : location.name();
            locationDetails = joinNonBlank(location.region(), location.country());
            ZoneId zone = zoneOf(location);

            OffsetDateTime now = OffsetDateTime.now(zone);
            LocalDate today = now.toLocalDate();
            LocalDate lastDate = SearchPeriod.lastForecastDate(today);

            SearchPeriod period = SearchPeriod.resolve(searchDate, startDate, endDate, today);
            selectedStart = period.startDate().toString();
            selectedEnd = period.endDate().toString();

            model.addAttribute("minSearchDate", today.toString());
            model.addAttribute("maxSearchDate", lastDate.toString());

            Optional<SearchPeriod.Problem> problem = period.validate(today);

            if (problem.isPresent()) {
                model.addAttribute("dateError", dateErrorMessage(problem.get(), today, lastDate));
                setStatus(response, HttpServletResponse.SC_BAD_REQUEST);
            } else {
                List<WeatherHour> hours = weatherClient.fetchForecast(
                        location.latitude(),
                        location.longitude(),
                        zone);

                OffsetDateTime from = now.isAfter(period.startAt(zone)) ? now : period.startAt(zone);
                OffsetDateTime to = period.endAt(zone);

                boolean hasDataInPeriod = hours.stream()
                        .anyMatch(hour -> !hour.getTime().isBefore(from) && !hour.getTime().isAfter(to));

                suitableWindows = timeWindowService.findSuitableWindows(
                        hours, from, to,
                        temperaturePreference, windPreference, precipitationPreference,
                        minDurationMinutes);

                if (!hasDataInPeriod) {
                    model.addAttribute("weatherNotice",
                            "Für den gewählten Zeitraum liegen keine verwertbaren Wetterdaten vor. "
                                    + "Bitte versuche es später erneut oder wähle einen anderen Zeitraum.");
                }

                model.addAttribute("result", ResultView.of(suitableWindows, period, zone, now));
                model.addAttribute("periodLabel", periodLabel(period));
                searchPerformed = true;
            }

        } catch (RestClientException | DateTimeException exception) {
            model.addAttribute(
                    "weatherError",
                    "Der Ort oder die Wetterdaten konnten nicht geladen werden. "
                            + "Bitte versuche es später erneut.");
            setStatus(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }

        model.addAttribute("suitableWindows", suitableWindows);
        model.addAttribute("searchPerformed", searchPerformed);
        model.addAttribute("minDurationMinutes", minDurationMinutes);
        model.addAttribute("locationId", locationId);
        model.addAttribute("locationName", locationName);
        model.addAttribute("locationDetails", locationDetails);
        model.addAttribute("precipitationPreference", precipitationPreference);
        model.addAttribute("precipitationPreferences", PrecipitationPreference.values());
        model.addAttribute("temperaturePreference", temperaturePreference);
        model.addAttribute("temperaturePreferences", TemperaturePreference.values());
        model.addAttribute("windPreference", windPreference);
        model.addAttribute("windPreferences", WindPreference.values());
        model.addAttribute("activity", activity);
        model.addAttribute("activities", Activity.values());
        model.addAttribute("startDate", selectedStart);
        model.addAttribute("endDate", selectedEnd);

        return "mainpage";
    }

    private static ZoneId zoneOf(GeocodingResponse.Location location) {
        if (location.timezone() == null || location.timezone().isBlank()) {
            throw new DateTimeException("Location has no time zone");
        }
        return ZoneId.of(location.timezone());
    }

    private static String dateErrorMessage(SearchPeriod.Problem problem, LocalDate today, LocalDate lastDate) {
        return switch (problem) {
            case END_BEFORE_START -> "Das Enddatum liegt vor dem Startdatum. "
                    + "Bitte wähle ein Enddatum, das gleich oder später als das Startdatum ist.";
            case OUTSIDE_FORECAST -> "Bitte wähle Daten zwischen dem " + GERMAN_DATE.format(today)
                    + " und dem " + GERMAN_DATE.format(lastDate)
                    + ". Für andere Tage liegt keine Vorhersage vor.";
        };
    }

    private static String periodLabel(SearchPeriod period) {
        if (period.isSingleDay()) {
            return "am " + GERMAN_DATE.format(period.startDate());
        }
        return "vom " + GERMAN_DATE.format(period.startDate()) + " bis " + GERMAN_DATE.format(period.endDate());
    }

    private static String joinNonBlank(String... parts) {
        return String.join(", ", java.util.Arrays.stream(parts)
                .filter(part -> part != null && !part.isBlank())
                .toList());
    }

    private static void setStatus(HttpServletResponse response, int status) {
        if (response != null) {
            response.setStatus(status);
        }
    }
}
