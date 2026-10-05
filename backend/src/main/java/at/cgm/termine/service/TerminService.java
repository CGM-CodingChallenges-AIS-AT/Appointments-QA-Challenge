package at.cgm.termine.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import at.cgm.termine.dto.BuchungsAnfrage;
import at.cgm.termine.dto.SlotAnfrage;
import at.cgm.termine.model.Slot;
import at.cgm.termine.model.Termin;
import at.cgm.termine.model.TerminStatus;
import at.cgm.termine.web.FachlicheAusnahme;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Fachlogik der Terminverwaltung. Die Daten liegen ausschliesslich im
 * Arbeitsspeicher und werden beim Start der Anwendung neu aufgebaut.
 */
@Service
public class TerminService {

    private static final int MAX_LAENGE_PATIENTENNAME = 100;
    private static final int MAX_LAENGE_GRUND = 255;
    private static final int STORNOFRIST_STUNDEN = 24;

    private final Map<String, Slot> slots = new ConcurrentHashMap<>();
    private final Map<String, Termin> termine = new ConcurrentHashMap<>();
    private final KalenderSyncService kalenderSync;

    public TerminService(KalenderSyncService kalenderSync) {
        this.kalenderSync = kalenderSync;
    }

    @PostConstruct
    void befuelleStammdaten() {
        LocalDateTime jetzt = LocalDateTime.now();
        lege("slot-1", "Dr. Bauer", jetzt.minusHours(3));
        lege("slot-2", "Dr. Bauer", jetzt.plusHours(2));
        lege("slot-3", "Dr. Bauer", jetzt.plusHours(23).plusMinutes(50));
        lege("slot-4", "Dr. Fischer", jetzt.plusHours(26));
        lege("slot-5", "Dr. Fischer", jetzt.plusDays(2));
        lege("slot-6", "Dr. Gruber", jetzt.plusDays(3));
        lege("slot-7", "Dr. Gruber", jetzt.plusDays(4));
        lege("slot-8", "Dr. Gruber", jetzt.plusDays(5));
    }

    private void lege(String id, String arzt, LocalDateTime beginn) {
        slots.put(id, new Slot(id, arzt, beginn));
    }

    /**
     * Legt einen zusaetzlichen Slot an. Dieser Einstiegspunkt existiert, damit
     * fuer Tests gezielt Termine mit frei waehlbarem Beginn erzeugt werden
     * koennen.
     */
    public Slot legeSlotAn(SlotAnfrage anfrage) {
        if (anfrage == null || anfrage.arzt() == null || anfrage.arzt().trim().isEmpty()) {
            throw new FachlicheAusnahme(HttpStatus.BAD_REQUEST,
                    "Der Arzt ist ein Pflichtfeld.");
        }
        if (anfrage.beginn() == null) {
            throw new FachlicheAusnahme(HttpStatus.BAD_REQUEST,
                    "Der Beginn ist ein Pflichtfeld.");
        }

        Slot slot = new Slot(UUID.randomUUID().toString(), anfrage.arzt().trim(), anfrage.beginn());
        slots.put(slot.getId(), slot);
        return slot;
    }

    public List<Slot> alleSlots() {
        List<Slot> ergebnis = new ArrayList<>(slots.values());
        ergebnis.sort(Comparator.comparing(Slot::getBeginn));
        return ergebnis;
    }

    public List<Termin> alleTermine() {
        List<Termin> ergebnis = new ArrayList<>(termine.values());
        ergebnis.sort(Comparator.comparing(Termin::getErstelltAm));
        return ergebnis;
    }

    public Termin buchen(BuchungsAnfrage anfrage) {
        pruefeAnfrage(anfrage);

        Slot slot = slots.get(anfrage.slotId());

        if (slot.getBeginn().isBefore(LocalDateTime.now())) {
            throw new FachlicheAusnahme(HttpStatus.BAD_REQUEST,
                    "Der Slot liegt in der Vergangenheit und kann nicht gebucht werden.");
        }

        if (slot.isGebucht()) {
            throw new FachlicheAusnahme(HttpStatus.CONFLICT,
                    "Der Slot ist bereits gebucht.");
        }

        kalenderSync.reserviereImKalender(slot);
        slot.setGebucht(true);

        Termin termin = new Termin(UUID.randomUUID().toString(), slot.getId(),
                anfrage.patientenname().trim(), anfrage.grund().trim());
        termine.put(termin.getId(), termin);
        return termin;
    }

    public Termin stornieren(String terminId) {
        Termin termin = termine.get(terminId);
        if (termin == null) {
            throw new FachlicheAusnahme(HttpStatus.NOT_FOUND,
                    "Der Termin wurde nicht gefunden.");
        }

        Slot slot = slots.get(termin.getSlotId());
        long minutenBisBeginn = ChronoUnit.MINUTES.between(LocalDateTime.now(), slot.getBeginn());
        long stundenBisBeginn = Math.round(minutenBisBeginn / 60.0);
        if (stundenBisBeginn < STORNOFRIST_STUNDEN) {
            throw new FachlicheAusnahme(HttpStatus.CONFLICT,
                    "Eine Stornierung ist nur bis 24 Stunden vor Terminbeginn moeglich.");
        }

        termin.setStatus(TerminStatus.STORNIERT);
        slot.setGebucht(false);
        return termin;
    }

    private void pruefeAnfrage(BuchungsAnfrage anfrage) {
        if (anfrage == null || anfrage.slotId() == null || anfrage.slotId().isBlank()) {
            throw new FachlicheAusnahme(HttpStatus.BAD_REQUEST,
                    "Die Slot-ID ist ein Pflichtfeld.");
        }
        if (anfrage.patientenname() == null || anfrage.patientenname().trim().isEmpty()) {
            throw new FachlicheAusnahme(HttpStatus.BAD_REQUEST,
                    "Der Patientenname ist ein Pflichtfeld.");
        }
        if (anfrage.patientenname().trim().length() > MAX_LAENGE_PATIENTENNAME) {
            throw new FachlicheAusnahme(HttpStatus.BAD_REQUEST,
                    "Der Patientenname darf hoechstens " + MAX_LAENGE_PATIENTENNAME + " Zeichen lang sein.");
        }
        if (anfrage.grund() == null || anfrage.grund().trim().isEmpty()) {
            throw new FachlicheAusnahme(HttpStatus.BAD_REQUEST,
                    "Der Grund ist ein Pflichtfeld.");
        }
    }
}
