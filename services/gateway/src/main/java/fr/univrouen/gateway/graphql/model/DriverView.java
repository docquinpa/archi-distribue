package fr.univrouen.gateway.graphql.model;

public record DriverView(
        Integer id,
        String prenom,
        String nom,
        LicenseView permis
) {
}
