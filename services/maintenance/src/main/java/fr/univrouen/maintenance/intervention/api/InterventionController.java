package fr.univrouen.maintenance.intervention.api;

import fr.univrouen.maintenance.intervention.service.InterventionService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interventions")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @GetMapping
    public List<InterventionResponse> findAll() {
        return interventionService.findAll();
    }

    @GetMapping("/{id}")
    public InterventionResponse findById(@PathVariable Integer id) {
        return interventionService.findById(id);
    }

    @PostMapping
    public ResponseEntity<InterventionResponse> create(@Valid @RequestBody InterventionInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interventionService.create(input));
    }

    @PutMapping("/{id}")
    public InterventionResponse update(@PathVariable Integer id, @Valid @RequestBody InterventionInput input) {
        return interventionService.update(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        interventionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    public InterventionResponse complete(
            @PathVariable Integer id,
            @RequestBody(required = false) CompleteInterventionRequest request
    ) {
        BigDecimal prix = request == null ? null : request.prix();
        return interventionService.complete(id, prix);
    }

    @PostMapping("/{id}/cancel")
    public InterventionResponse cancel(
            @PathVariable Integer id,
            @RequestBody(required = false) CancelInterventionRequest request
    ) {
        String reason = request == null ? null : request.reason();
        return interventionService.cancel(id, reason);
    }

    @GetMapping("/alerts/preventive")
    public List<InterventionResponse> preventiveAlerts(@RequestParam(defaultValue = "7") int withinDays) {
        return interventionService.findPreventiveAlerts(withinDays);
    }

    @GetMapping("/history")
    public List<MaintenanceHistoryResponse> history() {
        return interventionService.findHistory();
    }

    @GetMapping("/{id}/history")
    public List<MaintenanceHistoryResponse> historyByIntervention(@PathVariable Integer id) {
        return interventionService.findHistoryByIntervention(id);
    }

    public record CompleteInterventionRequest(BigDecimal prix) {
    }

    public record CancelInterventionRequest(String reason) {
    }
}
