package fr.univrouen.driver.conducteur.graphql;

import fr.univrouen.driver.conducteur.api.ConducteurResponse;
import fr.univrouen.driver.conducteur.api.PermisResponse;
import fr.univrouen.driver.conducteur.domain.Conducteur;
import fr.univrouen.driver.conducteur.domain.Permis;
import fr.univrouen.driver.conducteur.service.ConducteurService;
import fr.univrouen.driver.conducteur.service.PermisService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ConducteurGraphqlResolver {

    private final ConducteurService conducteurService;
    private final PermisService permisService;

    public ConducteurGraphqlResolver(ConducteurService conducteurService, PermisService permisService) {
        this.conducteurService = conducteurService;
        this.permisService = permisService;
    }

    @QueryMapping
    public List<ConducteurResponse> drivers() {
        return conducteurService.findAll();
    }

    @QueryMapping
    public ConducteurResponse driver(@Argument Integer id) {
        return conducteurService.findById(id);
    }

    @MutationMapping
    public ConducteurResponse createDriver(@Argument String prenom, @Argument String nom, @Argument Integer permisId) {
        Permis permis = permisService.findById(permisId);
        Conducteur conducteur = new Conducteur();
        conducteur.setPrenom(prenom);
        conducteur.setNom(nom);
        conducteur.setPermis(permis);
        return conducteurService.create(conducteur);
    }

    @MutationMapping
    public ConducteurResponse updateDriver(@Argument Integer id, @Argument String prenom, @Argument String nom, @Argument Integer permisId) {
        Permis permis = permisService.findById(permisId);
        Conducteur conducteur = new Conducteur();
        conducteur.setPrenom(prenom);
        conducteur.setNom(nom);
        conducteur.setPermis(permis);
        return conducteurService.update(id, conducteur);
    }

    @MutationMapping
    public Boolean deleteDriver(@Argument Integer id) {
        conducteurService.delete(id);
        return true;
    }

    @QueryMapping
    public List<PermisResponse> licenses() {
        return permisService.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @QueryMapping
    public PermisResponse license(@Argument Integer id) {
        return toResponse(permisService.findById(id));
    }

    @MutationMapping
    public PermisResponse createLicense(@Argument String type, @Argument java.time.LocalDate date_valid) {
        return toResponse(permisService.create(type, date_valid));
    }

    @MutationMapping
    public Boolean deleteLicense(@Argument Integer id) {
        permisService.delete(id);
        return true;
    }

    private PermisResponse toResponse(Permis permis) {
        String type = permis.getTypes() == null || permis.getTypes().isEmpty()
                ? null
                : permis.getTypes().iterator().next().name();

        return new PermisResponse(permis.getId(), type, permis.getDateValidite());
    }
}
