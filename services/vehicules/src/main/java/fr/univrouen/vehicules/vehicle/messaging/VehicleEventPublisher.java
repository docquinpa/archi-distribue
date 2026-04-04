package fr.univrouen.vehicules.vehicle.messaging;

import fr.univrouen.vehicules.vehicle.api.VehicleResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class VehicleEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String vehicleTopic;
    private final boolean kafkaEnabled;

    @Autowired
    public VehicleEventPublisher(
            ObjectProvider<KafkaTemplate<Object, Object>> kafkaTemplateProvider,
            @Value("${app.kafka.topic.vehicle:vehicule_topic}") String vehicleTopic,
            @Value("${app.kafka.enabled:true}") boolean kafkaEnabled
    ) {
        this(kafkaTemplateProvider.getIfAvailable(), vehicleTopic, kafkaEnabled);
    }

    VehicleEventPublisher(KafkaTemplate<Object, Object> kafkaTemplate, String vehicleTopic, boolean kafkaEnabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.vehicleTopic = vehicleTopic;
        this.kafkaEnabled = kafkaEnabled;
    }

    public void publishCreated(VehicleResponse vehicle) {
        publish(vehicle, "VEHICLE_CREATED");
    }

    public void publishUpdated(VehicleResponse vehicle) {
        publish(vehicle, "VEHICLE_UPDATED");
    }

    public void publishDeleted(Integer vehicleId) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            return;
        }

        String sagaId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();

        VehicleEvent event = new VehicleEvent(
                vehicleId,
                null,
                null,
                "VEHICLE_DELETED",
                Instant.now(),
                "1.0",
                eventId,
                sagaId,
                sagaId
        );

        kafkaTemplate.send(vehicleTopic, String.valueOf(vehicleId), event);
    }

    private void publish(VehicleResponse vehicle, String eventType) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            return;
        }

        String eventId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();

        VehicleEvent event = new VehicleEvent(
                vehicle.id(),
                vehicle.vin(),
                vehicle.dispo(),
                eventType,
                Instant.now(),
                "1.0",
                eventId,
                null,
                correlationId
        );

        kafkaTemplate.send(vehicleTopic, String.valueOf(vehicle.id()), event);
    }
}
