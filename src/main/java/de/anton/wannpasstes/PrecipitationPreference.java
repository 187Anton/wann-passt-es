package de.anton.wannpasstes;

public enum PrecipitationPreference {

    DRY("Trocken", 0.0),
    LIGHT_DRIZZLE("Höchstens leichter Nieselregen", 0.2),
    LIGHT_RAIN("Höchstens leichter Regen", 1.0),
    ANY("Egal", Double.POSITIVE_INFINITY);

    private final String label;
    private final double maxPrecipitationMm;

    PrecipitationPreference(String label, double maxPrecipitationMm) {
        this.label = label;
        this.maxPrecipitationMm = maxPrecipitationMm;
    }

    public String getLabel() {
        return label;
    }

    public double getMaxPrecipitationMm() {
        return maxPrecipitationMm;
    }
}
