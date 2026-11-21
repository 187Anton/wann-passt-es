package de.anton.wannpasstes;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Date parameters of all controllers follow the same rules: an empty value
 * means "not given", a value that is not an ISO date is a client error.
 */
@ControllerAdvice
public class DateBindingAdvice {

    @InitBinder
    public void registerDateEditor(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDate.class, new IsoDateEditor());
    }

    static class IsoDateEditor extends PropertyEditorSupport {

        @Override
        public void setAsText(String text) {
            if (text == null || text.isBlank()) {
                setValue(null);
                return;
            }
            try {
                setValue(LocalDate.parse(text.strip()));
            } catch (DateTimeParseException exception) {
                throw new IllegalArgumentException(
                        "Bitte gib ein Datum im Format JJJJ-MM-TT an.", exception);
            }
        }
    }
}
