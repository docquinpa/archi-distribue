package fr.univrouen.driver.conducteur.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConducteurInput(
        @NotBlank String prenom,
        @NotBlank String nom,
        @NotNull Integer permisId
) {
}
