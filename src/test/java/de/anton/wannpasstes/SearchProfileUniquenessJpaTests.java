package de.anton.wannpasstes;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class SearchProfileUniquenessJpaTests {

    @Autowired
    private SearchProfileRepository repository;

    @Autowired
    private EntityManager entityManager;

    private SearchProfile save(String name, String locationName) {
        return repository.saveAndFlush(new SearchProfile(name, 2950159, locationName, Activity.WALKING, 60,
                TemperaturePreference.MILD, WindPreference.WEAK, PrecipitationPreference.DRY));
    }

    @Test
    void comparesNamesIgnoringCaseAndSurroundingWhitespace() {
        SearchProfile saved = save("Evening ride", "Berlin");

        assertTrue(repository.existsByNormalizedName("Evening ride"));
        assertTrue(repository.existsByNormalizedName("  evening RIDE  "));
        assertFalse(repository.existsByNormalizedName("Evening rides"));

        assertFalse(repository.existsByNormalizedNameExcludingId("EVENING RIDE", saved.getId()));
        assertTrue(repository.existsByNormalizedNameExcludingId("EVENING RIDE", saved.getId() + 1));
    }

    @Test
    void storesLocationNameAndAllowsProfilesWithoutIt() {
        SearchProfile withName = save("With name", "  Potsdam ");
        SearchProfile withoutName = save("Without name", null);
        entityManager.clear();

        SearchProfile loaded = repository.findById(withName.getId()).orElseThrow();
        assertEquals("Potsdam", loaded.getLocationName());

        SearchProfile legacy = repository.findById(withoutName.getId()).orElseThrow();
        assertNull(legacy.getLocationName());
        assertEquals("Unbekannter Ort (ID 2950159)", legacy.getLocationLabel());
    }
}
