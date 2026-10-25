package de.anton.wannpasstes;

import java.time.Duration;
import java.time.OffsetDateTime;


public record TimeWindow(OffsetDateTime start, OffsetDateTime end) {

    public TimeWindow {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end must not be null");

        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End must be after start");

        }
    }

     public long durationMinutes() {
        return Duration.between(start, end).toMinutes();
     }
}
