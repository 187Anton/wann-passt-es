package de.anton.wannpasstes;

public enum Activity {

    WALKING("Spazieren gehen"),
    RUNNING("Laufen"),
    CYCLING("Radfahren"),
    HIKING("Wandern");

    private final String label;

    Activity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
