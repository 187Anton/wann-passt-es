package de.anton.wannpasstes;

/**
 * Thrown when a search profile with the requested ID does not exist.
 */
public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(long id) {
        super("Search profile " + id + " not found");
    }
}
