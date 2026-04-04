package fr.univrouen.maintenance.intervention.repository;

import fr.univrouen.maintenance.intervention.domain.MaintenanceHistoryEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceHistoryRepository extends JpaRepository<MaintenanceHistoryEntry, Long> {

    List<MaintenanceHistoryEntry> findAllByOrderByOccurredAtDesc();

    List<MaintenanceHistoryEntry> findByInterventionIdOrderByOccurredAtDesc(Integer interventionId);
}
