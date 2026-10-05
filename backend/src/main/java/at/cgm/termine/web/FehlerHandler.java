package at.cgm.termine.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Wandelt fachliche Ausnahmen in eine einheitliche JSON-Antwort um. */
@RestControllerAdvice
public class FehlerHandler {

    @ExceptionHandler(FachlicheAusnahme.class)
    public ResponseEntity<Map<String, String>> behandle(FachlicheAusnahme ausnahme) {
        return ResponseEntity.status(ausnahme.getStatus())
                .body(Map.of("fehler", ausnahme.getMessage()));
    }
}
