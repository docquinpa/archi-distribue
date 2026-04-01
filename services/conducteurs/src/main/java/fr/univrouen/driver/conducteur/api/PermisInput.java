package fr.univrouen.driver.conducteur.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PermisInput(
        @NotBlank String type,
        @NotNull LocalDate date_valid
) {
}
