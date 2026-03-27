package fr.univrouen.vehicules.vehicle.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.univrouen.vehicules.vehicle.api.VehicleResponse;
import fr.univrouen.vehicules.vehicle.service.VehicleService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleGraphqlResolverTest {

    @Mock
    private VehicleService vehicleService;

    private VehicleGraphqlResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new VehicleGraphqlResolver(vehicleService);
    }

    @Test
    void vehiclesShouldDelegateToService() {
        when(vehicleService.findAll()).thenReturn(List.of(new VehicleResponse(1, "VIN-A", true)));

        var result = resolver.vehicles();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().vin()).isEqualTo("VIN-A");
    }

    @Test
    void updateMutationShouldDelegatePatch() {
        var updated = new VehicleResponse(4, "VIN-UP", false);
        when(vehicleService.patch(4, "VIN-UP", false)).thenReturn(updated);

        var result = resolver.updateVehicle(4, "VIN-UP", false);

        assertThat(result).isEqualTo(updated);
        verify(vehicleService).patch(4, "VIN-UP", false);
    }
}
