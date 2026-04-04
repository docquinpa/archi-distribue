package fr.univrouen.maintenance.intervention.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.univrouen.maintenance.intervention.api.InterventionInput;
import fr.univrouen.maintenance.intervention.api.InterventionResponse;
import fr.univrouen.maintenance.intervention.domain.Intervention;
import fr.univrouen.maintenance.intervention.domain.InterventionState;
import fr.univrouen.maintenance.intervention.messaging.MaintenanceEventPublisher;
import fr.univrouen.maintenance.intervention.repository.InterventionRepository;
import fr.univrouen.maintenance.intervention.repository.MaintenanceHistoryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterventionServiceTest {

    @Mock
    private InterventionRepository interventionRepository;
    @Mock
    private MaintenanceHistoryRepository historyRepository;
    @Mock
    private MaintenanceEventPublisher maintenanceEventPublisher;

    private InterventionService interventionService;

    @BeforeEach
    void setUp() {
        interventionService = new InterventionService(interventionRepository, historyRepository, maintenanceEventPublisher);
    }

    @Test
    void create_publishesScheduledEvent_whenStateIsPlanned() {
        InterventionInput input = new InterventionInput(
                7,
                "PLANNED",
                LocalDate.now().plusDays(2),
                BigDecimal.valueOf(120.50),
                40000,
                39500,
                "Revision pre-voyage"
        );

        when(interventionRepository.save(any(Intervention.class))).thenAnswer(invocation -> {
            Intervention intervention = invocation.getArgument(0);
            intervention.setId(10);
            return intervention;
        });

        InterventionResponse response = interventionService.create(input);

        assertThat(response.id()).isEqualTo(10);
        assertThat(response.vehicle_id()).isEqualTo(7);
        assertThat(response.state()).isEqualTo("PLANNED");
        verify(maintenanceEventPublisher).publish(any(InterventionResponse.class), eq("MAINTENANCE_SCHEDULED"), eq(null));
    }

    @Test
    void preventiveAlerts_returnsDateAndMileageBasedCandidates() {
        Intervention dateDue = new Intervention();
        dateDue.setId(1);
        dateDue.setVehicleId(2);
        dateDue.setState(InterventionState.PLANNED);
        dateDue.setScheduledDate(LocalDate.now().plusDays(1));
        dateDue.setPrix(BigDecimal.TEN);

        Intervention mileageDue = new Intervention();
        mileageDue.setId(2);
        mileageDue.setVehicleId(3);
        mileageDue.setState(InterventionState.PLANNED);
        mileageDue.setScheduledDate(LocalDate.now().plusDays(20));
        mileageDue.setPrix(BigDecimal.ONE);
        mileageDue.setMileageThreshold(1000);
        mileageDue.setCurrentMileage(1005);

        when(interventionRepository.findByStateAndScheduledDateLessThanEqual(eq(InterventionState.PLANNED), any(LocalDate.class)))
                .thenReturn(List.of(dateDue));
        when(interventionRepository.findMileageDuePlanned()).thenReturn(List.of(mileageDue));

        List<InterventionResponse> alerts = interventionService.findPreventiveAlerts(7);

        assertThat(alerts).hasSize(2);
        assertThat(alerts).extracting(InterventionResponse::id).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void cancelForVehicleDeletion_propagatesSagaAndCorrelationIds() {
        Intervention planned = new Intervention();
        planned.setId(9);
        planned.setVehicleId(77);
        planned.setState(InterventionState.PLANNED);
        planned.setScheduledDate(LocalDate.now().plusDays(1));
        planned.setPrix(BigDecimal.TEN);

        when(interventionRepository.findByVehicleIdAndStateIn(eq(77), any())).thenReturn(List.of(planned));
        when(interventionRepository.save(any(Intervention.class))).thenAnswer(invocation -> invocation.getArgument(0));

        interventionService.cancelForVehicleDeletion(77, "saga-77", "corr-77");

        verify(maintenanceEventPublisher).publish(any(InterventionResponse.class), eq("MAINTENANCE_CANCELLED"), eq("saga-77"), eq("corr-77"));
    }
}
