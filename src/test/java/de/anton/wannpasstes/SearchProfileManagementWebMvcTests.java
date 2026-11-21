package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Overview, creation with unique names and deletion of profiles (package A).
 */
@WebMvcTest(SearchProfileController.class)
class SearchProfileManagementWebMvcTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SearchProfileRepository repository;

    private static SearchProfile profile(long id, String name, String locationName) {
        SearchProfile profile = new SearchProfile(name, 2950159, locationName, Activity.CYCLING, 90,
                TemperaturePreference.MILD, WindPreference.WEAK, PrecipitationPreference.DRY);
        ReflectionTestUtils.setField(profile, "id", id);
        return profile;
    }

    private MockHttpServletRequestBuilder createForm(String name) {
        return post("/profiles")
                .param("profileName", name)
                .param("locationId", "2950159")
                .param("locationName", "Berlin")
                .param("activity", "CYCLING")
                .param("minDurationMinutes", "90")
                .param("temperaturePreference", "MILD")
                .param("windPreference", "WEAK")
                .param("precipitationPreference", "DRY")
                .param("startDate", "2026-11-23")
                .param("endDate", "2026-11-24");
    }

    @Test
    void overviewShowsLocationNameAndPlaceholderForOlderProfiles() throws Exception {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(
                profile(1, "With name", "Potsdam"),
                profile(2, "Legacy profile", null)));

        mvc.perform(get("/profiles"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Potsdam"),
                        containsString("Unbekannter Ort (ID 2950159)"),
                        containsString("href=\"/profiles/1/edit\""),
                        containsString("href=\"/profiles/2/edit\""),
                        // the legacy profile stays usable
                        containsString("locationId=2950159"))));
    }

    @Test
    void savesLocationNameTogetherWithProfile() throws Exception {
        mvc.perform(createForm("  Evening ride "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles"))
                .andExpect(flash().attribute("profileSuccess", "Das Suchprofil „Evening ride“ wurde gespeichert."));

        ArgumentCaptor<SearchProfile> saved = ArgumentCaptor.forClass(SearchProfile.class);
        verify(repository).save(saved.capture());
        assertEquals("Evening ride", saved.getValue().getName());
        assertEquals("Berlin", saved.getValue().getLocationName());
        verify(repository).existsByNormalizedName("Evening ride");
    }

    @Test
    void rejectsDuplicateNameAndKeepsSelection() throws Exception {
        when(repository.existsByNormalizedName("evening RIDE")).thenReturn(true);

        mvc.perform(createForm(" evening RIDE "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?locationId=2950159&activity=CYCLING&minDurationMinutes=90"
                        + "&temperaturePreference=MILD&windPreference=WEAK&precipitationPreference=DRY"
                        + "&startDate=2026-11-23&endDate=2026-11-24"))
                .andExpect(flash().attribute("profileError",
                        "Es gibt bereits ein Suchprofil mit dem Namen „evening RIDE“. Bitte wähle einen anderen Namen."))
                .andExpect(flash().attribute("profileName", " evening RIDE "));

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsTooLongName() throws Exception {
        mvc.perform(createForm("x".repeat(101)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("profileError", ProfileNames.LENGTH_MESSAGE));

        verify(repository, never()).save(any());
    }

    @Test
    void deletingUnknownProfileShowsMessage() throws Exception {
        when(repository.existsById(99L)).thenReturn(false);

        mvc.perform(post("/profiles/delete").param("id", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles"))
                .andExpect(flash().attribute("profileError",
                        "Dieses Suchprofil gibt es nicht (mehr). Vielleicht wurde es bereits gelöscht."))
                .andExpect(flash().attributeCount(1));
    }

    @Test
    void deletingExistingProfileShowsSuccess() throws Exception {
        when(repository.existsById(5L)).thenReturn(true);

        mvc.perform(post("/profiles/delete").param("id", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("profileSuccess", "Das Suchprofil wurde entfernt."));

        verify(repository).deleteById(5L);
    }

    @Test
    void invalidProfileIdShowsFriendlyErrorPage() throws Exception {
        mvc.perform(post("/profiles/delete").param("id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("Ungültige Eingabe"),
                        not(containsString("Whitelabel")))));
    }

    @Test
    void showsMessagesOnOverview() throws Exception {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of());

        mvc.perform(get("/profiles").flashAttr("profileSuccess", "Das Suchprofil wurde entfernt."))
                .andExpect(content().string(allOf(
                        containsString("Das Suchprofil wurde entfernt."),
                        containsString("Noch keine Suchprofile gespeichert"))));
    }
}
