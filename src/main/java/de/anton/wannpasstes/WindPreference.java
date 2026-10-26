package de.anton.wannpasstes;

public enum WindPreference {

    WEAK("Höchstens schwacher Wind", 15.0),
    MODERATE("Höchstens mäßiger Wind", 30.0),
    ANY("Egal", Double.POSITIVE_INFINITY);

    private final String label;
    private final double maxWindKmh;

    WindPreference(String label, double maxWindKmh) {
        this.label = label;
        this.maxWindKmh = maxWindKmh;
    }

    public String getLabel() {
        return label;
    }

    public double getMaxWindKmh() {
        return maxWindKmh;
    }
}