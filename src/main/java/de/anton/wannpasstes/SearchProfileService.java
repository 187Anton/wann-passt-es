package de.anton.wannpasstes;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.Objects;
import java.util.Optional;

@Service
class SearchProfileService {

    private final SearchProfileRepository repository;
    private final GeocodingClient geocodingClient;

    SearchProfileService(SearchProfileRepository repository, GeocodingClient geocodingClient) {
        this.repository = repository;
        this.geocodingClient = geocodingClient;
    }

    @Transactional
    public void save(Long id, ProfileForm form) {
        SearchProfile existing = id == null ? null : repository.findForUpdate(id).orElseThrow(MissingProfile::new);
        String name = form.trimmedName();
        if (name.isEmpty() || name.length() > 100) {
            throw new InvalidProfile("Bitte gib einen Profilnamen mit 1 bis 100 Zeichen ein.");
        }

        String nameKey = nameKey(name);
        if (repository.findByNameKey(nameKey).filter(match -> !Objects.equals(id, match.getId())).isPresent()) {
            throw new InvalidProfile("Diesen Profilnamen gibt es schon. Bitte wähle einen anderen.");
        }

        String locationName = locationName(existing, form.locationId());
        SearchProfile profile = existing == null ? new SearchProfile() : existing;
        profile.update(name, nameKey, locationName, form);
        repository.saveAndFlush(profile);
    }

    @Transactional
    public boolean delete(long id) {
        Optional<SearchProfile> profile = repository.findForUpdate(id);
        profile.ifPresent(repository::delete);
        return profile.isPresent();
    }

    private String locationName(SearchProfile existing, int locationId) {
        if (existing != null && existing.getLocationId() == locationId && existing.getLocationName() != null) {
            return existing.getLocationName();
        }
        try {
            return geocodingClient.getLocation(locationId).name();
        } catch (RestClientException exception) {
            if (existing != null && existing.getLocationId() == locationId) {
                return null;
            }
            throw new InvalidProfile("Der Ortsname konnte nicht geladen werden. "
                    + "Bitte prüfe den Ort oder versuche es später erneut.");
        }
    }

    static String nameKey(String name) {
        StringBuilder key = new StringBuilder();
        name.strip().codePoints()
                .map(Character::toUpperCase)
                .map(Character::toLowerCase)
                .forEach(key::appendCodePoint);
        return key.toString();
    }

    static class InvalidProfile extends RuntimeException {
        InvalidProfile(String message) {
            super(message);
        }
    }

    static class MissingProfile extends RuntimeException {
    }
}
