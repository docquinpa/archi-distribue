package fr.univrouen.maintenance.intervention.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InterventionInput(
        @NotNull Integer vehicle_id,
        @NotBlank String state,
        @NotNull LocalDate date,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal prix,
        Integer mileage_threshold,
        Integer current_mileage,
        String notes
) {
}
