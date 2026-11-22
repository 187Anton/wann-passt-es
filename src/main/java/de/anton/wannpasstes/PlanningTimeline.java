package de.anton.wannpasstes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lays suitable time windows out for display: one row per day of the searched
 * range, each window as a segment on a shared 0–24 hour scale. This class only
 * formats and positions; the weather rules stay in {@link TimeWindowService}.
 */
public final class PlanningTimeline {

    private static final DateTimeFormatter DATE_TEXT =
            DateTimeFormatter.ofPattern("dd.MM.", Locale.GERMAN);
    private static final DateTimeFormatter TIME_TEXT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.GERMAN);

    private PlanningTimeline() {
    }

    /** One row of the result list. */
    public record Day(
            LocalDate date,
            String name,
            String dateText,
            boolean today,
            String pastStyle,
            String nowStyle,
            List<Segment> segments) {
    }

    /** The part of a window that falls into one day. */
    public record Segment(
            String style,
            String timeText,
            String durationText,
            String note,
            boolean continuesFromPrevious,
            boolean continuesNext,
            WindowDetails details) {
    }

    /** Weather values summarised over the hours of one window. */
    public record WindowDetails(
            String temperature,
            String wind,
            String precipitation) {
    }

    public static List<Day> build(
            List<TimeWindow> windows,
            LocalDate start,
            LocalDate end,
            ZoneId zone,
            List<WeatherHour> hours,
            OffsetDateTime now) {

        if (windows == null || start == null || end == null || zone == null || end.isBefore(start)) {
            return List.of();
        }

        Map<LocalDate, List<Segment>> segmentsByDay = new LinkedHashMap<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            segmentsByDay.put(date, new ArrayList<>());
        }

        for (TimeWindow window : windows) {
            if (window == null) {
                continue;
            }
            WindowDetails details = detailsOf(window, hours);
            for (Map.Entry<LocalDate, List<Segment>> entry : segmentsByDay.entrySet()) {
                Segment segment = segmentOf(window, entry.getKey(), zone, details);
                if (segment != null) {
                    entry.getValue().add(segment);
                }
            }
        }

        LocalDate today = now == null ? null : now.toLocalDate();
        List<Day> days = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Segment>> entry : segmentsByDay.entrySet()) {
            LocalDate date = entry.getKey();
            boolean isToday = date.equals(today);
            days.add(new Day(
                    date,
                    weekdayName(date, TextStyle.FULL),
                    DATE_TEXT.format(date),
                    isToday,
                    isToday ? "width:" + percent(minutesBetween(
                            date.atStartOfDay(zone).toOffsetDateTime(), now), dayMinutes(date, zone)) + "%" : null,
                    isToday ? "left:" + percent(minutesBetween(
                            date.atStartOfDay(zone).toOffsetDateTime(), now), dayMinutes(date, zone)) + "%" : null,
                    List.copyOf(entry.getValue())));
        }
        return List.copyOf(days);
    }

    private static Segment segmentOf(
            TimeWindow window, LocalDate date, ZoneId zone, WindowDetails details) {

        OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime dayEnd = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        OffsetDateTime segmentStart = window.start().isAfter(dayStart) ? window.start() : dayStart;
        OffsetDateTime segmentEnd = window.end().isBefore(dayEnd) ? window.end() : dayEnd;

        if (!segmentEnd.isAfter(segmentStart)) {
            return null;
        }

        long dayLength = Duration.between(dayStart, dayEnd).toMinutes();
        long left = Duration.between(dayStart, segmentStart).toMinutes();
        long width = Duration.between(segmentStart, segmentEnd).toMinutes();

        boolean continuesFromPrevious = window.start().isBefore(dayStart);
        boolean continuesNext = window.end().isAfter(dayEnd);

        return new Segment(
                "left:" + percent(left, dayLength) + "%;width:" + percent(width, dayLength) + "%",
                continuesFromPrevious ? "Fortsetzung" : windowTimeText(window),
                continuesFromPrevious ? null : formatDuration(window.durationMinutes()),
                continuesFromPrevious ? "bis " + windowEndText(window) : null,
                continuesFromPrevious,
                continuesNext,
                continuesFromPrevious ? null : details);
    }

    /**
     * Start and end as one label. A window ending exactly at midnight belongs to
     * the day before and shows 24:00 instead of 00:00 on the next day.
     */
    private static String windowTimeText(TimeWindow window) {
        EndLabel end = endOf(window);
        String startTime = TIME_TEXT.format(window.start());
        if (end.sameDay()) {
            return startTime + "–" + end.text();
        }
        return weekdayName(window.start().toLocalDate(), TextStyle.SHORT) + " "
                + startTime + " – " + end.text();
    }

    /** The end of the window as displayed, with weekday only when it is another day. */
    private static String windowEndText(TimeWindow window) {
        EndLabel end = endOf(window);
        return end.text();
    }

    /**
     * A window ending at midnight on the following day reads 17:00–24:00. A
     * window spanning several days keeps the real end such as Mo 00:00.
     */
    private static EndLabel endOf(TimeWindow window) {
        LocalDate startDay = window.start().toLocalDate();
        LocalDate endDay = window.end().toLocalDate();
        String time = TIME_TEXT.format(window.end());

        if (LocalTime.MIDNIGHT.equals(window.end().toLocalTime())) {
            if (endDay.minusDays(1).equals(startDay)) {
                endDay = startDay;
                time = "24:00";
            } else {
                time = "00:00";
            }
        }

        if (startDay.equals(endDay)) {
            return new EndLabel(time, true);
        }
        return new EndLabel(weekdayName(endDay, TextStyle.SHORT) + " " + time, false);
    }

    private record EndLabel(String text, boolean sameDay) {
    }

    private static WindowDetails detailsOf(TimeWindow window, List<WeatherHour> hours) {
        if (hours == null) {
            return null;
        }

        boolean found = false;
        double minTemperature = Double.MAX_VALUE;
        double maxTemperature = -Double.MAX_VALUE;
        double maxWind = 0;
        double maxPrecipitation = 0;

        for (WeatherHour hour : hours) {
            if (hour == null || hour.getTime() == null) {
                continue;
            }
            if (hour.getTime().isBefore(window.start()) || hour.getTime().isAfter(window.end())) {
                continue;
            }
            found = true;
            minTemperature = Math.min(minTemperature, hour.getTemperatureCelsius());
            maxTemperature = Math.max(maxTemperature, hour.getTemperatureCelsius());
            maxWind = Math.max(maxWind, hour.getWindKmh());
            maxPrecipitation = Math.max(maxPrecipitation, hour.getPrecipitationMm());
        }

        if (!found) {
            return null;
        }

        long min = Math.round(minTemperature);
        long max = Math.round(maxTemperature);
        String temperature = min == max ? min + " °C" : min + "–" + max + " °C";

        return new WindowDetails(
                temperature,
                "bis " + Math.round(maxWind) + " km/h",
                precipitationText(maxPrecipitation));
    }

    private static String precipitationText(double millimetres) {
        if (millimetres < 0.05) {
            return "kein Niederschlag";
        }
        String value = String.format(Locale.GERMAN, "%.1f", millimetres);
        return "bis " + value + " mm/h";
    }

    static String formatDuration(long minutes) {
        long hours = minutes / 60;
        long rest = minutes % 60;
        if (hours == 0) {
            return rest + " Min.";
        }
        if (rest == 0) {
            return hours + " Std.";
        }
        return hours + " Std. " + rest + " Min.";
    }

    private static String weekdayName(LocalDate date, TextStyle style) {
        String name = date.getDayOfWeek().getDisplayName(style, Locale.GERMAN);
        return name.endsWith(".") ? name.substring(0, name.length() - 1) : name;
    }

    private static long minutesBetween(OffsetDateTime from, OffsetDateTime to) {
        if (to == null) {
            return 0;
        }
        return Math.max(0, Duration.between(from, to).toMinutes());
    }

    private static long dayMinutes(LocalDate date, ZoneId zone) {
        return Duration.between(
                date.atStartOfDay(zone).toOffsetDateTime(),
                date.plusDays(1).atStartOfDay(zone).toOffsetDateTime()).toMinutes();
    }

    private static String percent(long part, long whole) {
        if (whole <= 0) {
            return "0.000";
        }
        double value = Math.min(100, Math.max(0, part * 100.0 / whole));
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
