package fr.univrouen.vehicules.vehicle.messaging;

import java.time.Instant;

public record VehicleEvent(
        Integer vehicle_id,
        String vin,
        Boolean dispo,
        String event,
        Instant timestamp,
        String version
) {
}
