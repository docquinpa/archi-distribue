package fr.univrouen.vehicules.vehicle.messaging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.univrouen.vehicules.vehicle.domain.Vehicle;
import fr.univrouen.vehicules.vehicle.repository.VehicleRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleEventConsumerTest {

    @Mock
    private VehicleRepository vehicleRepository;

    private VehicleEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new VehicleEventConsumer(vehicleRepository);
    }

    @Test
    void shouldUpsertOnCreatedEvent() {
        when(vehicleRepository.findById(7)).thenReturn(Optional.empty());

        consumer.onVehicleEvent(new VehicleEvent(7, "VIN-EVT", true, "VEHICLE_CREATED", Instant.now(), "1.0", "evt-1", null, "corr-1"));

        verify(vehicleRepository).save(org.mockito.ArgumentMatchers.any(Vehicle.class));
    }

    @Test
    void shouldDeleteOnDeletedEvent() {
        consumer.onVehicleEvent(new VehicleEvent(8, null, null, "VEHICLE_DELETED", Instant.now(), "1.0", "evt-2", "saga-1", "corr-2"));

        verify(vehicleRepository).deleteById(8);
    }
}
