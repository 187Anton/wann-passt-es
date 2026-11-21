package de.anton.wannpasstes;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchProfileRepository
        extends JpaRepository<SearchProfile, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
