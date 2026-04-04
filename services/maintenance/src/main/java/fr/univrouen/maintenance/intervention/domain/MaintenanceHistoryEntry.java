package fr.univrouen.maintenance.intervention.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "maintenance_history")
public class MaintenanceHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "intervention_id", nullable = false)
    private Integer interventionId;

    @Column(name = "vehicle_id", nullable = false)
    private Integer vehicleId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(length = 2000)
    private String details;

    @Column(name = "saga_id", length = 64)
    private String sagaId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @PrePersist
    void onCreate() {
        occurredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Integer getInterventionId() {
        return interventionId;
    }

    public void setInterventionId(Integer interventionId) {
        this.interventionId = interventionId;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
