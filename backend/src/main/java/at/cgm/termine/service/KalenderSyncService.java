package at.cgm.termine.service;

import at.cgm.termine.model.Slot;
import org.springframework.stereotype.Service;

/**
 * Stub fuer die Anbindung an den Kalender des Primaersystems.
 * Der Aufruf geht im Echtbetrieb ueber das Netz und braucht daher etwas Zeit.
 */
@Service
public class KalenderSyncService {

    private static final long ANTWORTZEIT_MS = 40;

    public void reserviereImKalender(Slot slot) {
        try {
            Thread.sleep(ANTWORTZEIT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
