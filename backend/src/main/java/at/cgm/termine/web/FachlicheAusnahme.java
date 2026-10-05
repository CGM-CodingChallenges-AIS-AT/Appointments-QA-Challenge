package at.cgm.termine.web;

import org.springframework.http.HttpStatus;

/** Fachlicher Fehler mit zugehoerigem HTTP-Status. */
public class FachlicheAusnahme extends RuntimeException {

    private final HttpStatus status;

    public FachlicheAusnahme(HttpStatus status, String meldung) {
        super(meldung);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
