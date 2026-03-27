package fr.univrouen.vehicules.vehicle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.univrouen.vehicules.vehicle.api.VehicleInput;
import fr.univrouen.vehicules.vehicle.api.VehicleNotFoundException;
import fr.univrouen.vehicules.vehicle.domain.Vehicle;
import fr.univrouen.vehicules.vehicle.messaging.VehicleEventPublisher;
import fr.univrouen.vehicules.vehicle.repository.VehicleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleEventPublisher vehicleEventPublisher;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleRepository, vehicleEventPublisher);
    }

    @Test
    void createShouldPersistAndPublishEvent() {
        Vehicle persisted = new Vehicle();
        persisted.setId(10);
        persisted.setVin("VIN-10");
        persisted.setDispo(true);

        when(vehicleRepository.save(org.mockito.ArgumentMatchers.any(Vehicle.class))).thenReturn(persisted);

        var response = vehicleService.create(new VehicleInput("VIN-10", true));

        assertThat(response.id()).isEqualTo(10);
        assertThat(response.vin()).isEqualTo("VIN-10");
        assertThat(response.dispo()).isTrue();
        verify(vehicleEventPublisher).publishCreated(response);
    }

    @Test
    void patchShouldUpdateOnlyProvidedFields() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(12);
        vehicle.setVin("OLD");
        vehicle.setDispo(true);

        when(vehicleRepository.findById(12)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        var response = vehicleService.patch(12, "NEW", null);

        assertThat(response.vin()).isEqualTo("NEW");
        assertThat(response.dispo()).isTrue();
        verify(vehicleEventPublisher).publishUpdated(response);
    }

    @Test
    void deleteShouldThrowWhenNotFound() {
        when(vehicleRepository.existsById(44)).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.delete(44))
                .isInstanceOf(VehicleNotFoundException.class);
    }
}
