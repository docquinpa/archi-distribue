package fr.univrouen.maintenance.intervention.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record MaintenanceEvent(
        Integer maintenance_id,
        Integer vehicle_id,
        String event,
        String state,
        LocalDate scheduled_date,
        LocalDate completed_date,
        BigDecimal prix,
        String saga_id,
        Instant timestamp,
        String version,
        String event_id,
        String correlation_id
) {
}
