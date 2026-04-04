package fr.univrouen.gateway.graphql.model;

public record InterventionView(
        Integer id,
        Integer vehicle_id,
        String state,
        String date,
        String completed_date,
        Double prix,
        Integer mileage_threshold,
        Integer current_mileage,
        String notes,
        Boolean preventive_alert
) {
}
