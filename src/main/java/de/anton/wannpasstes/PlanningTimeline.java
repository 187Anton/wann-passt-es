package de.anton.wannpasstes;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;

final class PlanningTimeline {

    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEEE, dd.MM.", Locale.GERMANY);
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("EEE dd.MM.", Locale.GERMANY);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.GERMANY);

    record Day(LocalDate date, String label, int pastMinutes, List<Band> bands, List<Entry> entries) {}

    record Band(int startMinute, int endMinute) {}

    record Entry(TimeWindow window, String startLabel, String endLabel, String duration,
                 String temperature, String wind, String precipitation) {}

    private PlanningTimeline() {}

    static List<Day> create(LocalDate start, LocalDate end, OffsetDateTime now, ZoneId zone,
                            List<TimeWindow> windows, List<WeatherHour> hours) {
        if (windows.isEmpty()) {
            return List.of();
        }

        List<Entry> entries = windows.stream().map(window -> describe(window, hours, zone)).toList();
        List<Day> days = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            Instant dayStart = date.atStartOfDay(zone).toInstant();
            Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();
            List<Band> bands = new ArrayList<>();
            List<Entry> startingEntries = new ArrayList<>();
            for (Entry entry : entries) {
                TimeWindow window = entry.window();
                Instant from = window.start().toInstant();
                Instant to = window.end().toInstant();
                if (!from.isBefore(dayStart) && from.isBefore(dayEnd)) {
                    startingEntries.add(entry);
                }
                addBands(bands, from.isBefore(dayStart) ? dayStart : from, to.isAfter(dayEnd) ? dayEnd : to,
                        date, zone.getRules());
            }
            int pastMinutes = date.equals(now.toLocalDate()) ? minuteOfDay(now.toLocalTime()) : 0;
            days.add(new Day(date, DATE.format(date), pastMinutes, List.copyOf(bands), List.copyOf(startingEntries)));
        }
        return List.copyOf(days);
    }

    private static void addBands(List<Band> bands, Instant from, Instant to, LocalDate date, ZoneRules rules) {
        // Project each offset separately so the band follows local clock labels on clock-change days.
        while (from.isBefore(to)) {
            ZoneOffsetTransition transition = rules.nextTransition(from);
            Instant end = transition != null && transition.getInstant().isBefore(to) ? transition.getInstant() : to;
            ZoneOffset offset = rules.getOffset(from);
            int startMinute = position(from.atOffset(offset), date);
            int endMinute = position(end.atOffset(offset), date);
            if (startMinute < endMinute) {
                bands.add(new Band(startMinute, endMinute));
            }
            from = end;
        }
    }

    private static int position(OffsetDateTime time, LocalDate date) {
        if (time.toLocalDate().isBefore(date)) {
            return 0;
        }
        return time.toLocalDate().equals(date) ? minuteOfDay(time.toLocalTime()) : MINUTES_PER_DAY;
    }

    private static int minuteOfDay(LocalTime time) {
        return time.toSecondOfDay() / 60;
    }

    private static Entry describe(TimeWindow window, List<WeatherHour> hours, ZoneId zone) {
        var temperature = new DoubleSummaryStatistics();
        var wind = new DoubleSummaryStatistics();
        var precipitation = new DoubleSummaryStatistics();

        for (WeatherHour hour : hours) {
            OffsetDateTime time = hour.getTime();
            if (!time.isBefore(window.start()) && !time.isAfter(window.end())) {
                temperature.accept(hour.getTemperatureCelsius());
                wind.accept(hour.getWindKmh());
                // Open-Meteo's precipitation at the start belongs to the preceding hour.
                if (time.isAfter(window.start())) {
                    precipitation.accept(hour.getPrecipitationMm());
                }
            }
        }

        boolean endsAtMidnight = window.end().toLocalTime().equals(LocalTime.MIDNIGHT)
                && window.end().toLocalDate().equals(window.start().toLocalDate().plusDays(1));
        boolean crossesDates = !endsAtMidnight && !window.start().toLocalDate().equals(window.end().toLocalDate());
        return new Entry(window, timeLabel(window.start(), crossesDates, zone),
                endsAtMidnight ? "24:00" : timeLabel(window.end(), crossesDates, zone),
                durationLabel(window.durationMinutes()), range(temperature, "°C"),
                range(wind, "km/h"), range(precipitation, "mm/h"));
    }

    private static String timeLabel(OffsetDateTime time, boolean showDate, ZoneId zone) {
        String label = showDate ? SHORT_DATE.format(time) + " " + TIME.format(time) : TIME.format(time);
        if (zone.getRules().getValidOffsets(time.toLocalDateTime()).size() > 1) {
            return label + " (UTC" + time.getOffset() + ")";
        }
        return label;
    }

    private static String durationLabel(long minutes) {
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        if (hours == 0) {
            return remainingMinutes + " Min.";
        }
        return hours + " Std." + (remainingMinutes == 0 ? "" : " " + remainingMinutes + " Min.");
    }

    private static String range(DoubleSummaryStatistics values, String unit) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMANY);
        String min = format.format(values.getMin());
        String max = format.format(values.getMax());
        return (min.equals(max) ? min : min + "–" + max) + " " + unit;
    }
}
