package de.anton.wannpasstes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The location search keeps the selection of the planning page or of a profile
 * being edited (C4).
 */
@WebMvcTest(LocationSearchController.class)
class LocationSearchWebMvcTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GeocodingClient geocodingClient;

    @BeforeEach
    void setUp() {
        when(geocodingClient.search("Potsdam")).thenReturn(List.of(new GeocodingResponse.Location(
                2852458, "Potsdam", 52.4, 13.07, "Europe/Berlin", "Deutschland", "Brandenburg")));
    }

    @SuppressWarnings("unchecked")
    private static <T> T modelValue(MvcResult result, String name) {
        return (T) result.getModelAndView().getModel().get(name);
    }

    @Test
    void resultLinksKeepPlanningSelection() throws Exception {
        MvcResult result = mvc.perform(get("/locations")
                        .param("query", "Potsdam")
                        .param("locationId", "2950159")
                        .param("activity", "CYCLING")
                        .param("minDurationMinutes", "90")
                        .param("temperaturePreference", "WARM")
                        .param("windPreference", "MODERATE")
                        .param("precipitationPreference", "DRY")
                        .param("startDate", "2026-11-23")
                        .param("endDate", "2026-11-25"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Brandenburg, Deutschland")))
                .andReturn();

        List<LocationSearchController.LocationChoice> choices = modelValue(result, "choices");
        assertEquals("/?locationId=2852458&activity=CYCLING&minDurationMinutes=90"
                        + "&temperaturePreference=WARM&windPreference=MODERATE&precipitationPreference=DRY"
                        + "&startDate=2026-11-23&endDate=2026-11-25",
                choices.getFirst().href());

        // Searching again keeps the selection in hidden fields.
        Map<String, String> hidden = modelValue(result, "hiddenFields");
        assertEquals("CYCLING", hidden.get("activity"));
        assertEquals("90", hidden.get("minDurationMinutes"));
        assertEquals("2026-11-25", hidden.get("endDate"));
        assertEquals("2950159", hidden.get("locationId"));

        // The back link returns to the old location with the same selection.
        String back = modelValue(result, "backHref");
        assertEquals("/?locationId=2950159&activity=CYCLING&minDurationMinutes=90"
                        + "&temperaturePreference=WARM&windPreference=MODERATE&precipitationPreference=DRY"
                        + "&startDate=2026-11-23&endDate=2026-11-25", back);
    }

    @Test
    void resultLinksWithoutSelectionOnlyContainTheLocation() throws Exception {
        MvcResult result = mvc.perform(get("/locations").param("query", "Potsdam"))
                .andExpect(status().isOk())
                .andReturn();

        List<LocationSearchController.LocationChoice> choices = modelValue(result, "choices");
        assertEquals("/?locationId=2852458", choices.getFirst().href());
    }

    @Test
    void resultLinksReturnToProfileBeingEdited() throws Exception {
        MvcResult result = mvc.perform(get("/locations")
                        .param("query", "Potsdam")
                        .param("profileId", "7")
                        .param("profileName", "Rad & Ruhe+")
                        .param("activity", "HIKING")
                        .param("minDurationMinutes", "120")
                        .param("temperaturePreference", "COOL")
                        .param("windPreference", "ANY")
                        .param("precipitationPreference", "LIGHT_RAIN"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("neuen Ort für das Suchprofil")))
                .andReturn();

        List<LocationSearchController.LocationChoice> choices = modelValue(result, "choices");
        assertEquals("/profiles/7/edit?locationId=2852458&locationName=Potsdam"
                        + "&profileName=Rad%20%26%20Ruhe%2B&activity=HIKING&minDurationMinutes=120"
                        + "&temperaturePreference=COOL&windPreference=ANY&precipitationPreference=LIGHT_RAIN",
                choices.getFirst().href());
    }

    @Test
    void rejectsInvalidSelectionWithFriendlyErrorPage() throws Exception {
        mvc.perform(get("/locations").param("query", "Potsdam").param("activity", "FLYING"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Ungültige Eingabe")));

        verifyNoInteractions(geocodingClient);
    }

    @Test
    void showsMessageWhenSearchServiceFails() throws Exception {
        when(geocodingClient.search("Berlin")).thenThrow(new ResourceAccessException("timeout"));

        mvc.perform(get("/locations").param("query", "Berlin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Die Ortssuche ist gerade nicht verfügbar")));
    }
}
