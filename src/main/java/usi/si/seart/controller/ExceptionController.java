package usi.si.seart.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import usi.si.seart.exception.IllegalBoundaryException;

import javax.persistence.EntityNotFoundException;
import java.util.Map;

@Slf4j
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
