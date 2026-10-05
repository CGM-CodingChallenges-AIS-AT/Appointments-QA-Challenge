package at.cgm.termine.model;

import java.time.LocalDateTime;

/** Ein buchbarer Termin-Slot einer Ordination. */
public class Slot {

    private final String id;
    private final String arzt;
    private final LocalDateTime beginn;
    private boolean gebucht;

    public Slot(String id, String arzt, LocalDateTime beginn) {
        this.id = id;
        this.arzt = arzt;
        this.beginn = beginn;
        this.gebucht = false;
    }

    public String getId() {
        return id;
    }

    public String getArzt() {
        return arzt;
    }

    public LocalDateTime getBeginn() {
        return beginn;
    }

    public boolean isGebucht() {
        return gebucht;
    }

    public void setGebucht(boolean gebucht) {
        this.gebucht = gebucht;
    }
}
