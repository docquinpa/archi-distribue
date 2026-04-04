package fr.univrouen.maintenance.intervention.service;

import fr.univrouen.maintenance.intervention.api.InterventionInput;
import fr.univrouen.maintenance.intervention.api.InterventionNotFoundException;
import fr.univrouen.maintenance.intervention.api.InterventionResponse;
import fr.univrouen.maintenance.intervention.api.MaintenanceHistoryResponse;
import fr.univrouen.maintenance.intervention.domain.Intervention;
import fr.univrouen.maintenance.intervention.domain.InterventionState;
import fr.univrouen.maintenance.intervention.domain.MaintenanceHistoryEntry;
import fr.univrouen.maintenance.intervention.messaging.MaintenanceEventPublisher;
import fr.univrouen.maintenance.intervention.repository.InterventionRepository;
import fr.univrouen.maintenance.intervention.repository.MaintenanceHistoryRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final MaintenanceHistoryRepository historyRepository;
    private final MaintenanceEventPublisher maintenanceEventPublisher;

    public InterventionService(
            InterventionRepository interventionRepository,
            MaintenanceHistoryRepository historyRepository,
            MaintenanceEventPublisher maintenanceEventPublisher
    ) {
        this.interventionRepository = interventionRepository;
        this.historyRepository = historyRepository;
        this.maintenanceEventPublisher = maintenanceEventPublisher;
    }

    public List<InterventionResponse> findAll() {
        return interventionRepository.findAll().stream().map(this::toResponse).toList();
    }

    public InterventionResponse findById(Integer id) {
        return toResponse(findEntityById(id));
    }

    public List<MaintenanceHistoryResponse> findHistory() {
        return historyRepository.findAllByOrderByOccurredAtDesc().stream().map(this::toHistoryResponse).toList();
    }

    public List<MaintenanceHistoryResponse> findHistoryByIntervention(Integer interventionId) {
        return historyRepository.findByInterventionIdOrderByOccurredAtDesc(interventionId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    public List<InterventionResponse> findPreventiveAlerts(int withinDays) {
        int safeWithinDays = Math.max(0, withinDays);
        LocalDate thresholdDate = LocalDate.now().plusDays(safeWithinDays);

        Map<Integer, Intervention> alerts = new LinkedHashMap<>();
        interventionRepository.findByStateAndScheduledDateLessThanEqual(InterventionState.PLANNED, thresholdDate)
                .forEach(it -> alerts.put(it.getId(), it));
        interventionRepository.findMileageDuePlanned()
                .forEach(it -> alerts.put(it.getId(), it));

        return alerts.values().stream().map(this::toResponse).toList();
    }

    @Transactional
    public InterventionResponse create(InterventionInput input) {
        Intervention intervention = new Intervention();
        applyInput(intervention, input);
        if (intervention.getState() == InterventionState.COMPLETED && intervention.getCompletedDate() == null) {
            intervention.setCompletedDate(LocalDate.now());
        }

        Intervention saved = interventionRepository.save(intervention);
        String eventType = saved.getState() == InterventionState.PLANNED
                ? "MAINTENANCE_SCHEDULED"
                : "MAINTENANCE_CREATED";

        recordHistory(saved, eventType, input.notes(), null);
        maintenanceEventPublisher.publish(toResponse(saved), eventType, null);
        return toResponse(saved);
    }

    @Transactional
    public InterventionResponse update(Integer id, InterventionInput input) {
        Intervention intervention = findEntityById(id);
        applyInput(intervention, input);
        if (intervention.getState() == InterventionState.COMPLETED && intervention.getCompletedDate() == null) {
            intervention.setCompletedDate(LocalDate.now());
        }
        if (intervention.getState() != InterventionState.COMPLETED) {
            intervention.setCompletedDate(null);
        }

        Intervention saved = interventionRepository.save(intervention);
        recordHistory(saved, "MAINTENANCE_UPDATED", input.notes(), null);
        maintenanceEventPublisher.publish(toResponse(saved), "MAINTENANCE_UPDATED", null);
        return toResponse(saved);
    }

    @Transactional
    public InterventionResponse patch(
            Integer id,
            String state,
            LocalDate date,
            BigDecimal prix,
            Integer mileageThreshold,
            Integer currentMileage,
            String notes
    ) {
        Intervention intervention = findEntityById(id);

        if (state != null && !state.isBlank()) {
            intervention.setState(parseState(state));
        }
        if (date != null) {
            intervention.setScheduledDate(date);
        }
        if (prix != null) {
            intervention.setPrix(prix);
        }
        if (mileageThreshold != null) {
            intervention.setMileageThreshold(mileageThreshold);
        }
        if (currentMileage != null) {
            intervention.setCurrentMileage(currentMileage);
        }
        if (notes != null) {
            intervention.setNotes(notes);
        }

        if (intervention.getState() == InterventionState.COMPLETED && intervention.getCompletedDate() == null) {
            intervention.setCompletedDate(LocalDate.now());
        }

        Intervention saved = interventionRepository.save(intervention);
        recordHistory(saved, "MAINTENANCE_UPDATED", notes, null);
        maintenanceEventPublisher.publish(toResponse(saved), "MAINTENANCE_UPDATED", null);
        return toResponse(saved);
    }

    @Transactional
    public InterventionResponse complete(Integer id, BigDecimal prixOverride) {
        Intervention intervention = findEntityById(id);
        intervention.setState(InterventionState.COMPLETED);
        intervention.setCompletedDate(LocalDate.now());
        if (prixOverride != null) {
            intervention.setPrix(prixOverride);
        }

        Intervention saved = interventionRepository.save(intervention);
        recordHistory(saved, "MAINTENANCE_COMPLETED", null, null);
        maintenanceEventPublisher.publish(toResponse(saved), "MAINTENANCE_COMPLETED", null);
        return toResponse(saved);
    }

    @Transactional
    public InterventionResponse cancel(Integer id, String reason) {
        Intervention intervention = findEntityById(id);
        intervention.setState(InterventionState.CANCELLED);
        intervention.setCompletedDate(LocalDate.now());

        Intervention saved = interventionRepository.save(intervention);
        recordHistory(saved, "MAINTENANCE_CANCELLED", reason, null);
        maintenanceEventPublisher.publish(toResponse(saved), "MAINTENANCE_CANCELLED", null);
        return toResponse(saved);
    }

    @Transactional
    public int cancelForVehicleDeletion(Integer vehicleId) {
        String sagaId = UUID.randomUUID().toString();
        return cancelForVehicleDeletion(vehicleId, sagaId, sagaId);
    }

    @Transactional
    public int cancelForVehicleDeletion(Integer vehicleId, String sagaId, String correlationId) {
        String effectiveSagaId = (sagaId == null || sagaId.isBlank()) ? UUID.randomUUID().toString() : sagaId;
        String effectiveCorrelationId = (correlationId == null || correlationId.isBlank()) ? effectiveSagaId : correlationId;

        List<Intervention> impacted = interventionRepository.findByVehicleIdAndStateIn(
                vehicleId,
                EnumSet.of(InterventionState.PLANNED, InterventionState.IN_PROGRESS)
        );

        int cancelled = 0;
        for (Intervention intervention : impacted) {
            intervention.setState(InterventionState.CANCELLED);
            intervention.setCompletedDate(LocalDate.now());
            Intervention saved = interventionRepository.save(intervention);
            String details = "Cancelled after VEHICLE_DELETED event for vehicle " + vehicleId
                    + " (correlationId=" + effectiveCorrelationId + ")";
            recordHistory(saved, "MAINTENANCE_CANCELLED", details, effectiveSagaId);
            maintenanceEventPublisher.publish(toResponse(saved), "MAINTENANCE_CANCELLED", effectiveSagaId, effectiveCorrelationId);
            cancelled++;
        }

        return cancelled;
    }

    @Transactional
    public void delete(Integer id) {
        Intervention intervention = findEntityById(id);
        recordHistory(intervention, "MAINTENANCE_DELETED", "Intervention deleted", null);
        maintenanceEventPublisher.publish(toResponse(intervention), "MAINTENANCE_DELETED", null);
        interventionRepository.delete(intervention);
    }

    private Intervention findEntityById(Integer id) {
        return interventionRepository.findById(id)
                .orElseThrow(() -> new InterventionNotFoundException(id));
    }

    private void applyInput(Intervention intervention, InterventionInput input) {
        intervention.setVehicleId(input.vehicle_id());
        intervention.setState(parseState(input.state()));
        intervention.setScheduledDate(input.date());
        intervention.setPrix(input.prix());
        intervention.setMileageThreshold(input.mileage_threshold());
        intervention.setCurrentMileage(input.current_mileage());
        intervention.setNotes(input.notes());
    }

    private InterventionState parseState(String state) {
        try {
            return InterventionState.valueOf(state.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unknown maintenance state: " + state);
        }
    }

    private void recordHistory(Intervention intervention, String eventType, String details, String sagaId) {
        MaintenanceHistoryEntry entry = new MaintenanceHistoryEntry();
        entry.setInterventionId(intervention.getId());
        entry.setVehicleId(intervention.getVehicleId());
        entry.setEventType(eventType);
        entry.setDetails(details);
        entry.setSagaId(sagaId);
        historyRepository.save(entry);
    }

    private InterventionResponse toResponse(Intervention intervention) {
        boolean preventiveAlert = intervention.getState() == InterventionState.PLANNED
                && (
                (intervention.getScheduledDate() != null
                        && !intervention.getScheduledDate().isAfter(LocalDate.now().plusDays(7)))
                        || (intervention.getMileageThreshold() != null
                        && intervention.getCurrentMileage() != null
                        && intervention.getCurrentMileage() >= intervention.getMileageThreshold())
        );

        return new InterventionResponse(
                intervention.getId(),
                intervention.getVehicleId(),
                intervention.getState().name(),
                intervention.getScheduledDate(),
                intervention.getCompletedDate(),
                intervention.getPrix(),
                intervention.getMileageThreshold(),
                intervention.getCurrentMileage(),
                intervention.getNotes(),
                preventiveAlert
        );
    }

    private MaintenanceHistoryResponse toHistoryResponse(MaintenanceHistoryEntry entry) {
        return new MaintenanceHistoryResponse(
                entry.getId(),
                entry.getInterventionId(),
                entry.getVehicleId(),
                entry.getEventType(),
                entry.getDetails(),
                entry.getSagaId(),
                entry.getOccurredAt()
        );
    }
}