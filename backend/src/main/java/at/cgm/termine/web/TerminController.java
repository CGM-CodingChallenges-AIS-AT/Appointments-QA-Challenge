package at.cgm.termine.web;

import java.util.List;

import at.cgm.termine.dto.BuchungsAnfrage;
import at.cgm.termine.model.Termin;
import at.cgm.termine.service.TerminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class TerminController {

    private final TerminService terminService;

    public TerminController(TerminService terminService) {
        this.terminService = terminService;
    }

    @GetMapping
    public List<Termin> alle() {
        return terminService.alleTermine();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Termin buchen(@RequestBody BuchungsAnfrage anfrage) {
        return terminService.buchen(anfrage);
    }

    @DeleteMapping("/{id}")
    public Termin stornieren(@PathVariable String id) {
        return terminService.stornieren(id);
    }
}
