package fr.univrouen.vehicules.vehicle.service;

import fr.univrouen.vehicules.vehicle.api.VehicleInput;
import fr.univrouen.vehicules.vehicle.api.VehicleNotFoundException;
import fr.univrouen.vehicules.vehicle.api.VehicleResponse;
import fr.univrouen.vehicules.vehicle.domain.Vehicle;
import fr.univrouen.vehicules.vehicle.messaging.VehicleEventPublisher;
import fr.univrouen.vehicules.vehicle.repository.VehicleRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleEventPublisher vehicleEventPublisher;

    public VehicleService(VehicleRepository vehicleRepository, VehicleEventPublisher vehicleEventPublisher) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleEventPublisher = vehicleEventPublisher;
    }

    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public VehicleResponse findById(Integer id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
        return toResponse(vehicle);
    }

    public VehicleResponse create(VehicleInput input) {
        Vehicle vehicle = new Vehicle();
        vehicle.setVin(input.vin());
        vehicle.setDispo(input.dispo());
        VehicleResponse response = toResponse(vehicleRepository.save(vehicle));
        vehicleEventPublisher.publishCreated(response);
        return response;
    }

    public VehicleResponse update(Integer id, VehicleInput input) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
        vehicle.setVin(input.vin());
        vehicle.setDispo(input.dispo());
        VehicleResponse response = toResponse(vehicleRepository.save(vehicle));
        vehicleEventPublisher.publishUpdated(response);
        return response;
    }

    public VehicleResponse patch(Integer id, String vin, Boolean dispo) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        if (vin != null && !vin.isBlank()) {
            vehicle.setVin(vin);
        }
        if (dispo != null) {
            vehicle.setDispo(dispo);
        }

        VehicleResponse response = toResponse(vehicleRepository.save(vehicle));
        vehicleEventPublisher.publishUpdated(response);
        return response;
    }

    public void delete(Integer id) {
        if (!vehicleRepository.existsById(id)) {
            throw new VehicleNotFoundException(id);
        }
        vehicleRepository.deleteById(id);
        vehicleEventPublisher.publishDeleted(id);
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(vehicle.getId(), vehicle.getVin(), vehicle.getDispo());
    }
}
