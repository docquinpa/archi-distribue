package fr.univrouen.maintenance.intervention.api;

public class InterventionNotFoundException extends RuntimeException {

    public InterventionNotFoundException(Integer interventionId) {
        super("Intervention with id " + interventionId + " not found");
    }
}
