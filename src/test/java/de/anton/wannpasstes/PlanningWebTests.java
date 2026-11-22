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
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({MainpageController.class, LocationSearchController.class})
@Import(TimeWindowService.class)
class PlanningWebTests {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final GeocodingResponse.Location LOCATION =
            new GeocodingResponse.Location(2950159, "Testort", 52.5, 13.4, ZONE.getId(), "Testland", "Testregion");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private WeatherClient weatherClient;

    @MockitoBean
    private GeocodingClient geocodingClient;

    @BeforeEach
    void stubLocation() {
        when(geocodingClient.getLocation(LOCATION.id())).thenReturn(LOCATION);
    }

    @Test
    void defaultsToTodayInLocationTimezoneAndIgnoresThePast() throws Exception {
        GeocodingResponse.Location location = new GeocodingResponse.Location(108, "Testinsel",
                1.5, -157.4, "Pacific/Kiritimati", null, null);
        ZoneId islandZone = ZoneId.of(location.timezone());
        LocalDate today = LocalDate.now(islandZone);
        OffsetDateTime tomorrow = today.plusDays(1).atStartOfDay(islandZone).toOffsetDateTime();
        OffsetDateTime yesterday = today.minusDays(1).atStartOfDay(islandZone).toOffsetDateTime();
        when(geocodingClient.getLocation(108)).thenReturn(location);
        when(weatherClient.fetchForecast(location.latitude(), location.longitude(), islandZone))
                .thenReturn(List.of(hour(yesterday), hour(yesterday.plusHours(1)),
                        hour(tomorrow), hour(tomorrow.plusHours(1))));

        mvc.perform(get("/").param("locationId", "108"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("startDate", today))
                .andExpect(model().attribute("endDate", today))
                .andExpect(model().attribute("suitableWindows", List.of()))
                .andExpect(content().string(containsString("Keine passenden Zeitfenster")))
                .andExpect(content().string(containsString("value=\"" + today + "\"")));
    }

    @Test
    void rejectsReversedAndOutOfRangeDatesWithoutWeatherRequest() throws Exception {
        LocalDate today = LocalDate.now(ZONE);
        for (List<LocalDate> range : List.of(List.of(today.plusDays(2), today.plusDays(1)),
                List.of(today.minusDays(1), today), List.of(today, today.plusDays(7)))) {
            mvc.perform(get("/").param("startDate", range.getFirst().toString())
                            .param("endDate", range.getLast().toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Das Enddatum darf nicht vor dem Startdatum liegen.")));
        }
        verifyNoInteractions(weatherClient);
    }

    @Test
    void findsSortedWindowsAcrossDaysAndCountsTimeAcrossMidnight() throws Exception {
        LocalDate start = LocalDate.now(ZONE).plusDays(1);
        OffsetDateTime evening = start.atTime(22, 0).atZone(ZONE).toOffsetDateTime();
        OffsetDateTime nextAfternoon = start.plusDays(1).atTime(14, 0).atZone(ZONE).toOffsetDateTime();
        var crossMidnight = new TimeWindow(evening, evening.plusHours(3));
        var afternoon = new TimeWindow(nextAfternoon, nextAfternoon.plusHours(3));
        when(weatherClient.fetchForecast(LOCATION.latitude(), LOCATION.longitude(), ZONE))
                .thenReturn(List.of(hour(nextAfternoon), hour(nextAfternoon.plusHours(1)), hour(nextAfternoon.plusHours(2)),
                        hour(nextAfternoon.plusHours(3)), hour(evening.plusHours(3)), hour(evening.plusHours(1)),
                        new WeatherHour(17.5, evening, 8.5, 2, 0), hour(evening.plusHours(2))));

        mvc.perform(get("/").param("startDate", start.toString()).param("endDate", start.plusDays(1).toString())
                        .param("minDurationMinutes", "180"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("suitableWindows", List.of(crossMidnight, afternoon)))
                .andExpect(content().string(containsString("3 Std.")))
                .andExpect(content().string(containsString("01:00")))
                .andExpect(content().string(containsString("--from: 1320; --to: 1440")))
                .andExpect(content().string(containsString("--from: 0; --to: 60")))
                .andExpect(content().string(containsString("17,5–18 °C")))
                .andExpect(content().string(containsString("8,5–10 km/h")))
                .andExpect(content().string(containsString("<dd>0 mm/h</dd>")));

        mvc.perform(get("/").param("searchDate", start.toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("startDate", start))
                .andExpect(model().attribute("endDate", start))
                .andExpect(model().attribute("suitableWindows", List.of(new TimeWindow(evening, evening.plusHours(2)))))
                .andExpect(content().string(containsString(">24:00</time>")));
    }

    @Test
    void showsFriendlyMessageWhenForecastCannotBeUsed() throws Exception {
        when(weatherClient.fetchForecast(LOCATION.latitude(), LOCATION.longitude(), ZONE))
                .thenThrow(new RestClientException("Weather API returned no usable forecast hours"));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Der Ort oder die Wetterdaten konnten nicht geladen werden.")))
                .andExpect(content().string(not(containsString("no usable forecast hours"))));
    }

    @ParameterizedTest
    @CsvSource({"activity,FLYING", "minDurationMinutes,abc", "minDurationMinutes,0",
            "minDurationMinutes,-1", "startDate,2026-02-30", "searchDate,gestern", "locationId,0",
            "windPreference,STURM"})
    void rendersGermanBadRequestForInvalidInput(String parameter, String value) throws Exception {
        mvc.perform(get("/").param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Bitte prüfe deine Eingaben")))
                .andExpect(content().string(containsString("Zur Planung")))
                .andExpect(content().string(not(containsString("Whitelabel"))));
        verifyNoInteractions(weatherClient, geocodingClient);
    }

    @Test
    void keepsAllPlanningParametersInTheLocationSelectionForm() throws Exception {
        when(geocodingClient.search("Testort")).thenReturn(List.of(LOCATION));
        LocalDate first = LocalDate.now(ZONE).plusDays(2);
        LocalDate last = first.plusDays(1);

        String html = mvc.perform(get("/locations").param("query", "Testort").param("locationId", "42")
                        .param("startDate", first.toString()).param("endDate", last.toString())
                        .param("activity", "CYCLING").param("minDurationMinutes", "120")
                        .param("temperaturePreference", "WARM").param("windPreference", "MODERATE")
                        .param("precipitationPreference", "DRY"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var resultForm = Pattern.compile("(?s)<li class=\"panel location-result\">.*?<form[^>]*>(.*?)</form>")
                .matcher(html);
        assertTrue(resultForm.find());
        var expected = Map.of("locationId", "2950159", "startDate", first.toString(), "endDate", last.toString(),
                "activity", "CYCLING", "minDurationMinutes", "120", "temperaturePreference", "WARM",
                "windPreference", "MODERATE", "precipitationPreference", "DRY");
        expected.forEach((name, value) -> {
            String input = "<input(?=[^>]*name=\"" + name + "\")(?=[^>]*value=\"" + value + "\")[^>]*>";
            assertTrue(Pattern.compile(input).matcher(resultForm.group(1)).find(), name);
        });
    }

    private static WeatherHour hour(OffsetDateTime time) {
        return new WeatherHour(18, time, 10, 0, 0);
    }
}
