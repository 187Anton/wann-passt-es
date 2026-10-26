package de.anton.wannpasstes;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TimeWindowService {

    public List<TimeWindow> findSuitableWindows(
            List<WeatherHour> hours,
            TemperaturePreference temperaturePreference,
            WindPreference windPreference,
            PrecipitationPreference precipitationPreference) {

        double minTemperatureCelsius =
                temperaturePreference.getMinTemperatureCelsius();
        double maxTemperatureCelsius =
                temperaturePreference.getMaxTemperatureCelsius();
        double maxWindKmh =
                windPreference.getMaxWindKmh();
        double maxPrecipitationMm =
                precipitationPreference.getMaxPrecipitationMm();

        List<WeatherHour> sorted = new ArrayList<>(hours);
        sorted.sort(Comparator.comparing(hour -> hour.getTime().toInstant()));

        List<TimeWindow> intervals = new ArrayList<>();

        for (int i = 0; i + 1 < sorted.size(); i++) {
            WeatherHour start = sorted.get(i);
            WeatherHour end = sorted.get(i + 1);

            Duration gap = Duration.between(start.getTime(), end.getTime());

            if (!gap.equals(Duration.ofHours(1))) {
                continue;
            }

            boolean temperatureMatches = start.getTemperatureCelsius() >= minTemperatureCelsius
                    && start.getTemperatureCelsius() <= maxTemperatureCelsius
                    && end.getTemperatureCelsius() >= minTemperatureCelsius
                    && end.getTemperatureCelsius() <= maxTemperatureCelsius;

            boolean windMatches = start.getWindKmh() <= maxWindKmh
                    && end.getWindKmh() <= maxWindKmh;

            boolean precipitationMatches = end.getPrecipitationMm() <= maxPrecipitationMm;

            if (temperatureMatches && windMatches && precipitationMatches) {
                intervals.add(new TimeWindow(start.getTime(), end.getTime()));
            }
        }

        return mergeWindows(intervals);
    }

    public List<TimeWindow> mergeWindows(List<TimeWindow> windows) {
        if (windows.isEmpty()) {
            return List.of();
        }
        List<TimeWindow> sorted = new ArrayList<>(windows);
        sorted.sort(Comparator.comparing(window -> window.start().toInstant()));

        List<TimeWindow> merged = new ArrayList<>();
        TimeWindow current = sorted.getFirst();

        for (int i = 1; i < sorted.size(); i++) {
            TimeWindow next = sorted.get(i);

            if (!next.start().isAfter(current.end())) {
                if (next.end().isAfter(current.end())) {
                    current = new TimeWindow(current.start(), next.end());
                }
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }
}
