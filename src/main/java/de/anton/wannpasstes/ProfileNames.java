package de.anton.wannpasstes;

import java.util.Optional;

/**
 * Rules for profile names shared by creating and editing profiles:
 * 1 to 100 characters after trimming, unique ignoring case and surrounding
 * whitespace.
 */
final class ProfileNames {

    static final String LENGTH_MESSAGE = "Bitte gib einen Profilnamen mit 1 bis "
            + SearchProfile.NAME_MAX_LENGTH + " Zeichen ein.";

    private ProfileNames() {
    }

    static String normalize(String rawName) {
        return rawName == null ? "" : rawName.strip();
    }

    /**
     * Returns a German error message if the name is invalid.
     *
     * @param excludedProfileId ID of the profile being edited (may keep its own name), or {@code null}
     */
    static Optional<String> validate(String rawName, SearchProfileRepository repository, Long excludedProfileId) {
        String name = normalize(rawName);

        if (name.isEmpty() || name.length() > SearchProfile.NAME_MAX_LENGTH) {
            return Optional.of(LENGTH_MESSAGE);
        }

        boolean taken = excludedProfileId == null
                ? repository.existsByNormalizedName(name)
                : repository.existsByNormalizedNameExcludingId(name, excludedProfileId);

        if (taken) {
            return Optional.of("Es gibt bereits ein Suchprofil mit dem Namen „" + name
                    + "“. Bitte wähle einen anderen Namen.");
        }
        return Optional.empty();
    }
}
