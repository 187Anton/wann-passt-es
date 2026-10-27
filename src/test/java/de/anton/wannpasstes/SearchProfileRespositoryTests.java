package de.anton.wannpasstes;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class SearchProfileRepositoryTests {

    @Autowired
    private SearchProfileRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndLoadsSearchProfile() {
        SearchProfile profile = new SearchProfile(
                "Evening bike ride",
                2950159,
                Activity.CYCLING,
                90,
                TemperaturePreference.MILD,
                WindPreference.WEAK,
                PrecipitationPreference.DRY);

        SearchProfile saved = repository.saveAndFlush(profile);

        assertNotNull(saved.getId());
        Long id = saved.getId();

        entityManager.clear();

        SearchProfile loaded = repository.findById(id).orElseThrow();

        assertEquals("Evening bike ride", loaded.getName());
        assertEquals(2950159, loaded.getLocationId());
        assertEquals(Activity.CYCLING, loaded.getActivity());
        assertEquals(90, loaded.getMinDurationMinutes());
        assertEquals(TemperaturePreference.MILD,
                loaded.getTemperaturePreference());
        assertEquals(WindPreference.WEAK,
                loaded.getWindPreference());
        assertEquals(PrecipitationPreference.DRY,
                loaded.getPrecipitationPreference());
    }
}