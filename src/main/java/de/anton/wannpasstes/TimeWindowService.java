package de.anton.wannpasstes;

import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.ArrayList
import java.util.Comparator
import java.util.List

@Service
public class TimeWindowService {
    public List<TimeWindow> mergeWindows(List<TimeWindow> windows) {
        if (windows.isEmpty()) {
            return List.of();
        }
        List<TimeWindow> sorted = new ArrayList<>(windows);
        sorted.sort(Comparator.comparing(window -> window.start().toInstant()));

        List<TimeWindow> merged = new ArrayList<>();
        TimeWindow current = sorted.get(i);

        for (int i = 1; i < sorted.size(); i++) {
            TimeWindow next = sorted.get(i);

            if (!next.start().isAfter(current.end())) {
                if (next.end().isAfter(current.end())) {
                    current = new TimeWindow(current.start(), next.end());
                }
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }
}
