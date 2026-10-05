package at.cgm.termine.dto;

import java.time.LocalDateTime;

/**
 * Anfrage zum Anlegen eines zusaetzlichen Slots.
 */
public record SlotAnfrage(String arzt, LocalDateTime beginn) {
}
