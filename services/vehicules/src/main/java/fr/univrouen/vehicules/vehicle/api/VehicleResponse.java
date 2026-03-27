package fr.univrouen.vehicules.vehicle.api;

public record VehicleResponse(
        Integer id,
        String vin,
        Boolean dispo
) {
}
