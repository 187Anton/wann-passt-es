package de.anton.wannpasstes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * Presentation model of the found time windows: one row per day of the search
 * period on a shared 0–24 h scale, the windows grouped by start day, a short
 * summary and the weather range within each window. All labels are German.
 */
public record ResultView(
        List<DayGroup> groups,
        List<DayRow> days,
        int windowCount,
        String longestDurationLabel,
        String totalDurationLabel,
        String periodLabel) {

    private static final Locale GERMAN = Locale.GERMAN;
    private static final DateTimeFormatter DAY_LONG = DateTimeFormatter.ofPattern("EEEE, d. MMMM", GERMAN);
    private static final DateTimeFormatter DAY_SHORT = DateTimeFormatter.ofPattern("EEE, dd.MM.", GERMAN);
    private static final DateTimeFormatter WEEKDAY = DateTimeFormatter.ofPattern("EEE", GERMAN);
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("dd.MM.", GERMAN);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", GERMAN);
    private static final double MINUTES_PER_DAY = 24 * 60;

    /** Range of the hourly forecast values within a window, e.g. "12–17 °C". */
    public record WeatherRange(String temperature, String wind, String precipitation) {
    }

    /**
     * A time window as shown in the list. {@code rangeLabel} is the full text,
     * e.g. "17:00–24:00" or, for windows across midnight, "Sa 19:00 – Mo 00:00".
     */
    public record WindowView(
            String startTime,
            String endTime,
            String endDayLabel,
            boolean spansMidnight,
            String durationLabel,
            String startIso,
            String endIso,
            String rangeLabel,
            String accessibleLabel,
            WeatherRange weather) {

        public boolean hasWeather() {
            return weather != null;
        }
    }

    public record DayGroup(LocalDate date, String label, List<WindowView> windows) {
    }

    /**
     * Part of a window within one day row. Positions are percentages of the
     * 0–24 h scale; the flags mark windows continuing from the previous or into
     * the next day.
     */
    public record Segment(
            String offsetPercent,
            String widthPercent,
            boolean continuesBefore,
            boolean continuesAfter) {
    }

    /**
     * One day of the search period. {@code windows} start on this day,
     * {@code continuations} started on an earlier day and reach into it.
     * {@code pastPercent} is the elapsed part of today, otherwise {@code null}.
     */
    public record DayRow(
            LocalDate date,
            String weekday,
            String dateLabel,
            String label,
            boolean today,
            String pastPercent,
            List<Segment> segments,
            List<WindowView> windows,
            List<WindowView> continuations) {

        public boolean isEmpty() {
            return windows.isEmpty() && continuations.isEmpty();
        }
    }

    /** Builds the view without weather details. */
    public static ResultView of(
            List<TimeWindow> windows, SearchPeriod period, ZoneId zone, OffsetDateTime now) {
        return of(windows, period, zone, now, List.of());
    }

    /**
     * Builds the view. {@code hours} are only used to show the weather range
     * within each window; they do not influence which windows are shown.
     */
    public static ResultView of(
            List<TimeWindow> windows, SearchPeriod period, ZoneId zone, OffsetDateTime now,
            List<WeatherHour> hours) {

        List<TimeWindow> sorted = new ArrayList<>(windows);
        sorted.sort(Comparator.comparing(window -> window.start().toInstant()));

        Map<LocalDate, List<WindowView>> byDay = new LinkedHashMap<>();
        List<Map.Entry<TimeWindow, WindowView>> views = new ArrayList<>();
        long longest = 0;
        long total = 0;

        for (TimeWindow window : sorted) {
            WindowView view = toView(window, zone, hours);
            LocalDate day = window.start().atZoneSameInstant(zone).toLocalDate();
            byDay.computeIfAbsent(day, ignored -> new ArrayList<>()).add(view);
            views.add(Map.entry(window, view));
            longest = Math.max(longest, window.durationMinutes());
            total += window.durationMinutes();
        }

        List<DayGroup> groups = byDay.entrySet().stream()
                .map(entry -> new DayGroup(entry.getKey(), DAY_LONG.format(entry.getKey()), entry.getValue()))
                .toList();

        LocalDate today = now.atZoneSameInstant(zone).toLocalDate();
        List<DayRow> days = period.days().stream()
                .map(day -> toDayRow(day, zone, now, today, views))
                .toList();

        return new ResultView(
                groups,
                days,
                sorted.size(),
                sorted.isEmpty() ? "" : formatDuration(longest),
                sorted.isEmpty() ? "" : formatDuration(total),
                periodLabel(period));
    }

    private static DayRow toDayRow(
            LocalDate day, ZoneId zone, OffsetDateTime now, LocalDate today,
            List<Map.Entry<TimeWindow, WindowView>> views) {

        OffsetDateTime dayStart = day.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime dayEnd = day.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        List<Segment> segments = new ArrayList<>();
        List<WindowView> starting = new ArrayList<>();
        List<WindowView> continuing = new ArrayList<>();

        for (Map.Entry<TimeWindow, WindowView> entry : views) {
            TimeWindow window = entry.getKey();
            WindowView view = entry.getValue();
            if (!window.start().isBefore(dayEnd) || !window.end().isAfter(dayStart)) {
                continue;
            }
            boolean continuesBefore = window.start().isBefore(dayStart);
            boolean continuesAfter = window.end().isAfter(dayEnd);
            double from = continuesBefore ? 0 : minuteOfDay(window.start(), zone);
            double to = window.end().isBefore(dayEnd) ? minuteOfDay(window.end(), zone) : MINUTES_PER_DAY;
            segments.add(new Segment(
                    percent(from, MINUTES_PER_DAY),
                    percent(to - from, MINUTES_PER_DAY),
                    continuesBefore,
                    continuesAfter));
            (continuesBefore ? continuing : starting).add(view);
        }

        String pastPercent = null;
        if (day.equals(today) && now.isAfter(dayStart)) {
            pastPercent = percent(now.isBefore(dayEnd) ? minuteOfDay(now, zone) : MINUTES_PER_DAY, MINUTES_PER_DAY);
        }

        return new DayRow(
                day,
                weekday(day),
                DATE_SHORT.format(day),
                DAY_LONG.format(day),
                day.equals(today),
                pastPercent,
                segments,
                starting,
                continuing);
    }

    private static WindowView toView(TimeWindow window, ZoneId zone, List<WeatherHour> hours) {
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

        // Windows across midnight name both days, e.g. "Sa 19:00 – Mo 00:00".
        String rangeLabel = spansMidnight
                ? weekday(startDay) + " " + TIME.format(start) + " – " + weekday(end.toLocalDate()) + " " + TIME.format(end)
                : TIME.format(start) + "–" + endTime;

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
                rangeLabel,
                accessibleLabel,
                weatherRange(window, hours));
    }

    /**
     * Weather range within a window, consistent with the evaluation: temperature
     * and wind at all hourly points from start to end, precipitation at the points
     * after the start (each value describes the preceding hour).
     */
    private static WeatherRange weatherRange(TimeWindow window, List<WeatherHour> hours) {
        Predicate<WeatherHour> atPoint = hour -> hour.getTime() != null
                && !hour.getTime().isBefore(window.start()) && !hour.getTime().isAfter(window.end());
        Predicate<WeatherHour> afterStart = atPoint.and(hour -> hour.getTime().isAfter(window.start()));

        DoubleSummaryStatistics temperature = statistics(hours, atPoint, WeatherHour::getTemperatureCelsius);
        DoubleSummaryStatistics wind = statistics(hours, atPoint, WeatherHour::getWindKmh);
        DoubleSummaryStatistics precipitation = statistics(hours, afterStart, WeatherHour::getPrecipitationMm);

        if (temperature.getCount() == 0 || precipitation.getCount() == 0) {
            return null;
        }
        return new WeatherRange(
                range(temperature, "%.0f", "°C"),
                range(wind, "%.0f", "km/h"),
                range(precipitation, "%.1f", "mm/h"));
    }

    private static DoubleSummaryStatistics statistics(
            List<WeatherHour> hours, Predicate<WeatherHour> filter, ToDoubleFunction<WeatherHour> value) {
        return hours.stream().filter(filter).mapToDouble(value).summaryStatistics();
    }

    private static String range(DoubleSummaryStatistics statistics, String format, String unit) {
        String min = String.format(GERMAN, format, statistics.getMin());
        String max = String.format(GERMAN, format, statistics.getMax());
        // Avoid "-0" for slightly negative values that round to zero.
        min = min.equals("-0") ? "0" : min;
        max = max.equals("-0") ? "0" : max;
        return (min.equals(max) ? min : min + " bis " + max) + " " + unit;
    }

    private static String periodLabel(SearchPeriod period) {
        String start = weekday(period.startDate()) + " " + DATE_SHORT.format(period.startDate());
        if (period.isSingleDay()) {
            return start;
        }
        return start + " – " + weekday(period.endDate()) + " " + DATE_SHORT.format(period.endDate());
    }

    /** Short weekday without the trailing dot, e.g. "Sa". */
    private static String weekday(LocalDate day) {
        String weekday = WEEKDAY.format(day);
        return weekday.endsWith(".") ? weekday.substring(0, weekday.length() - 1) : weekday;
    }

    /** Minutes since local midnight (wall clock), so that the 0–24 h scale matches the hour marks. */
    private static double minuteOfDay(OffsetDateTime time, ZoneId zone) {
        LocalTime local = time.atZoneSameInstant(zone).toLocalTime();
        return local.getHour() * 60 + local.getMinute();
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
