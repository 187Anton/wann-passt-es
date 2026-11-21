package de.anton.wannpasstes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Editing profiles (A2, A5–A8).
 */
@WebMvcTest(SearchProfileEditController.class)
class SearchProfileEditWebMvcTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SearchProfileRepository repository;

    @MockitoBean
    private GeocodingClient geocodingClient;

    private SearchProfile profile;

    @BeforeEach
    void setUp() {
        profile = new SearchProfile("Evening ride", 2950159, "Berlin", Activity.CYCLING, 90,
                TemperaturePreference.MILD, WindPreference.WEAK, PrecipitationPreference.DRY);
        ReflectionTestUtils.setField(profile, "id", 7L);
        when(repository.findById(7L)).thenReturn(Optional.of(profile));
        when(repository.findById(99L)).thenReturn(Optional.empty());
    }

    private static ProfileForm form(MvcResult result) {
        return (ProfileForm) result.getModelAndView().getModel().get("profileForm");
    }

    private MockHttpServletRequestBuilder editForm(String name, String minDuration) {
        return post("/profiles/7/edit")
                .param("profileName", name)
                .param("locationId", "2852458")
                .param("locationName", "Potsdam")
                .param("activity", "HIKING")
                .param("minDurationMinutes", minDuration)
                .param("temperaturePreference", "COOL")
                .param("windPreference", "MODERATE")
                .param("precipitationPreference", "LIGHT_RAIN");
    }

    @Test
    void showsFormWithStoredValues() throws Exception {
        mvc.perform(get("/profiles/7/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Suchprofil bearbeiten"),
                        containsString("value=\"Evening ride\""),
                        containsString("Berlin"),
                        containsString("<option value=\"CYCLING\" selected=\"selected\">"),
                        containsString("value=\"90\""))));

        verifyNoInteractions(geocodingClient);
    }

    @Test
    void showsStoredWeatherWishesAsCheckedRadioButtons() throws Exception {
        String html = mvc.perform(get("/profiles/7/edit"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(List.of("MILD"), RadioMarkup.checked(html, "temperaturePreference"));
        assertEquals(List.of("WEAK"), RadioMarkup.checked(html, "windPreference"));
        assertEquals(List.of("DRY"), RadioMarkup.checked(html, "precipitationPreference"));
    }

    @Test
    void appliesValuesReturnedFromLocationSearch() throws Exception {
        MvcResult result = mvc.perform(get("/profiles/7/edit")
                        .param("locationId", "2852458")
                        .param("locationName", "Potsdam")
                        .param("profileName", "Unsaved name")
                        .param("minDurationMinutes", "45")
                        .param("windPreference", "ANY"))
                .andExpect(status().isOk())
                .andReturn();

        ProfileForm form = form(result);
        assertEquals("Unsaved name", form.getProfileName());
        assertEquals(2852458, form.getLocationId());
        assertEquals("Potsdam", form.getLocationName());
        assertEquals(45, form.getMinDurationMinutes());
        assertEquals(WindPreference.ANY, form.getWindPreference());
        assertEquals(Activity.CYCLING, form.getActivity());
    }

    @Test
    void looksUpLocationNameForOlderProfiles() throws Exception {
        profile.update("Legacy", 2950159, null, Activity.WALKING, 60,
                TemperaturePreference.MILD, WindPreference.WEAK, PrecipitationPreference.DRY);
        when(geocodingClient.getLocation(2950159)).thenReturn(new GeocodingResponse.Location(
                2950159, "Berlin", 52.52, 13.405, "Europe/Berlin", "Deutschland", "Land Berlin"));

        MvcResult result = mvc.perform(get("/profiles/7/edit"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("Berlin", form(result).getLocationName());
    }

    @Test
    void showsPlaceholderWhenLocationNameCannotBeDetermined() throws Exception {
        profile.update("Legacy", 2950159, null, Activity.WALKING, 60,
                TemperaturePreference.MILD, WindPreference.WEAK, PrecipitationPreference.DRY);
        when(geocodingClient.getLocation(2950159)).thenThrow(new ResourceAccessException("offline"));

        mvc.perform(get("/profiles/7/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Unbekannter Ort (ID 2950159)")));
    }

    @Test
    void updatesAllFieldsAndShowsSuccessMessage() throws Exception {
        mvc.perform(editForm("  Mountain hike ", "120"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles"))
                .andExpect(flash().attribute("profileSuccess", "Das Suchprofil „Mountain hike“ wurde aktualisiert."));

        verify(repository).save(profile);
        assertEquals("Mountain hike", profile.getName());
        assertEquals(2852458, profile.getLocationId());
        assertEquals("Potsdam", profile.getLocationName());
        assertEquals(Activity.HIKING, profile.getActivity());
        assertEquals(120, profile.getMinDurationMinutes());
        assertEquals(TemperaturePreference.COOL, profile.getTemperaturePreference());
        assertEquals(WindPreference.MODERATE, profile.getWindPreference());
        assertEquals(PrecipitationPreference.LIGHT_RAIN, profile.getPrecipitationPreference());
    }

    @Test
    void profileMayKeepItsOwnName() throws Exception {
        mvc.perform(editForm(" evening RIDE ", "90"))
                .andExpect(status().is3xxRedirection());

        // uniqueness is checked against all other profiles only
        verify(repository).existsByNormalizedNameExcludingId("evening RIDE", 7L);
        verify(repository).save(profile);
    }

    @Test
    void rejectsNameOfAnotherProfileAndKeepsInput() throws Exception {
        when(repository.existsByNormalizedNameExcludingId("Morning run", 7L)).thenReturn(true);

        mvc.perform(editForm("Morning run", "120"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("Es gibt bereits ein Suchprofil mit dem Namen „Morning run“"),
                        containsString("Das Profil wurde noch nicht gespeichert."),
                        containsString("value=\"Morning run\""),
                        containsString("value=\"120\""),
                        containsString("Potsdam"),
                        containsString("<option value=\"HIKING\" selected=\"selected\">"))));

        verify(repository, never()).save(any());
        assertEquals("Evening ride", profile.getName());
    }

    @Test
    void rejectsBlankAndTooLongNames() throws Exception {
        mvc.perform(editForm("   ", "120"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(ProfileNames.LENGTH_MESSAGE)));
        mvc.perform(editForm("x".repeat(101), "120"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(ProfileNames.LENGTH_MESSAGE)));

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsInvalidMinimumDurationInsideTheForm() throws Exception {
        mvc.perform(editForm("Mountain hike", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("Mindestdauer von mindestens 1 Minute"),
                        containsString("value=\"Mountain hike\""))));

        mvc.perform(editForm("Mountain hike", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(allOf(
                        containsString("Bitte gib eine ganze Zahl von mindestens 1 Minute ein."),
                        containsString("value=\"Mountain hike\""),
                        not(containsString("Failed to convert")))));

        // browsers send an empty value for an invalid number input
        mvc.perform(editForm("Mountain hike", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Mindestdauer von mindestens 1 Minute")));

        verify(repository, never()).save(any());
    }

    @Test
    void keepsStoredLocationNameWhenLocationIsUnchanged() throws Exception {
        mvc.perform(post("/profiles/7/edit")
                        .param("profileName", "Evening ride")
                        .param("locationId", "2950159")
                        .param("activity", "CYCLING")
                        .param("minDurationMinutes", "60")
                        .param("temperaturePreference", "MILD")
                        .param("windPreference", "WEAK")
                        .param("precipitationPreference", "DRY"))
                .andExpect(status().is3xxRedirection());

        assertEquals("Berlin", profile.getLocationName());
        assertEquals(60, profile.getMinDurationMinutes());
    }

    @Test
    void unknownProfileShowsFriendlyNotFoundPage() throws Exception {
        mvc.perform(get("/profiles/99/edit"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(allOf(
                        containsString("Suchprofil nicht gefunden"),
                        containsString("href=\"/profiles\""),
                        not(containsString("Whitelabel")))));

        mvc.perform(editForm("Anything", "60").with(request -> {
                    request.setRequestURI("/profiles/99/edit");
                    return request;
                }))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Suchprofil nicht gefunden")));

        verify(repository, never()).save(any());
    }

    @Test
    void invalidProfileIdShowsFriendlyErrorPage() throws Exception {
        mvc.perform(get("/profiles/abc/edit"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Ungültige Eingabe")));

        verify(repository, never()).findById(anyLong());
    }
}
