package fr.univrouen.maintenance.intervention.messaging;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.univrouen.maintenance.intervention.service.InterventionService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleEventConsumerTest {

    @Mock
    private InterventionService interventionService;
    @Mock
    private KafkaEventDeduplicationService deduplicationService;

    private VehicleEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new VehicleEventConsumer(interventionService, deduplicationService);
    }

    @Test
    void shouldSkipDuplicateEvent() {
        VehicleEvent event = new VehicleEvent(
                4,
                null,
                null,
                "VEHICLE_DELETED",
                Instant.now(),
                "1.0",
                "evt-dup",
                "saga-dup",
                "corr-dup"
        );

        when(deduplicationService.isProcessed("evt-dup")).thenReturn(true);

        consumer.onVehicleEvent(event, "vehicule_topic", 0, 42L);

        verify(interventionService, never()).cancelForVehicleDeletion(4, "saga-dup", "corr-dup");
        verify(deduplicationService, never()).markProcessed("evt-dup", "vehicule_topic", 0, 42L);
    }

    @Test
    void shouldProcessFirstDeliveryAndPropagateSagaMetadata() {
        VehicleEvent event = new VehicleEvent(
                5,
                null,
                null,
                "VEHICLE_DELETED",
                Instant.now(),
                "1.0",
                "evt-1",
                "saga-1",
                "corr-1"
        );

        when(deduplicationService.isProcessed("evt-1")).thenReturn(false);

        consumer.onVehicleEvent(event, "vehicule_topic", 1, 99L);

        verify(interventionService).cancelForVehicleDeletion(5, "saga-1", "corr-1");
        verify(deduplicationService).markProcessed("evt-1", "vehicule_topic", 1, 99L);
    }
}