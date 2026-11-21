package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

class SearchProfileControllerTests {

    private final SearchProfileRepository repository =
            mock(SearchProfileRepository.class);

    private final GeocodingClient geocodingClient = mock(GeocodingClient.class);
    private final SearchProfileController controller = new SearchProfileController(repository,
            new SearchProfileService(repository, geocodingClient), geocodingClient);

    @Test
    void savesProfileAndRedirectsToOverview() {
        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        when(geocodingClient.getLocation(2950159)).thenReturn(new GeocodingResponse.Location(
                2950159, "Testort", 52.5, 13.4, "Europe/Berlin", null, null));
        String view = controller.saveProfile(new ProfileForm(
                "  Evening bike ride  ",
                2950159,
                Activity.CYCLING,
                90,
                TemperaturePreference.MILD,
                WindPreference.WEAK,
                PrecipitationPreference.DRY),
                null, null, LocalDate.of(2026, 10, 28),
                redirect);

        ArgumentCaptor<SearchProfile> captor =
                ArgumentCaptor.forClass(SearchProfile.class);

        verify(repository).saveAndFlush(captor.capture());

        SearchProfile saved = captor.getValue();

        assertEquals("Evening bike ride", saved.getName());
        assertEquals(2950159, saved.getLocationId());
        assertEquals("Testort", saved.getLocationName());
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
    void rejectsBlankNameAndPreservesSelection() {
        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view = controller.saveProfile(new ProfileForm(
                "   ",
                2950159,
                Activity.CYCLING,
                90,
                TemperaturePreference.MILD,
                WindPreference.WEAK,
                PrecipitationPreference.DRY),
                null, null, LocalDate.of(2026, 10, 28),
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
        assertEquals("2026-10-28", redirect.get("searchDate"));
    }
}