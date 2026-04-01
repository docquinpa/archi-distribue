package fr.univrouen.driver.conducteur.api;

import java.time.LocalDate;

public record PermisResponse(
        Integer id,
        String type,
        LocalDate date_valid
) {
}
