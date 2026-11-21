package de.anton.wannpasstes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchProfileRepository
        extends JpaRepository<SearchProfile, Long> {

    /**
     * Checks whether a profile with the given name exists. Leading/trailing
     * whitespace and case are ignored on both sides.
     */
    @Query("""
            select count(p) > 0 from SearchProfile p
            where lower(trim(p.name)) = lower(trim(:name))
            """)
    boolean existsByNormalizedName(@Param("name") String name);

    /** Like {@link #existsByNormalizedName(String)}, but ignores the profile with the given ID. */
    @Query("""
            select count(p) > 0 from SearchProfile p
            where lower(trim(p.name)) = lower(trim(:name)) and p.id <> :id
            """)
    boolean existsByNormalizedNameExcludingId(@Param("name") String name, @Param("id") Long id);
}
