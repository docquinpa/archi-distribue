package fr.univrouen.maintenance.intervention.graphql;

import fr.univrouen.maintenance.intervention.api.InterventionInput;
import fr.univrouen.maintenance.intervention.api.InterventionResponse;
import fr.univrouen.maintenance.intervention.api.MaintenanceHistoryResponse;
import fr.univrouen.maintenance.intervention.service.InterventionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class MaintenanceGraphqlResolver {

    private final InterventionService interventionService;

    public MaintenanceGraphqlResolver(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @QueryMapping
    public List<InterventionResponse> interventions() {
        return interventionService.findAll();
    }

    @QueryMapping
    public InterventionResponse intervention(@Argument Integer id) {
        return interventionService.findById(id);
    }

    @QueryMapping
    public List<InterventionResponse> preventiveAlerts(@Argument Integer withinDays) {
        return interventionService.findPreventiveAlerts(withinDays == null ? 7 : withinDays);
    }

    @QueryMapping
    public List<MaintenanceHistoryResponse> maintenanceHistory() {
        return interventionService.findHistory();
    }

    @QueryMapping
    public List<MaintenanceHistoryResponse> interventionHistory(@Argument Integer intervention_id) {
        return interventionService.findHistoryByIntervention(intervention_id);
    }

    @MutationMapping
    public InterventionResponse createIntervention(
            @Argument Integer vehicle_id,
            @Argument String state,
            @Argument LocalDate date,
            @Argument BigDecimal prix,
            @Argument Integer mileage_threshold,
            @Argument Integer current_mileage,
            @Argument String notes
    ) {
        InterventionInput input = new InterventionInput(
                vehicle_id,
                state,
                date,
                prix,
                mileage_threshold,
                current_mileage,
                notes
        );
        return interventionService.create(input);
    }

    @MutationMapping
    public InterventionResponse updateIntervention(
            @Argument Integer id,
            @Argument String state,
            @Argument LocalDate date,
            @Argument BigDecimal prix,
            @Argument Integer mileage_threshold,
            @Argument Integer current_mileage,
            @Argument String notes
    ) {
        return interventionService.patch(id, state, date, prix, mileage_threshold, current_mileage, notes);
    }

    @MutationMapping
    public InterventionResponse completeIntervention(@Argument Integer id, @Argument BigDecimal prix) {
        return interventionService.complete(id, prix);
    }

    @MutationMapping
    public InterventionResponse cancelIntervention(@Argument Integer id, @Argument String reason) {
        return interventionService.cancel(id, reason);
    }

    @MutationMapping
    public Boolean deleteIntervention(@Argument Integer id) {
        interventionService.delete(id);
        return true;
    }
}
