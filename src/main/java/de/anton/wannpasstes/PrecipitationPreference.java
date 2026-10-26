package de.anton.wannpasstes;

public enum PrecipitationPreference {

    DRY("Trocken"),
    LIGHT_DRIZZLE("Höchstens leichter Nieselregen"),
    LIGHT_RAIN("Höchstens leichter Regen"),
    ANY("Egal");

    private final String label;

    PrecipitationPreference(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
