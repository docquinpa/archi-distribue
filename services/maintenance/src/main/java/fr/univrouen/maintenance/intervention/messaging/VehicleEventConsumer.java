package fr.univrouen.maintenance.intervention.messaging;

import fr.univrouen.maintenance.intervention.service.InterventionService;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VehicleEventConsumer {

    private final InterventionService interventionService;
    private final KafkaEventDeduplicationService deduplicationService;

    public VehicleEventConsumer(InterventionService interventionService, KafkaEventDeduplicationService deduplicationService) {
        this.interventionService = interventionService;
        this.deduplicationService = deduplicationService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.vehicle:vehicule_topic}",
            groupId = "${app.kafka.group-id:maintenance-service}",
            autoStartup = "${app.kafka.enabled:true}"
    )
    public void onVehicleEvent(
            VehicleEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        if (event == null || event.event() == null || event.vehicle_id() == null) {
            return;
        }

        String eventId = resolveEventId(event);
        if (deduplicationService.isProcessed(eventId)) {
            return;
        }

        if ("VEHICLE_DELETED".equals(event.event())) {
            String sagaId = hasText(event.saga_id()) ? event.saga_id() : "saga-" + eventId;
            String correlationId = hasText(event.correlation_id()) ? event.correlation_id() : sagaId;
            interventionService.cancelForVehicleDeletion(event.vehicle_id(), sagaId, correlationId);
        }

        deduplicationService.markProcessed(eventId, topic, partition, offset);
    }

    private String resolveEventId(VehicleEvent event) {
        if (hasText(event.event_id())) {
            return event.event_id();
        }

        String timestamp = event.timestamp() == null ? "na" : event.timestamp().toString();
        return "legacy-" + event.vehicle_id() + "-" + event.event() + "-" + timestamp;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
