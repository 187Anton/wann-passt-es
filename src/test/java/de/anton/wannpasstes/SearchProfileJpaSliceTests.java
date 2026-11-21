package de.anton.wannpasstes;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SearchProfileJpaSliceTests {

    @Autowired
    private SearchProfileRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndReloadsProfileWithoutLoadingWebLayer() {
        SearchProfile saved = repository.saveAndFlush(profile("Jpa demo profile"));
        entityManager.clear();

        SearchProfile loaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Jpa demo profile", loaded.getName());
        assertEquals(Activity.CYCLING, loaded.getActivity());
        assertEquals(90, loaded.getMinDurationMinutes());
    }

    @Test
    void rejectsBlankProfileNameWhenFlushingToDatabase() {
        assertThrows(ConstraintViolationException.class,
                () -> repository.saveAndFlush(profile(" ")));
    }

    private SearchProfile profile(String name) {
        return new SearchProfile(name, 123, Activity.CYCLING, 90,
                TemperaturePreference.MILD, WindPreference.WEAK,
                PrecipitationPreference.DRY);
    }
}
