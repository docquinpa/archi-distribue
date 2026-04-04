package fr.univrouen.maintenance.intervention.messaging;

import java.time.Instant;

public record VehicleEvent(
        Integer vehicle_id,
        String vin,
        Boolean dispo,
        String event,
        Instant timestamp,
        String version,
        String event_id,
        String saga_id,
        String correlation_id
) {
}
