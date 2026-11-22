package de.anton.wannpasstes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Planning page: search period (package B), invalid parameters (C1) and
 * the selection handed to the location search (C4).
 */
@WebMvcTest(MainpageController.class)
@Import(TimeWindowService.class)
class MainpageWebMvcTests {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private WeatherClient weatherClient;

    @MockitoBean
    private GeocodingClient geocodingClient;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now(ZONE);
        when(geocodingClient.getLocation(anyInt())).thenReturn(new GeocodingResponse.Location(
                2950159, "Berlin", 52.52, 13.405, ZONE.getId(), "Deutschland", "Land Berlin"));
    }

    /** Hourly forecast for the seven days; hours (offset from tomorrow 00:00) in {@code good} are suitable. */
    private List<WeatherHour> forecast(Set<Integer> good) {
        List<WeatherHour> hours = new ArrayList<>();
        OffsetDateTime first = today.atStartOfDay(ZONE).toOffsetDateTime();
        OffsetDateTime tomorrow = today.plusDays(1).atStartOfDay(ZONE).toOffsetDateTime();
        for (OffsetDateTime time = first; time.isBefore(first.plusDays(7)); time = time.plusHours(1)) {
            long offset = java.time.Duration.between(tomorrow, time).toHours();
            boolean suitable = good.contains((int) offset);
            hours.add(new WeatherHour(suitable ? 18.0 : 0.0, time, 5.0, 0.0, 0));
        }
        return hours;
    }

    private OffsetDateTime tomorrowAt(int hourOffset) {
        return today.plusDays(1).atStartOfDay(ZONE).plusHours(hourOffset).toOffsetDateTime();
    }

    @Test
    void searchesOnlyTodayWithoutDates() throws Exception {
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any())).thenReturn(List.of());

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("startDate", today.toString()))
                .andExpect(model().attribute("endDate", today.toString()));

        verify(weatherClient).fetchForecast(52.52, 13.405, ZONE);
    }

    @Test
    void legacySearchDateLinkSearchesExactlyThatDay() throws Exception {
        LocalDate day = today.plusDays(2);
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any())).thenReturn(List.of());

        mvc.perform(get("/").param("searchDate", day.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("startDate", day.toString()))
                .andExpect(model().attribute("endDate", day.toString()))
                .andExpect(model().attributeDoesNotExist("dateError"));
    }

    @Test
    void rejectsEndDateBeforeStartDateWithoutWeatherRequest() throws Exception {
        mvc.perform(get("/")
                        .param("startDate", today.plusDays(3).toString())
                        .param("endDate", today.plusDays(1).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(model().attributeExists("dateError"))
                .andExpect(content().string(containsString("Das Enddatum liegt vor dem Startdatum")))
                .andExpect(content().string(containsString("Es wurde keine Wetterabfrage ausgeführt")))
                // the entered values stay in the form
                .andExpect(model().attribute("startDate", today.plusDays(3).toString()))
                .andExpect(model().attribute("endDate", today.plusDays(1).toString()));

        verify(weatherClient, never()).fetchForecast(anyDouble(), anyDouble(), any());
    }

    @Test
    void rejectsDatesOutsideTheForecastWithoutWeatherRequest() throws Exception {
        mvc.perform(get("/").param("startDate", today.minusDays(1).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Bitte wähle Daten zwischen")));
        mvc.perform(get("/").param("startDate", today.toString()).param("endDate", today.plusDays(7).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Bitte wähle Daten zwischen")));

        verify(weatherClient, never()).fetchForecast(anyDouble(), anyDouble(), any());
    }

    @Test
    void showsAllWindowsOfSeveralDaysChronologically() throws Exception {
        // tomorrow 10-12 and the day after tomorrow 14-17
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any()))
                .thenReturn(forecast(Set.of(10, 11, 12, 38, 39, 40, 41)));

        mvc.perform(get("/")
                        .param("startDate", today.plusDays(1).toString())
                        .param("endDate", today.plusDays(2).toString())
                        .param("temperaturePreference", "MILD")
                        .param("windPreference", "WEAK")
                        .param("precipitationPreference", "DRY"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("suitableWindows", List.of(
                        new TimeWindow(tomorrowAt(10), tomorrowAt(12)),
                        new TimeWindow(tomorrowAt(38), tomorrowAt(41)))))
                .andExpect(content().string(allOf(
                        containsString("2 Std."),
                        containsString("3 Std."),
                        containsString("Passende Zeitfenster"))));
    }

    @Test
    void showsWindowAcrossMidnightAsOneWindow() throws Exception {
        // tomorrow 22:00 until the day after tomorrow 02:00
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any()))
                .thenReturn(forecast(Set.of(22, 23, 24, 25, 26)));

        MvcResult result = mvc.perform(get("/")
                        .param("startDate", today.plusDays(1).toString())
                        .param("endDate", today.plusDays(2).toString())
                        .param("minDurationMinutes", "240"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("suitableWindows", List.of(
                        new TimeWindow(tomorrowAt(22), tomorrowAt(26)))))
                .andExpect(content().string(containsString("über Mitternacht")))
                .andReturn();

        ResultView view = (ResultView) result.getModelAndView().getModel().get("result");
        assertEquals(1, view.windowCount());
        assertEquals(1, view.groups().size());
    }

    @Test
    void rendersDayRowsWeatherDetailsAndWishesAsRadioButtons() throws Exception {
        // tomorrow 22:00 until the day after tomorrow 02:00; the third day has no window
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any()))
                .thenReturn(forecast(Set.of(22, 23, 24, 25, 26)));

        String html = mvc.perform(get("/")
                        .param("startDate", today.plusDays(1).toString())
                        .param("endDate", today.plusDays(3).toString())
                        .param("windPreference", "MODERATE"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // One row per day of the period, the window is listed once and continued once.
        assertEquals(3, html.split("class=\"day\"", -1).length - 1);
        assertEquals(1, html.split("<details class=\"window\"", -1).length - 1);
        assertEquals(1, html.split("window--continued", -1).length - 1);
        assertEquals(2, html.split("class=\"band__window", -1).length - 1);
        org.hamcrest.MatcherAssert.assertThat(html, allOf(
                containsString("Fortsetzung"),
                containsString("Kein passendes Zeitfenster"),
                containsString("4 Std."),
                containsString("18 °C"),
                containsString("5 km/h"),
                containsString("0,0 mm/h"),
                containsString("<input type=\"radio\" name=\"windPreference\" value=\"MODERATE\" checked=\"checked\">"),
                containsString("<input type=\"radio\" name=\"windPreference\" value=\"WEAK\">"),
                containsString("bis 30 km/h")));
    }

    @Test
    void explainsWhenNoWindowMatches() throws Exception {
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any())).thenReturn(forecast(Set.of()));

        mvc.perform(get("/").param("startDate", today.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Leider keine passenden Zeitfenster")));
    }

    @Test
    void showsMessageWhenWeatherServiceFails() throws Exception {
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any()))
                .thenThrow(new ResourceAccessException("timeout"));

        mvc.perform(get("/"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("konnten nicht geladen werden")));
    }

    @Test
    void handlesForecastWithoutUsableHoursWithoutCrash() throws Exception {
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any())).thenReturn(List.of());

        mvc.perform(get("/").param("startDate", today.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("keine verwertbaren Wetterdaten")));
    }

    @ParameterizedTest(name = "{0}={1}")
    @CsvSource({
            "activity, FLYING, Aktivität",
            "temperaturePreference, HOT, Temperatur",
            "windPreference, STORM, Wind",
            "precipitationPreference, SNOW, Niederschlag",
            "minDurationMinutes, abc, Mindestdauer",
            "minDurationMinutes, 0, Mindestdauer",
            "minDurationMinutes, -5, Mindestdauer",
            "minDurationMinutes, 99999999999, Mindestdauer",
            "startDate, 2026-02-31, Startdatum",
            "endDate, morgen, Enddatum",
            "searchDate, 31.12.2026, Datum",
            "locationId, abc, Ort",
            "locationId, 0, Ort"
    })
    void rejectsInvalidParametersWithFriendlyErrorPage(String name, String value, String label) throws Exception {
        mvc.perform(get("/").param(name, value))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(allOf(
                        containsString("Ungültige Eingabe"),
                        containsString("„" + label + "“"),
                        containsString("href=\"/\""),
                        containsString("Zur Planung"),
                        not(containsString("Whitelabel")),
                        not(containsString("Exception")))));

        verifyNoInteractions(weatherClient);
    }

    @Test
    void passesCurrentSelectionToTheLocationSearch() throws Exception {
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any())).thenReturn(List.of());
        String start = today.plusDays(1).toString();
        String end = today.plusDays(3).toString();

        mvc.perform(get("/")
                        .param("activity", "CYCLING")
                        .param("minDurationMinutes", "90")
                        .param("temperaturePreference", "WARM")
                        .param("windPreference", "MODERATE")
                        .param("precipitationPreference", "LIGHT_RAIN")
                        .param("startDate", start)
                        .param("endDate", end))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("id=\"location-form\""),
                        containsString("action=\"/locations\""),
                        containsString("<input type=\"hidden\" name=\"activity\" value=\"CYCLING\">"),
                        containsString("<input type=\"hidden\" name=\"minDurationMinutes\" value=\"90\">"),
                        containsString("<input type=\"hidden\" name=\"temperaturePreference\" value=\"WARM\">"),
                        containsString("<input type=\"hidden\" name=\"windPreference\" value=\"MODERATE\">"),
                        containsString("<input type=\"hidden\" name=\"precipitationPreference\" value=\"LIGHT_RAIN\">"),
                        containsString("<input type=\"hidden\" name=\"startDate\" value=\"" + start + "\">"),
                        containsString("<input type=\"hidden\" name=\"endDate\" value=\"" + end + "\">"))));
    }
}
