package fr.univrouen.maintenance.intervention.messaging;

import fr.univrouen.maintenance.intervention.api.InterventionResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String maintenanceTopic;
    private final boolean kafkaEnabled;

    @Autowired
    public MaintenanceEventPublisher(
            ObjectProvider<KafkaTemplate<Object, Object>> kafkaTemplateProvider,
            @Value("${app.kafka.topic.maintenance:maintenance_topic}") String maintenanceTopic,
            @Value("${app.kafka.enabled:true}") boolean kafkaEnabled
    ) {
        this(kafkaTemplateProvider.getIfAvailable(), maintenanceTopic, kafkaEnabled);
    }

    MaintenanceEventPublisher(KafkaTemplate<Object, Object> kafkaTemplate, String maintenanceTopic, boolean kafkaEnabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.maintenanceTopic = maintenanceTopic;
        this.kafkaEnabled = kafkaEnabled;
    }

    public void publish(InterventionResponse intervention, String eventType, String sagaId) {
        publish(intervention, eventType, sagaId, null);
    }

    public void publish(InterventionResponse intervention, String eventType, String sagaId, String correlationId) {
        if (!kafkaEnabled || kafkaTemplate == null || intervention == null) {
            return;
        }

        String eventId = UUID.randomUUID().toString();
        String effectiveCorrelationId = correlationId == null || correlationId.isBlank()
                ? (sagaId == null || sagaId.isBlank() ? eventId : sagaId)
                : correlationId;

        MaintenanceEvent event = new MaintenanceEvent(
                intervention.id(),
                intervention.vehicle_id(),
                eventType,
                intervention.state(),
                intervention.date(),
                intervention.completed_date(),
                intervention.prix(),
                sagaId,
                Instant.now(),
                "1.0",
                eventId,
                effectiveCorrelationId
        );

        kafkaTemplate.send(maintenanceTopic, String.valueOf(intervention.id()), event);
    }
}
