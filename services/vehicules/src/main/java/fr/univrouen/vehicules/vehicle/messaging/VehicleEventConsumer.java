package fr.univrouen.vehicules.vehicle.messaging;

import fr.univrouen.vehicules.vehicle.domain.Vehicle;
import fr.univrouen.vehicules.vehicle.repository.VehicleRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VehicleEventConsumer {

    private final VehicleRepository vehicleRepository;

    public VehicleEventConsumer(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.vehicle:vehicule_topic}",
            groupId = "${app.kafka.group-id:vehicules-service}",
            autoStartup = "${app.kafka.enabled:true}"
    )
    public void onVehicleEvent(VehicleEvent event) {
        if (event == null || event.event() == null || event.vehicle_id() == null) {
            return;
        }

        switch (event.event()) {
            case "VEHICLE_CREATED", "VEHICLE_UPDATED" -> upsert(event);
            case "VEHICLE_DELETED" -> vehicleRepository.deleteById(event.vehicle_id());
            default -> {
                // Unknown event type: ignore for forward compatibility.
            }
        }
    }

    private void upsert(VehicleEvent event) {
        Vehicle vehicle = vehicleRepository.findById(event.vehicle_id()).orElseGet(Vehicle::new);
        vehicle.setId(event.vehicle_id());
        if (event.vin() != null) {
            vehicle.setVin(event.vin());
        }
        if (event.dispo() != null) {
            vehicle.setDispo(event.dispo());
        }
        vehicleRepository.save(vehicle);
    }
}
