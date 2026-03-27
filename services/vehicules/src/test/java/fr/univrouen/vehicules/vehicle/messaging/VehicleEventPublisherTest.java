package fr.univrouen.vehicules.vehicle.messaging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import fr.univrouen.vehicules.vehicle.api.VehicleResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class VehicleEventPublisherTest {

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Test
    void shouldPublishCreatedEventWhenKafkaEnabled() {
        VehicleEventPublisher publisher = new VehicleEventPublisher(kafkaTemplate, "vehicule_topic", true);

        publisher.publishCreated(new VehicleResponse(1, "VIN-1", true));

        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("vehicule_topic"), org.mockito.ArgumentMatchers.eq("1"), org.mockito.ArgumentMatchers.any(VehicleEvent.class));
    }

    @Test
    void shouldNotPublishWhenKafkaDisabled() {
        VehicleEventPublisher publisher = new VehicleEventPublisher(kafkaTemplate, "vehicule_topic", false);

        publisher.publishDeleted(1);

        verifyNoInteractions(kafkaTemplate);
    }
}
