package de.anton.wannpasstes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Presentation model of the found time windows: grouped list, summary and a
 * timeline spanning the whole search period. All labels are German.
 */
public record ResultView(
        List<DayGroup> groups,
        Timeline timeline,
        int windowCount,
        String longestDurationLabel,
        String totalDurationLabel) {

    private static final Locale GERMAN = Locale.GERMAN;
    private static final DateTimeFormatter DAY_LONG = DateTimeFormatter.ofPattern("EEEE, d. MMMM", GERMAN);
    private static final DateTimeFormatter DAY_SHORT = DateTimeFormatter.ofPattern("EEE, dd.MM.", GERMAN);
    private static final DateTimeFormatter WEEKDAY = DateTimeFormatter.ofPattern("EEE", GERMAN);
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("dd.MM.", GERMAN);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", GERMAN);

    public record WindowView(
            String startTime,
            String endTime,
            String endDayLabel,
            boolean spansMidnight,
            String durationLabel,
            String startIso,
            String endIso,
            String offsetPercent,
            String widthPercent,
            String accessibleLabel) {
    }

    public record DayGroup(LocalDate date, String label, List<WindowView> windows) {
    }

    public record DayMarker(String weekday, String date, String offsetPercent, String widthPercent) {
    }

    public record Timeline(List<DayMarker> days, String pastWidthPercent, List<WindowView> segments) {
    }

    public static ResultView of(
            List<TimeWindow> windows, SearchPeriod period, ZoneId zone, OffsetDateTime now) {

        OffsetDateTime periodStart = period.startAt(zone);
        OffsetDateTime periodEnd = period.endAt(zone);
        double totalMinutes = Duration.between(periodStart, periodEnd).toMinutes();

        List<TimeWindow> sorted = new ArrayList<>(windows);
        sorted.sort(Comparator.comparing(window -> window.start().toInstant()));

        Map<LocalDate, List<WindowView>> byDay = new LinkedHashMap<>();
        List<WindowView> segments = new ArrayList<>();
        long longest = 0;
        long total = 0;

        for (TimeWindow window : sorted) {
            WindowView view = toView(window, zone, periodStart, totalMinutes);
            LocalDate day = window.start().atZoneSameInstant(zone).toLocalDate();
            byDay.computeIfAbsent(day, ignored -> new ArrayList<>()).add(view);
            segments.add(view);
            longest = Math.max(longest, window.durationMinutes());
            total += window.durationMinutes();
        }

        List<DayGroup> groups = byDay.entrySet().stream()
                .map(entry -> new DayGroup(entry.getKey(), DAY_LONG.format(entry.getKey()), entry.getValue()))
                .toList();

        List<DayMarker> markers = period.days().stream()
                .map(day -> {
                    OffsetDateTime dayStart = day.atStartOfDay(zone).toOffsetDateTime();
                    OffsetDateTime dayEnd = day.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
                    return new DayMarker(
                            WEEKDAY.format(day),
                            DATE_SHORT.format(day),
                            percent(Duration.between(periodStart, dayStart).toMinutes(), totalMinutes),
                            percent(Duration.between(dayStart, dayEnd).toMinutes(), totalMinutes));
                })
                .toList();

        long pastMinutes = 0;
        if (now.isAfter(periodStart)) {
            OffsetDateTime pastEnd = now.isBefore(periodEnd) ? now : periodEnd;
            pastMinutes = Duration.between(periodStart, pastEnd).toMinutes();
        }

        return new ResultView(
                groups,
                new Timeline(markers, percent(pastMinutes, totalMinutes), segments),
                sorted.size(),
                sorted.isEmpty() ? "" : formatDuration(longest),
                sorted.isEmpty() ? "" : formatDuration(total));
    }

    private static WindowView toView(
            TimeWindow window, ZoneId zone, OffsetDateTime periodStart, double totalMinutes) {

        OffsetDateTime start = window.start().atZoneSameInstant(zone).toOffsetDateTime();
        OffsetDateTime end = window.end().atZoneSameInstant(zone).toOffsetDateTime();
        LocalDate startDay = start.toLocalDate();

        // A window ending exactly at midnight is shown as "24:00" of the previous day.
        boolean endsAtMidnight = end.toLocalTime().equals(LocalTime.MIDNIGHT);
        LocalDate endDisplayDay = endsAtMidnight ? end.toLocalDate().minusDays(1) : end.toLocalDate();
        boolean spansMidnight = endDisplayDay.isAfter(startDay);

        String endTime = endsAtMidnight ? "24:00" : TIME.format(end);
        String endDayLabel = spansMidnight ? DAY_SHORT.format(endDisplayDay) : "";
        String duration = formatDuration(window.durationMinutes());

        String accessibleLabel = DAY_SHORT.format(start) + " " + TIME.format(start) + " bis "
                + (spansMidnight ? endDayLabel + " " : "") + endTime + " Uhr, " + duration;

        return new WindowView(
                TIME.format(start),
                endTime,
                endDayLabel,
                spansMidnight,
                duration,
                start.toString(),
                end.toString(),
                percent(Duration.between(periodStart, start).toMinutes(), totalMinutes),
                percent(window.durationMinutes(), totalMinutes),
                accessibleLabel);
    }

    /** Formats minutes as e.g. "2 Std. 30 Min.", "45 Min." or "3 Std.". */
    public static String formatDuration(long minutes) {
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

    /** Percentage for inline CSS; always uses a dot as decimal separator. */
    private static String percent(double part, double total) {
        double value = total <= 0 ? 0 : Math.max(0, Math.min(100, part * 100.0 / total));
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
