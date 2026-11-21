package de.anton.wannpasstes;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SearchProfileRepository extends JpaRepository<SearchProfile, Long> {

    Optional<SearchProfile> findByNameKey(String nameKey);

    boolean existsByNameKeyIsNull();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from SearchProfile profile where profile.id = :id")
    Optional<SearchProfile> findForUpdate(@Param("id") long id);
}
