package de.anton.wannpasstes;

import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

public record PlanningSelection(
        @Min(1) Integer locationId,
        Activity activity,
        @Min(1) Integer minDurationMinutes,
        TemperaturePreference temperaturePreference,
        WindPreference windPreference,
        PrecipitationPreference precipitationPreference,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate searchDate) {

    public PlanningSelection {
        locationId = locationId == null ? 2950159 : locationId;
        activity = activity == null ? Activity.WALKING : activity;
        minDurationMinutes = minDurationMinutes == null ? 60 : minDurationMinutes;
        temperaturePreference = temperaturePreference == null ? TemperaturePreference.MILD : temperaturePreference;
        windPreference = windPreference == null ? WindPreference.WEAK : windPreference;
        precipitationPreference = precipitationPreference == null
                ? PrecipitationPreference.LIGHT_DRIZZLE : precipitationPreference;
    }

    public Map<String, String> parameters() {
        Map<String, String> values = new TreeMap<>();
        values.put("locationId", locationId.toString());
        values.put("activity", activity.name());
        values.put("minDurationMinutes", minDurationMinutes.toString());
        values.put("temperaturePreference", temperaturePreference.name());
        values.put("windPreference", windPreference.name());
        values.put("precipitationPreference", precipitationPreference.name());
        if (startDate != null) {
            values.put("startDate", startDate.toString());
        }
        if (endDate != null) {
            values.put("endDate", endDate.toString());
        }
        if (searchDate != null) {
            values.put("searchDate", searchDate.toString());
        }
        return values;
    }
}
