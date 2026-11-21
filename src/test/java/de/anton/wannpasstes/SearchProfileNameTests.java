package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class SearchProfileNameTests {

    @Autowired
    private SearchProfileRepository repository;

    @Test
    void findsNamesIgnoringCaseAndSurroundingWhitespace() {
        SearchProfile saved = repository.saveAndFlush(profile("Abendrunde"));

        assertTrue(repository.existsByNameIgnoreCase("abendrunde"));
        assertTrue(repository.existsByNameIgnoreCase("ABENDRUNDE"));
        assertTrue(repository.existsByNameIgnoreCase("Abendrunde"));
        assertFalse(repository.existsByNameIgnoreCase("Morgenrunde"));
    }

    @Test
    void ignoresTheProfileItselfWhenCheckingForUpdates() {
        SearchProfile saved = repository.saveAndFlush(profile("Abendrunde"));
        SearchProfile other = repository.saveAndFlush(profile("Morgenrunde"));

        assertFalse(repository.existsByNameIgnoreCaseAndIdNot(
                "abendrunde", saved.getId()));
        assertTrue(repository.existsByNameIgnoreCaseAndIdNot(
                "abendrunde", other.getId()));
    }

    @Test
    void storesTheLocationName() {
        SearchProfile saved = repository.saveAndFlush(
                new SearchProfile("Mit Ort", 123, "Freiburg",
                        Activity.CYCLING, 90, TemperaturePreference.MILD,
                        WindPreference.WEAK, PrecipitationPreference.DRY));

        assertTrue(repository.findById(saved.getId())
                .orElseThrow().getLocationName().equals("Freiburg"));
    }

    private SearchProfile profile(String name) {
        return new SearchProfile(name, 123, "Berlin", Activity.CYCLING, 90,
                TemperaturePreference.MILD, WindPreference.WEAK,
                PrecipitationPreference.DRY);
    }
}
