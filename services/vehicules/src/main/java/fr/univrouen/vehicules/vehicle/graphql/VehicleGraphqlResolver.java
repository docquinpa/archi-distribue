package fr.univrouen.vehicules.vehicle.graphql;

import fr.univrouen.vehicules.vehicle.api.VehicleInput;
import fr.univrouen.vehicules.vehicle.api.VehicleResponse;
import fr.univrouen.vehicules.vehicle.service.VehicleService;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleGraphqlResolver {

    private final VehicleService vehicleService;

    public VehicleGraphqlResolver(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @QueryMapping
    public List<VehicleResponse> vehicles() {
        return vehicleService.findAll();
    }

    @QueryMapping
    public VehicleResponse vehicle(@Argument Integer id) {
        return vehicleService.findById(id);
    }

    @MutationMapping
    public VehicleResponse createVehicle(@Argument String vin, @Argument Boolean dispo) {
        return vehicleService.create(new VehicleInput(vin, dispo));
    }

    @MutationMapping
    public VehicleResponse updateVehicle(@Argument Integer id, @Argument String vin, @Argument Boolean dispo) {
        return vehicleService.patch(id, vin, dispo);
    }

    @MutationMapping
    public Boolean deleteVehicle(@Argument Integer id) {
        vehicleService.delete(id);
        return true;
    }
}
