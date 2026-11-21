package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchPeriodTests {

    private final LocalDate today = LocalDate.parse("2026-11-21");

    @Test
    void searchesOnlyTodayWithoutAnyDate() {
        SearchPeriod period = SearchPeriod.resolve(null, null, null, today);

        assertEquals(new SearchPeriod(today, today), period);
        assertEquals(Optional.empty(), period.validate(today));
    }

    @Test
    void legacySearchDateSearchesExactlyThatDay() {
        LocalDate day = today.plusDays(2);

        assertEquals(new SearchPeriod(day, day), SearchPeriod.resolve(day, null, null, today));
    }

    @Test
    void startAndEndDateTakePrecedenceOverLegacySearchDate() {
        SearchPeriod period = SearchPeriod.resolve(today, today.plusDays(1), today.plusDays(3), today);

        assertEquals(new SearchPeriod(today.plusDays(1), today.plusDays(3)), period);
    }

    @Test
    void onlyStartDateSearchesThatDayAndOnlyEndDateStartsToday() {
        assertEquals(new SearchPeriod(today.plusDays(4), today.plusDays(4)),
                SearchPeriod.resolve(null, today.plusDays(4), null, today));
        assertEquals(new SearchPeriod(today, today.plusDays(4)),
                SearchPeriod.resolve(null, null, today.plusDays(4), today));
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        SearchPeriod period = new SearchPeriod(today.plusDays(3), today.plusDays(1));

        assertEquals(Optional.of(SearchPeriod.Problem.END_BEFORE_START), period.validate(today));
    }

    @Test
    void rejectsDatesOutsideTheSevenForecastDays() {
        assertEquals(Optional.of(SearchPeriod.Problem.OUTSIDE_FORECAST),
                new SearchPeriod(today.minusDays(1), today).validate(today));
        assertEquals(Optional.of(SearchPeriod.Problem.OUTSIDE_FORECAST),
                new SearchPeriod(today, today.plusDays(7)).validate(today));
        assertEquals(Optional.empty(),
                new SearchPeriod(today, today.plusDays(6)).validate(today));
    }

    @Test
    void providesDaysAndZonedBoundaries() {
        SearchPeriod period = new SearchPeriod(today, today.plusDays(2));
        ZoneId zone = ZoneId.of("Europe/Berlin");

        assertEquals(List.of(today, today.plusDays(1), today.plusDays(2)), period.days());
        assertEquals(OffsetDateTime.parse("2026-11-21T00:00+01:00"), period.startAt(zone));
        assertEquals(OffsetDateTime.parse("2026-11-24T00:00+01:00"), period.endAt(zone));
        assertTrue(new SearchPeriod(today, today).isSingleDay());
    }
}
