package at.cgm.termine.dto;

/** Anfrage zum Buchen eines Slots. */
public record BuchungsAnfrage(String slotId, String patientenname, String grund) {
}
