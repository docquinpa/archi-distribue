package fr.univrouen.gateway.graphql.model;

public record LicenseView(
        Integer id,
        String type,
        String date_valid
) {
}
