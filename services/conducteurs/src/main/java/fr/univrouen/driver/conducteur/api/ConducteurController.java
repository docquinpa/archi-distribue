package fr.univrouen.driver.conducteur.api;

import fr.univrouen.driver.conducteur.domain.Conducteur;
import fr.univrouen.driver.conducteur.domain.Permis;
import fr.univrouen.driver.conducteur.service.ConducteurService;
import fr.univrouen.driver.conducteur.service.PermisService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ConducteurController {

    private final ConducteurService conducteurService;
    private final PermisService permisService;

    public ConducteurController(ConducteurService conducteurService, PermisService permisService) {
        this.conducteurService = conducteurService;
        this.permisService = permisService;
    }

    @GetMapping("/drivers")
    public List<ConducteurResponse> findAllDrivers() {
        return conducteurService.findAll();
    }

    @GetMapping("/drivers/{id}")
    public ConducteurResponse findDriverById(@PathVariable Integer id) {
        return conducteurService.findById(id);
    }

    @PostMapping("/drivers")
    public ResponseEntity<ConducteurResponse> createDriver(@Valid @RequestBody ConducteurInput input) {
        Permis permis = permisService.findById(input.permisId());
        Conducteur conducteur = new Conducteur();
        conducteur.setPrenom(input.prenom());
        conducteur.setNom(input.nom());
        conducteur.setPermis(permis);
        ConducteurResponse created = conducteurService.create(conducteur);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/drivers/{id}")
    public ConducteurResponse updateDriver(@PathVariable Integer id, @Valid @RequestBody ConducteurInput input) {
        Permis permis = permisService.findById(input.permisId());
        Conducteur conducteur = new Conducteur();
        conducteur.setPrenom(input.prenom());
        conducteur.setNom(input.nom());
        conducteur.setPermis(permis);
        return conducteurService.update(id, conducteur);
    }

    @DeleteMapping("/drivers/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Integer id) {
        conducteurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/licenses")
    public List<PermisResponse> findAllLicenses() {
        return permisService.findAll().stream().map(this::toLicenseResponse).collect(Collectors.toList());
    }

    @GetMapping("/licenses/{id}")
    public PermisResponse findLicenseById(@PathVariable Integer id) {
        return toLicenseResponse(permisService.findById(id));
    }

    @PostMapping("/licenses")
    public ResponseEntity<PermisResponse> createLicense(@Valid @RequestBody PermisInput input) {
        Permis permis = permisService.create(input.type(), input.date_valid());
        return ResponseEntity.status(HttpStatus.CREATED).body(toLicenseResponse(permis));
    }

    @DeleteMapping("/licenses/{id}")
    public ResponseEntity<Void> deleteLicense(@PathVariable Integer id) {
        permisService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private PermisResponse toLicenseResponse(Permis permis) {
        String type = permis.getTypes() == null || permis.getTypes().isEmpty() ? null : permis.getTypes().iterator().next().name();
        return new PermisResponse(permis.getId(), type, permis.getDateValidite());
    }
}

