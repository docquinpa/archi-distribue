package fr.univrouen.vehicules.vehicle.repository;

import fr.univrouen.vehicules.vehicle.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
}
