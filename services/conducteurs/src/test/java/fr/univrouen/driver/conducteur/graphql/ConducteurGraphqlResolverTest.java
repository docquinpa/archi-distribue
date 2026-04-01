package fr.univrouen.driver.conducteur.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.univrouen.driver.conducteur.api.ConducteurResponse;
import fr.univrouen.driver.conducteur.service.ConducteurService;
import fr.univrouen.driver.conducteur.service.PermisService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConducteurGraphqlResolverTest {

    @Mock
    private ConducteurService conducteurService;

    @Mock
    private PermisService permisService;

    private ConducteurGraphqlResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ConducteurGraphqlResolver(conducteurService, permisService);
    }

    @Test
    void driversShouldDelegateToService() {
        when(conducteurService.findAll()).thenReturn(List.of(new ConducteurResponse(1, "Jean", "Dupont", null)));

        var result = resolver.drivers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).prenom()).isEqualTo("Jean");
    }

    @Test
    void deleteDriverShouldCallServiceDeleteAndReturnTrue() {
        var result = resolver.deleteDriver(1);

        assertThat(result).isTrue();
        verify(conducteurService).delete(1);
    }
}
