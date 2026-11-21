package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SearchProfileControllerTests {

    private final SearchProfileRepository repository =
            mock(SearchProfileRepository.class);

    private final SearchProfileController controller =
            new SearchProfileController(repository); //Controller selbst wird erzeugt

    @Test
    void savesProfileAndRedirectsToOverview() {
        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view = controller.saveProfile(
                "  Evening bike ride  ",
                2950159,
                " Berlin ",
                Activity.CYCLING,
                90,
                TemperaturePreference.MILD,
                WindPreference.WEAK,
                PrecipitationPreference.DRY,
                "2026-10-28",
                "2026-10-30",
                null,
                redirect);

        ArgumentCaptor<SearchProfile> captor =
                ArgumentCaptor.forClass(SearchProfile.class);

        verify(repository).save(captor.capture());

        SearchProfile saved = captor.getValue();

        assertEquals("Evening bike ride", saved.getName());
        assertEquals(2950159, saved.getLocationId());
        assertEquals("Berlin", saved.getLocationName());
        assertEquals(Activity.CYCLING, saved.getActivity());
        assertEquals(90, saved.getMinDurationMinutes());
        assertEquals(TemperaturePreference.MILD,
                saved.getTemperaturePreference());
        assertEquals(WindPreference.WEAK,
                saved.getWindPreference());
        assertEquals(PrecipitationPreference.DRY,
                saved.getPrecipitationPreference());

        assertEquals("redirect:/profiles", view);
        assertTrue(
                redirect.getFlashAttributes().containsKey("profileSuccess"));
    }

    @Test
    void storesMissingLocationNameAsNull() {
        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        controller.saveProfile("Namenlos", 123, "  ", Activity.WALKING, 60,
                TemperaturePreference.MILD, WindPreference.WEAK,
                PrecipitationPreference.DRY, null, null, null, redirect);

        ArgumentCaptor<SearchProfile> captor =
                ArgumentCaptor.forClass(SearchProfile.class);
        verify(repository).save(captor.capture());

        assertNull(captor.getValue().getLocationName());
    }

    @Test
    void rejectsBlankNameAndPreservesSelection() {
        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view = controller.saveProfile(
                "   ",
                2950159,
                "Berlin",
                Activity.CYCLING,
                90,
                TemperaturePreference.MILD,
                WindPreference.WEAK,
                PrecipitationPreference.DRY,
                "2026-10-28",
                "2026-10-30",
                null,
                redirect);

        verifyNoInteractions(repository);

        assertEquals("redirect:/", view);
        assertTrue(
                redirect.getFlashAttributes().containsKey("profileError"));

        assertEquals("2950159", redirect.get("locationId"));
        assertEquals("CYCLING", redirect.get("activity"));
        assertEquals("90", redirect.get("minDurationMinutes"));
        assertEquals("MILD", redirect.get("temperaturePreference"));
        assertEquals("WEAK", redirect.get("windPreference"));
        assertEquals("DRY", redirect.get("precipitationPreference"));
        assertEquals("2026-10-28", redirect.get("startDate"));
        assertEquals("2026-10-30", redirect.get("endDate"));
    }

    @Test
    void rejectsDuplicateNameIgnoringCaseAndWhitespace() {
        when(repository.existsByNameIgnoreCase("abendrunde")).thenReturn(true);

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view = controller.saveProfile(
                "  abendrunde ",
                2950159,
                "Berlin",
                Activity.WALKING,
                60,
                TemperaturePreference.MILD,
                WindPreference.WEAK,
                PrecipitationPreference.DRY,
                null,
                null,
                null,
                redirect);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
        assertEquals("redirect:/", view);

        String error = (String) redirect.getFlashAttributes().get("profileError");
        assertTrue(error.contains("gibt es bereits"), error);
        assertEquals("  abendrunde ", redirect.getFlashAttributes().get("profileName"),
                "the raw input is kept so the user can correct it");
        assertEquals("2950159", redirect.get("locationId"));
    }

    @Test
    void updatesProfileAndRedirectsToOverview() {
        SearchProfile profile = new SearchProfile("Abendrunde", 2950159,
                "Berlin", Activity.CYCLING, 90, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY);
        when(repository.findById(7L)).thenReturn(Optional.of(profile));
        when(repository.existsByNameIgnoreCaseAndIdNot("Morgenrunde", 7L))
                .thenReturn(false);

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view = controller.updateProfile(7, " Morgenrunde ", 123,
                "Freiburg", Activity.HIKING, 45, TemperaturePreference.WARM,
                WindPreference.MODERATE, PrecipitationPreference.LIGHT_RAIN,
                new ExtendedModelMap(), redirect);

        assertEquals("redirect:/profiles", view);
        assertEquals("Morgenrunde", profile.getName());
        assertEquals(123, profile.getLocationId());
        assertEquals("Freiburg", profile.getLocationName());
        assertEquals(Activity.HIKING, profile.getActivity());
        assertEquals(45, profile.getMinDurationMinutes());
        assertEquals(TemperaturePreference.WARM, profile.getTemperaturePreference());
        verify(repository).save(profile);
        assertTrue(redirect.getFlashAttributes().containsKey("profileSuccess"));
    }

    @Test
    void keepsItsOwnNameWhenTheNameIsUnchanged() {
        SearchProfile profile = new SearchProfile("Abendrunde", 2950159,
                "Berlin", Activity.CYCLING, 90, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY);
        when(repository.findById(7L)).thenReturn(Optional.of(profile));
        when(repository.existsByNameIgnoreCaseAndIdNot("Abendrunde", 7L))
                .thenReturn(false);

        String view = controller.updateProfile(7, "Abendrunde", 2950159,
                "Berlin", Activity.CYCLING, 90, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY,
                new ExtendedModelMap(), new RedirectAttributesModelMap());

        assertEquals("redirect:/profiles", view);
        verify(repository).save(profile);
    }

    @Test
    void showsEditFormAgainWhenNameIsInvalid() {
        SearchProfile profile = new SearchProfile("Abendrunde", 2950159,
                "Berlin", Activity.CYCLING, 90, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY);
        when(repository.findById(7L)).thenReturn(Optional.of(profile));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.updateProfile(7, "  ", 123, "Freiburg",
                Activity.HIKING, 45, TemperaturePreference.WARM,
                WindPreference.MODERATE, PrecipitationPreference.LIGHT_RAIN,
                model, new RedirectAttributesModelMap());

        assertEquals("profile-edit", view);
        assertTrue(model.get("profileError") != null);
        assertEquals("  ", model.get("profileName"), "raw input is preserved");
        assertEquals(123, model.get("locationId"));
        assertEquals("Freiburg", model.get("locationName"));
        assertEquals(45, model.get("minDurationMinutes"));
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void showsEditFormAgainWhenAnotherProfileUsesTheName() {
        SearchProfile profile = new SearchProfile("Abendrunde", 2950159,
                "Berlin", Activity.CYCLING, 90, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY);
        when(repository.findById(7L)).thenReturn(Optional.of(profile));
        when(repository.existsByNameIgnoreCaseAndIdNot("Morgenrunde", 7L))
                .thenReturn(true);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.updateProfile(7, "Morgenrunde", 2950159,
                "Berlin", Activity.CYCLING, 90, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY,
                model, new RedirectAttributesModelMap());

        assertEquals("profile-edit", view);
        String error = (String) model.get("profileError");
        assertTrue(error.contains("anderes Suchprofil"), error);
        assertEquals("Morgenrunde", model.get("profileName"));
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void explainsWhenProfileToEditDoesNotExist() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view = controller.editProfile(404, null, null,
                new ExtendedModelMap(), redirect);

        assertEquals("redirect:/profiles", view);
        assertTrue(((String) redirect.getFlashAttributes().get("profileError"))
                .contains("nicht gefunden"));
    }

    @Test
    void explainsWhenProfileToDeleteDoesNotExist() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view = controller.deleteProfile(404, redirect);

        assertEquals("redirect:/profiles", view);
        assertTrue(((String) redirect.getFlashAttributes().get("profileError"))
                .contains("nicht gefunden"));
        verify(repository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deletesExistingProfile() {
        SearchProfile profile = new SearchProfile("Abendrunde", 2950159,
                "Berlin", Activity.CYCLING, 90, TemperaturePreference.MILD,
                WindPreference.WEAK, PrecipitationPreference.DRY);
        when(repository.findById(7L)).thenReturn(Optional.of(profile));

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        assertEquals("redirect:/profiles", controller.deleteProfile(7, redirect));
        verify(repository).delete(profile);
        assertTrue(((String) redirect.getFlashAttributes().get("profileSuccess"))
                .contains("Abendrunde"));
    }
}
