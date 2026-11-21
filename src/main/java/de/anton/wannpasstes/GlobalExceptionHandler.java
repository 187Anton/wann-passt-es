package de.anton.wannpasstes;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Turns invalid request parameters and missing profiles into friendly German
 * error pages with a suitable 4xx status. Unknown URLs and unexpected server
 * errors are rendered by Spring Boot's error handling with the same
 * {@code error} template (without technical details).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    static final String VIEW = "error";

    private static final int MAX_SHOWN_VALUE_LENGTH = 40;

    private static final Map<String, String> PARAMETER_LABELS = Map.ofEntries(
            Map.entry("locationId", "Ort"),
            Map.entry("activity", "Aktivität"),
            Map.entry("minDurationMinutes", "Mindestdauer"),
            Map.entry("temperaturePreference", "Temperatur"),
            Map.entry("windPreference", "Wind"),
            Map.entry("precipitationPreference", "Niederschlag"),
            Map.entry("searchDate", "Datum"),
            Map.entry("startDate", "Startdatum"),
            Map.entry("endDate", "Enddatum"),
            Map.entry("id", "Suchprofil"),
            Map.entry("profileId", "Suchprofil"));

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String label = labelOf(exception.getName());
        String value = exception.getValue() == null ? "" : shorten(exception.getValue().toString());
        String message = value.isEmpty()
                ? "Die Angabe für „" + label + "“ ist ungültig."
                : "Der Wert „" + value + "“ ist für „" + label + "“ nicht gültig.";
        return badRequest(message + hintFor(exception.getName()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ModelAndView handleMissingParameter(MissingServletRequestParameterException exception) {
        return badRequest("Es fehlt die Angabe „" + labelOf(exception.getParameterName()) + "“.");
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ModelAndView handleValidation(HandlerMethodValidationException exception) {
        String message = exception.getParameterValidationResults().stream()
                .findFirst()
                .map(result -> {
                    String name = parameterName(result.getMethodParameter());
                    return "Die Angabe für „" + labelOf(name) + "“ ist ungültig." + hintFor(name);
                })
                .orElse("Einige Angaben sind ungültig.");
        return badRequest(message);
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ModelAndView handleProfileNotFound() {
        ModelAndView view = page(HttpStatus.NOT_FOUND,
                "Suchprofil nicht gefunden",
                "Dieses Suchprofil gibt es nicht (mehr). Vielleicht wurde es bereits gelöscht.");
        view.addObject("backHref", "/profiles");
        view.addObject("backLabel", "Zu den Suchprofilen");
        return view;
    }

    private static ModelAndView badRequest(String message) {
        return page(HttpStatus.BAD_REQUEST, "Ungültige Eingabe", message);
    }

    private static ModelAndView page(HttpStatus status, String title, String message) {
        ModelAndView view = new ModelAndView(VIEW, status);
        view.addObject("status", status.value());
        view.addObject("errorTitle", title);
        view.addObject("errorMessage", message);
        return view;
    }

    private static String parameterName(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null && !requestParam.name().isEmpty()) {
            return requestParam.name();
        }
        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null && !pathVariable.name().isEmpty()) {
            return pathVariable.name();
        }
        return parameter.getParameterName();
    }

    private static String labelOf(String parameterName) {
        return PARAMETER_LABELS.getOrDefault(parameterName, "Eingabe");
    }

    private static String hintFor(String parameterName) {
        if (parameterName == null) {
            return "";
        }
        return switch (parameterName) {
            case "minDurationMinutes" -> " Bitte gib eine ganze Zahl von mindestens 1 Minute an.";
            case "searchDate", "startDate", "endDate" -> " Bitte gib ein gültiges Datum an.";
            case "locationId" -> " Bitte wähle den Ort über die Ortssuche aus.";
            default -> " Bitte wähle einen der angebotenen Werte.";
        };
    }

    private static String shorten(String value) {
        String stripped = value.strip();
        return stripped.length() > MAX_SHOWN_VALUE_LENGTH
                ? stripped.substring(0, MAX_SHOWN_VALUE_LENGTH) + "…"
                : stripped;
    }
}
