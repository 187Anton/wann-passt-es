package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchProfileController.class)  //lädt Webschicht
class SearchProfileWebMvcTests {

    @Autowired //stellt dem Test das vorbereitete MockMvc-Objekt bereit
    private MockMvc mvc; //verarbeitet simulierte Anfragen durch Spring MVC

  @MockitoBean //ersetzt das Repository durch einen Mock. Dieser speichert keine Daten, kann aber Aufrufe entgegennehmen
    private SearchProfileRepository repository;


    @Test //Leerzeichen entfernt
    void bindsFormValuesAndSavesProfile() throws Exception {
        mvc.perform(validForm().param("profileName", "  Demo walk  ")) //Anfrage durch Spring MVC ausführen
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles"));

        ArgumentCaptor<SearchProfile> saved = ArgumentCaptor.forClass(SearchProfile.class);
        verify(repository).save(saved.capture());
        assertEquals("Demo walk", saved.getValue().getName());
        assertEquals(Activity.WALKING, saved.getValue().getActivity());
    }

    @Test //Mindestdauer prüfen
    void acceptsMinimumDurationOfOneMinute() throws Exception {
        mvc.perform(validForm("WALKING", "1").param("profileName", "Boundary walk"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles"));

        ArgumentCaptor<SearchProfile> saved = ArgumentCaptor.forClass(SearchProfile.class);
        verify(repository).save(saved.capture());
        assertEquals(1, saved.getValue().getMinDurationMinutes());
    }

    @Test //Ungültige Mindestdauer prüfen
    void rejectsInvalidMinimumDurationBeforeCallingRepository() throws Exception {
        mvc.perform(validForm("WALKING", "0").param("profileName", "Demo walk"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsUnknownActivityDuringBinding() throws Exception {
        mvc.perform(validForm("FLYING", "60").param("profileName", "Demo walk"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void escapesUserControlledProfileNameInHtml() throws Exception {
        SearchProfile profile = new SearchProfile("<script>alert(1)</script>",
                123, Activity.WALKING, 60, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY);
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(profile));

        mvc.perform(get("/profiles"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>"))));
    }

    @Test
    void deletesExistingProfileViaPost() throws Exception {
        SearchProfile profile = new SearchProfile("Demo walk", 123,
                Activity.WALKING, 60, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY);
        when(repository.findById(42L)).thenReturn(java.util.Optional.of(profile));

        mvc.perform(post("/profiles/delete").param("id", "42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles"));
        verify(repository).delete(profile);
    }

    @Test
    void explainsMissingProfileInsteadOfFailing() throws Exception {
        when(repository.findById(42L)).thenReturn(java.util.Optional.empty());

        mvc.perform(post("/profiles/delete").param("id", "42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profiles"))
                .andExpect(flash().attribute("profileError",
                        containsString("nicht gefunden")));
    }

    @Test
    void showsLocationNameAndEditLinkOnOverview() throws Exception {
        SearchProfile profile = new SearchProfile("Abendrunde", 123,
                "Freiburg", Activity.CYCLING, 90, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY);
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(profile));

        mvc.perform(get("/profiles"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Freiburg")))
                .andExpect(content().string(containsString("/edit")));
    }

    @Test
    void showsPlaceholderWhenOlderProfileHasNoLocationName() throws Exception {
        SearchProfile profile = new SearchProfile("Altes Profil", 123,
                Activity.WALKING, 60, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY);
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(profile));

        mvc.perform(get("/profiles"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ort-ID 123")))
                .andExpect(content().string(containsString("Altes Profil")));
    }

    @Test
    void rendersEditFormForExistingProfile() throws Exception {
        SearchProfile profile = new SearchProfile("Abendrunde", 123,
                "Freiburg", Activity.CYCLING, 90, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY);
        when(repository.findById(7L)).thenReturn(java.util.Optional.of(profile));

        mvc.perform(get("/profiles/7/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile-edit"))
                .andExpect(content().string(containsString("Profil bearbeiten")))
                .andExpect(content().string(containsString("Abendrunde")))
                .andExpect(content().string(containsString("Freiburg")));
    }

    @Test
    void reportsDuplicateNameOnSave() throws Exception {
        when(repository.existsByNameIgnoreCase("demo walk")).thenReturn(true);

        mvc.perform(validForm().param("profileName", "  demo walk "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?locationId=123&activity=WALKING"
                        + "&minDurationMinutes=60&temperaturePreference=MILD"
                        + "&windPreference=WEAK&precipitationPreference=DRY"))
                .andExpect(flash().attribute("profileError",
                        containsString("gibt es bereits")));
        verify(repository, never()).save(any());
    }

    //Formularanfrage
    private MockHttpServletRequestBuilder validForm() {
        return validForm("WALKING", "60");
    }

    private MockHttpServletRequestBuilder validForm(String activity, String duration) {
        return post("/profiles") //Ziel der Anfrage
                .param("locationId", "123") //Formularwerte
                .param("activity", activity)
                .param("minDurationMinutes", duration)
                .param("temperaturePreference", "MILD")
                .param("windPreference", "WEAK")
                .param("precipitationPreference", "DRY");
    }
}
