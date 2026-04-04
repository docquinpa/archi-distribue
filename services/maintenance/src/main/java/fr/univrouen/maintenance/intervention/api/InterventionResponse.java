package fr.univrouen.maintenance.intervention.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InterventionResponse(
        Integer id,
        Integer vehicle_id,
        String state,
        LocalDate date,
        LocalDate completed_date,
        BigDecimal prix,
        Integer mileage_threshold,
        Integer current_mileage,
        String notes,
        boolean preventive_alert
) {
}
