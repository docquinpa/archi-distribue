package fr.univrouen.driver.conducteur.service;

import fr.univrouen.driver.conducteur.api.PermisNotFoundException;
import fr.univrouen.driver.conducteur.domain.Permis;
import fr.univrouen.driver.conducteur.domain.PermisTypes;
import fr.univrouen.driver.conducteur.repository.PermisRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PermisService {

    private final PermisRepository permisRepository;

    public PermisService(PermisRepository permisRepository) {
        this.permisRepository = permisRepository;
    }

    public List<Permis> findAll() {
        return permisRepository.findAll();
    }

    public Permis findById(Integer id) {
        return permisRepository.findById(id)
                .orElseThrow(() -> new PermisNotFoundException(id));
    }

    public Permis create(String type, java.time.LocalDate date_valid) {
        Permis permis = new Permis();
        permis.setDateValidite(date_valid);
        Set<PermisTypes> types = Set.of(PermisTypes.valueOf(type));
        permis.setTypes(types);
        return permisRepository.save(permis);
    }

    public void delete(Integer id) {
        if (!permisRepository.existsById(id)) {
            throw new PermisNotFoundException(id);
        }
        permisRepository.deleteById(id);
    }
}
