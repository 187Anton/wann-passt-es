package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationSearchController.class)
class LocationSearchStateTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GeocodingClient geocodingClient;

    private final GeocodingResponse.Location freiburg = new GeocodingResponse.Location(
            123, "Freiburg", 47.99, 7.85, "Europe/Berlin", "Deutschland",
            "Baden-Württemberg");

    @Test
    void keepsThePlanningSelectionWhenALocationIsChosen() throws Exception {
        when(geocodingClient.search("Frei")).thenReturn(List.of(freiburg));

        mvc.perform(get("/locations")
                        .param("query", "Frei")
                        .param("activity", "CYCLING")
                        .param("minDurationMinutes", "90")
                        .param("temperaturePreference", "WARM")
                        .param("windPreference", "MODERATE")
                        .param("precipitationPreference", "DRY")
                        .param("startDate", "2026-11-22")
                        .param("endDate", "2026-11-24"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("locationId=123")))
                .andExpect(content().string(containsString("activity=CYCLING")))
                .andExpect(content().string(containsString("minDurationMinutes=90")))
                .andExpect(content().string(containsString("temperaturePreference=WARM")))
                .andExpect(content().string(containsString("windPreference=MODERATE")))
                .andExpect(content().string(containsString("precipitationPreference=DRY")))
                .andExpect(content().string(containsString("startDate=2026-11-22")))
                .andExpect(content().string(containsString("endDate=2026-11-24")))
                .andExpect(content().string(containsString("name=\"activity\"")));
    }

    @Test
    void linksBackToTheEditedProfileWhenStartedFromThere() throws Exception {
        when(geocodingClient.search("Frei")).thenReturn(List.of(freiburg));

        mvc.perform(get("/locations")
                        .param("query", "Frei")
                        .param("profileId", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/profiles/5/edit")))
                .andExpect(content().string(containsString("locationId=123")))
                .andExpect(content().string(containsString("locationName=Freiburg")));
    }

    @Test
    void omitsEmptyPlanningParameters() throws Exception {
        when(geocodingClient.search("Frei")).thenReturn(List.of(freiburg));

        MvcResult result = mvc.perform(get("/locations").param("query", "Frei"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertTrue(html.contains("locationId=123"), html);
        assertFalse(html.contains("activity="), html);
        assertFalse(html.contains("startDate="), html);
        assertFalse(html.contains("profileId="), html);
    }
}
