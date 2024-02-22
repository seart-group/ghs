package ch.usi.si.seart.controller;

import ch.usi.si.seart.exception.IllegalBoundaryException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;

@ControllerAdvice
public class ExceptionController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException ignored) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalBoundaryException.class)
    public ResponseEntity<?> handleIllegalBoundaryException(IllegalBoundaryException ibe) {
        String message = ibe.getMessage();
        Map<String, String> payload = Map.of("message", message);
        return ResponseEntity.badRequest().body(payload);
    }
}
