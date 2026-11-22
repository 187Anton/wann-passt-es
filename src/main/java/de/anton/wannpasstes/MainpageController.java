package de.anton.wannpasstes;

import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class MainpageController {

    static final int FORECAST_DAYS = 7;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN);
    private static final String WEATHER_UNAVAILABLE =
            "Der Ort oder die Wetterdaten konnten nicht geladen werden. "
                    + "Bitte versuche es später erneut.";

    private final WeatherClient weatherClient;
    private final TimeWindowService timeWindowService;
    private final GeocodingClient geocodingClient;
    private final Clock clock;

    @Autowired
    public MainpageController(
            WeatherClient weatherClient,
            TimeWindowService timeWindowService,
            GeocodingClient geocodingClient) {
        this(weatherClient, timeWindowService, geocodingClient,
                Clock.systemDefaultZone());
    }

    MainpageController(
            WeatherClient weatherClient,
            TimeWindowService timeWindowService,
            GeocodingClient geocodingClient,
            Clock clock) {

        this.weatherClient = weatherClient;
        this.timeWindowService = timeWindowService;
        this.geocodingClient = geocodingClient;
        this.clock = clock;
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
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate,
            @RequestParam(name = "searchDate", required = false) LocalDate searchDate,
            Model model) {

        // The former single-day parameter keeps working: a legacy searchDate
        // searches exactly that one day. Explicit start/end dates win.
        LocalDate requestedStart = startDate != null ? startDate : searchDate;
        LocalDate requestedEnd = startDate != null
                ? (endDate != null ? endDate : startDate)
                : searchDate;

        Plan plan = buildPlan(locationId, minDurationMinutes, requestedStart,
                requestedEnd, temperaturePreference, windPreference,
                precipitationPreference);

        model.addAttribute("suitableWindows", plan.windows());
        model.addAttribute("resultDays", plan.days());
        model.addAttribute("weatherError", plan.error());
        model.addAttribute("minDurationMinutes", minDurationMinutes);
        model.addAttribute("locationId", locationId);
        model.addAttribute("locationName",
                plan.locationName() == null ? "den ausgewählten Ort" : plan.locationName());
        model.addAttribute("locationResolvedName", plan.locationName());
        model.addAttribute("precipitationPreference", precipitationPreference);
        model.addAttribute("precipitationPreferences", PrecipitationPreference.values());
        model.addAttribute("temperaturePreference", temperaturePreference);
        model.addAttribute("temperaturePreferences", TemperaturePreference.values());
        model.addAttribute("windPreference", windPreference);
        model.addAttribute("windPreferences", WindPreference.values());
        model.addAttribute("activity", activity);
        model.addAttribute("activities", Activity.values());
        model.addAttribute("startDate", plan.start());
        model.addAttribute("endDate", plan.end());
        model.addAttribute("minSearchDate", plan.today() == null ? "" : plan.today().toString());
        model.addAttribute("maxSearchDate", plan.lastDate() == null ? "" : plan.lastDate().toString());
        model.addAttribute("rangeLabel", rangeLabel(plan.start(), plan.end()));

        return "mainpage";
    }

    private String rangeLabel(LocalDate start, LocalDate end) {
        if (start == null) {
            return "";
        }
        if (end == null || end.equals(start)) {
            return DATE_FORMAT.format(start);
        }
        return DATE_FORMAT.format(start) + " – " + DATE_FORMAT.format(end);
    }

    private Plan buildPlan(
            int locationId,
            int minDurationMinutes,
            LocalDate requestedStart,
            LocalDate requestedEnd,
            TemperaturePreference temperaturePreference,
            WindPreference windPreference,
            PrecipitationPreference precipitationPreference) {

        GeocodingResponse.Location location;
        try {
            location = geocodingClient.getLocation(locationId);
        } catch (RestClientException exception) {
            return Plan.failed(requestedStart, requestedEnd, WEATHER_UNAVAILABLE);
        }

        ZoneId zone;
        try {
            zone = parseZone(location);
        } catch (DateTimeException exception) {
            return Plan.failed(requestedStart, requestedEnd,
                    "Für diesen Ort liegen keine gültigen Zeitangaben vor. "
                            + "Bitte wähle einen anderen Ort.");
        }

        OffsetDateTime now = OffsetDateTime.now(clock.withZone(zone));
        LocalDate today = now.toLocalDate();
        LocalDate lastDate = today.plusDays(FORECAST_DAYS - 1);
        LocalDate start = requestedStart == null ? today : requestedStart;
        LocalDate end = requestedEnd == null ? start : requestedEnd;

        String rangeError = validateRange(start, end, today, lastDate);
        if (rangeError != null) {
            return new Plan(location.name(), today, lastDate, start, end,
                    List.of(), List.of(), rangeError);
        }

        OffsetDateTime rangeBegin = start.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime rangeFinish = end.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        List<WeatherHour> hours;
        try {
            hours = new ArrayList<>(weatherClient.fetchForecast(
                    location.latitude(), location.longitude(), zone));
        } catch (RestClientException exception) {
            return new Plan(location.name(), today, lastDate, start, end,
                    List.of(), List.of(), WEATHER_UNAVAILABLE);
        }

        hours.removeIf(hour -> hour == null
                || hour.getTime() == null
                || hour.getTime().isBefore(now)
                || hour.getTime().isBefore(rangeBegin)
                || hour.getTime().isAfter(rangeFinish));

        // One pass over the whole range keeps windows that run across midnight
        // together instead of splitting them at the day boundary.
        List<TimeWindow> windows = new ArrayList<>(
                timeWindowService.findSuitableWindows(
                        hours, temperaturePreference, windPreference,
                        precipitationPreference));

        windows.removeIf(window -> window.durationMinutes() < minDurationMinutes);

        List<PlanningTimeline.Day> days = PlanningTimeline.build(
                windows, start, end, zone, hours, now);

        return new Plan(location.name(), today, lastDate, start, end, windows, days, null);
    }

    private ZoneId parseZone(GeocodingResponse.Location location) {
        String timezone = location.timezone();
        if (timezone == null || timezone.isBlank()) {
            throw new DateTimeException("Location has no timezone");
        }
        return ZoneId.of(timezone);
    }

    private String validateRange(LocalDate start, LocalDate end, LocalDate today, LocalDate lastDate) {
        if (end.isBefore(start)) {
            return "Das Enddatum " + DATE_FORMAT.format(end)
                    + " liegt vor dem Startdatum " + DATE_FORMAT.format(start)
                    + ". Bitte wähle einen Zeitraum, der am oder nach dem Startdatum beginnt.";
        }
        if (start.isBefore(today) || end.isAfter(lastDate)) {
            return "Bitte wähle einen Zeitraum zwischen " + DATE_FORMAT.format(today)
                    + " und " + DATE_FORMAT.format(lastDate) + ".";
        }
        return null;
    }

    private record Plan(
            String locationName,
            LocalDate today,
            LocalDate lastDate,
            LocalDate start,
            LocalDate end,
            List<TimeWindow> windows,
            List<PlanningTimeline.Day> days,
            String error) {

        static Plan failed(LocalDate start, LocalDate end, String error) {
            return new Plan(null, null, null, start, end, List.of(), List.of(), error);
        }
    }
}
