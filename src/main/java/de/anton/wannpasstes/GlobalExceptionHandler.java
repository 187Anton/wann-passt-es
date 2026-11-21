package de.anton.wannpasstes;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Turns request errors into a styled, German page instead of a whitelabel error
 * page or a stack trace.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST, "Ungültige Eingabe",
                "Eine Eingabe konnte nicht verarbeitet werden. "
                        + "Bitte prüfe deine Angaben und versuche es erneut.");
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            BindException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class})
    public ModelAndView handleInvalidRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "Ungültige Eingabe",
                "Bitte prüfe deine Angaben. Mindestens eine Eingabe ist ungültig "
                        + "oder fehlt noch.");
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ModelAndView handleNotFound(Exception exception) {
        return error(HttpStatus.NOT_FOUND, "Seite nicht gefunden",
                "Diese Seite gibt es nicht (mehr). Über die Navigation kommst du "
                        + "zurück zur Planung.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Aktion nicht möglich",
                "Diese Aktion kann auf dieser Seite nicht ausgeführt werden. "
                        + "Bitte gehe zurück zur Planung.");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception exception) {
        LOG.error("Unexpected error while handling a request", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Etwas ist schiefgelaufen",
                "Bei der Verarbeitung ist ein unerwarteter Fehler aufgetreten. "
                        + "Bitte versuche es später erneut.");
    }

    private ModelAndView error(HttpStatus status, String title, String message) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.setStatus(status);
        modelAndView.addObject("status", status.value());
        modelAndView.addObject("title", title);
        modelAndView.addObject("message", message);
        return modelAndView;
    }
}
