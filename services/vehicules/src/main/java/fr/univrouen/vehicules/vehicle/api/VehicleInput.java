package fr.univrouen.vehicules.vehicle.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleInput(
        @NotBlank String vin,
        @NotNull Boolean dispo
) {
}
