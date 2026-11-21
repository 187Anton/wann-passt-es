package de.anton.wannpasstes;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@ControllerAdvice
class WebErrorHandler {

    @ExceptionHandler({BindException.class, ServletRequestBindingException.class,
            HandlerMethodValidationException.class, TypeMismatchException.class})
    ModelAndView invalidInput(Exception exception) {
        if (exception instanceof HandlerMethodValidationException validation && validation.isForReturnValue()) {
            return new ModelAndView("error", Map.of("status", 500), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ModelAndView("error", Map.of("status", 400), HttpStatus.BAD_REQUEST);
    }
}
