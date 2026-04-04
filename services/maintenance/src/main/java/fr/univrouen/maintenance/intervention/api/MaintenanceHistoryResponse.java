package fr.univrouen.maintenance.intervention.api;

import java.time.Instant;

public record MaintenanceHistoryResponse(
        Long id,
        Integer intervention_id,
        Integer vehicle_id,
        String event,
        String details,
        String saga_id,
        Instant timestamp
) {
}
