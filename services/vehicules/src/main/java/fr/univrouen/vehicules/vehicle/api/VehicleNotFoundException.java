package fr.univrouen.vehicules.vehicle.api;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(Integer id) {
        super("Vehicle not found: " + id);
    }
}
