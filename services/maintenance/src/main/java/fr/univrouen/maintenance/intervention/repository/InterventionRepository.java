package fr.univrouen.maintenance.intervention.repository;

import fr.univrouen.maintenance.intervention.domain.Intervention;
import fr.univrouen.maintenance.intervention.domain.InterventionState;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InterventionRepository extends JpaRepository<Intervention, Integer> {

    List<Intervention> findByStateAndScheduledDateLessThanEqual(InterventionState state, LocalDate scheduledDate);

    List<Intervention> findByVehicleIdAndStateIn(Integer vehicleId, Collection<InterventionState> states);

    @Query("""
            select i from Intervention i
            where i.state = 'PLANNED'
              and i.mileageThreshold is not null
              and i.currentMileage is not null
              and i.currentMileage >= i.mileageThreshold
            """)
    List<Intervention> findMileageDuePlanned();
}
