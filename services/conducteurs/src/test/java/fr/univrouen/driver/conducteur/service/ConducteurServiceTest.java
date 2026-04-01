package fr.univrouen.driver.conducteur.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.univrouen.driver.conducteur.api.ConducteurNotFoundException;
import fr.univrouen.driver.conducteur.domain.Conducteur;
import fr.univrouen.driver.conducteur.domain.Permis;
import fr.univrouen.driver.conducteur.messaging.ConducteurEventPublisher;
import fr.univrouen.driver.conducteur.repository.ConducteurRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConducteurServiceTest {

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private ConducteurEventPublisher conducteurEventPublisher;

    private ConducteurService conducteurService;

    @BeforeEach
    void setUp() {
        conducteurService = new ConducteurService(conducteurRepository, conducteurEventPublisher);
    }

    @Test
    void createShouldPersistAndPublishCreatedEvent() {
        Conducteur saved = new Conducteur();
        saved.setPrenom("Jean");
        saved.setNom("Dupond");
        Permis permis = new Permis();
        permis.setDateValidite(java.time.LocalDate.now());
        permis.setTypes(java.util.Set.of(fr.univrouen.driver.conducteur.domain.PermisTypes.B));
        saved.setPermis(permis);

        when(conducteurRepository.save(org.mockito.ArgumentMatchers.any(Conducteur.class))).thenReturn(saved);

        var response = conducteurService.create(saved);

        assertThat(response.prenom()).isEqualTo("Jean");
        assertThat(response.nom()).isEqualTo("Dupond");
        assertThat(response.permis()).isNotNull();
        verify(conducteurEventPublisher).publishCreated(response);
    }

    @Test
    void patchShouldUpdateOnlyNomEtPrenom() {
        Conducteur existing = new Conducteur();
        existing.setPrenom("Alice");
        existing.setNom("Martin");

        when(conducteurRepository.findById(1)).thenReturn(Optional.of(existing));
        when(conducteurRepository.save(existing)).thenReturn(existing);

        var response = conducteurService.patch(1, "Alicia", null);

        assertThat(response.prenom()).isEqualTo("Alicia");
        assertThat(response.nom()).isEqualTo("Martin");
        verify(conducteurEventPublisher).publishUpdated(response);
    }

    @Test
    void deleteShouldThrowWhenNotFound() {
        when(conducteurRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conducteurService.delete(99))
                .isInstanceOf(ConducteurNotFoundException.class);
    }
}
