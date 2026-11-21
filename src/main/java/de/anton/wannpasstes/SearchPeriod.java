package de.anton.wannpasstes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Inclusive range of local calendar days that is searched for time windows.
 */
public record SearchPeriod(LocalDate startDate, LocalDate endDate) {

    /** Number of forecast days loaded from Open-Meteo, including today. */
    public static final int FORECAST_DAYS = 7;

    public enum Problem {
        END_BEFORE_START,
        OUTSIDE_FORECAST
    }

    public SearchPeriod {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end date must not be null");
        }
    }

    /**
     * Resolves the request parameters to a period.
     * <ul>
     *     <li>{@code startDate}/{@code endDate} take precedence over the legacy {@code searchDate}.</li>
     *     <li>Only a start date: exactly that day.</li>
     *     <li>Only an end date: from today up to the end date.</li>
     *     <li>Only the legacy {@code searchDate}: exactly that day.</li>
     *     <li>Nothing: today only.</li>
     * </ul>
     */
    public static SearchPeriod resolve(
            LocalDate legacySearchDate, LocalDate startDate, LocalDate endDate, LocalDate today) {

        if (startDate == null && endDate == null) {
            LocalDate day = legacySearchDate == null ? today : legacySearchDate;
            return new SearchPeriod(day, day);
        }
        if (startDate == null) {
            return new SearchPeriod(today, endDate);
        }
        if (endDate == null) {
            return new SearchPeriod(startDate, startDate);
        }
        return new SearchPeriod(startDate, endDate);
    }

    public static LocalDate lastForecastDate(LocalDate today) {
        return today.plusDays(FORECAST_DAYS - 1L);
    }

    /** Returns the first problem of this period, if any, relative to {@code today}. */
    public Optional<Problem> validate(LocalDate today) {
        LocalDate lastDate = lastForecastDate(today);
        if (startDate.isBefore(today) || startDate.isAfter(lastDate)
                || endDate.isBefore(today) || endDate.isAfter(lastDate)) {
            return Optional.of(Problem.OUTSIDE_FORECAST);
        }
        if (endDate.isBefore(startDate)) {
            return Optional.of(Problem.END_BEFORE_START);
        }
        return Optional.empty();
    }

    /** Start of the first day in the given zone. */
    public OffsetDateTime startAt(ZoneId zone) {
        return startDate.atStartOfDay(zone).toOffsetDateTime();
    }

    /** Start of the day after the last day in the given zone (exclusive end). */
    public OffsetDateTime endAt(ZoneId zone) {
        return endDate.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
    }

    public List<LocalDate> days() {
        List<LocalDate> days = new ArrayList<>();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            days.add(day);
        }
        return days;
    }

    public boolean isSingleDay() {
        return startDate.equals(endDate);
    }
}
