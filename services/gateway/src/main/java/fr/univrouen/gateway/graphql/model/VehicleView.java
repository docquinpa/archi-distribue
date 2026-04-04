package fr.univrouen.gateway.graphql.model;

public record VehicleView(
        Integer id,
        String vin,
        Boolean dispo
) {
}
