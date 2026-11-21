package de.anton.wannpasstes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small helper for tests: finds the checked radio buttons in rendered HTML. */
final class RadioMarkup {

    private static final Pattern RADIO = Pattern.compile("<input\\b[^>]*type=\"radio\"[^>]*>");
    private static final Pattern NAME = Pattern.compile("\\bname=\"([^\"]*)\"");
    private static final Pattern VALUE = Pattern.compile("\\bvalue=\"([^\"]*)\"");

    private RadioMarkup() {
    }

    /** Returns all values of the radio group {@code name}, in document order. */
    static List<String> values(String html, String name) {
        return collect(html, name, false);
    }

    /** Returns the values of the checked radio buttons of the group {@code name}. */
    static List<String> checked(String html, String name) {
        return collect(html, name, true);
    }

    private static List<String> collect(String html, String name, boolean onlyChecked) {
        List<String> result = new ArrayList<>();
        Matcher radio = RADIO.matcher(html);
        while (radio.find()) {
            String tag = radio.group();
            Matcher tagName = NAME.matcher(tag);
            Matcher tagValue = VALUE.matcher(tag);
            if (tagName.find() && tagName.group(1).equals(name) && tagValue.find()
                    && (!onlyChecked || tag.contains("checked"))) {
                result.add(tagValue.group(1));
            }
        }
        return result;
    }
}
