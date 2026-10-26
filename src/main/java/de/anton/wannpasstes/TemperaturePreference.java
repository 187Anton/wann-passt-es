package de.anton.wannpasstes;

public enum TemperaturePreference {

    COOL("Kühl", 5.0, 15.0),
    MILD("Mild", 15.0, 25.0),
    WARM("Warm", 20.0, 30.0),
    ANY("Egal", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    private final String label;
    private final double minTemperatureCelsius;
    private final double maxTemperatureCelsius;

    TemperaturePreference(
            String label,
            double minTemperatureCelsius,
            double maxTemperatureCelsius) {
        this.label = label;
        this.minTemperatureCelsius = minTemperatureCelsius;
        this.maxTemperatureCelsius = maxTemperatureCelsius;
    }

    public String getLabel() {
        return label;
    }

    public double getMinTemperatureCelsius() {
        return minTemperatureCelsius;
    }

    public double getMaxTemperatureCelsius() {
        return maxTemperatureCelsius;
    }
}