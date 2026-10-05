package at.cgm.termine.model;

import java.time.LocalDateTime;

/** Ein gebuchter Termin eines Patienten auf einem Slot. */
public class Termin {

    private final String id;
    private final String slotId;
    private final String patientenname;
    private final String grund;
    private final LocalDateTime erstelltAm;
    private TerminStatus status;

    public Termin(String id, String slotId, String patientenname, String grund) {
        this.id = id;
        this.slotId = slotId;
        this.patientenname = patientenname;
        this.grund = grund;
        this.erstelltAm = LocalDateTime.now();
        this.status = TerminStatus.GEBUCHT;
    }

    public String getId() {
        return id;
    }

    public String getSlotId() {
        return slotId;
    }

    public String getPatientenname() {
        return patientenname;
    }

    public String getGrund() {
        return grund;
    }

    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }

    public TerminStatus getStatus() {
        return status;
    }

    public void setStatus(TerminStatus status) {
        this.status = status;
    }
}
