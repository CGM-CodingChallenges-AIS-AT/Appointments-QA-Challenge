package at.cgm.termine.web;

import java.util.List;

import at.cgm.termine.dto.SlotAnfrage;
import at.cgm.termine.model.Slot;
import at.cgm.termine.service.TerminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    private final TerminService terminService;

    public SlotController(TerminService terminService) {
        this.terminService = terminService;
    }

    @GetMapping
    public List<Slot> alle() {
        return terminService.alleSlots();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Slot anlegen(@RequestBody SlotAnfrage anfrage) {
        return terminService.legeSlotAn(anfrage);
    }
}
