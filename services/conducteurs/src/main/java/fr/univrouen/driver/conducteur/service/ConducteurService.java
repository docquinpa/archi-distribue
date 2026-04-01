package fr.univrouen.driver.conducteur.service;

import java.util.List;
import org.springframework.stereotype.Service;
import fr.univrouen.driver.conducteur.api.ConducteurNotFoundException;
import fr.univrouen.driver.conducteur.api.ConducteurResponse;
import fr.univrouen.driver.conducteur.domain.Conducteur;
import fr.univrouen.driver.conducteur.messaging.ConducteurEventPublisher;
import fr.univrouen.driver.conducteur.repository.ConducteurRepository;

@Service
public class ConducteurService {
    private final ConducteurRepository conducteurRepository;
    private final ConducteurEventPublisher conducteurEventPublisher;

    public ConducteurService(ConducteurRepository conducteurRepository, ConducteurEventPublisher conducteurEventPublisher) {
        this.conducteurRepository = conducteurRepository;
        this.conducteurEventPublisher = conducteurEventPublisher;
    }

    public List<ConducteurResponse> findAll() {
        return conducteurRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ConducteurResponse findById(Integer id) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new ConducteurNotFoundException(id));
        return toResponse(conducteur);
    }

    public ConducteurResponse create(Conducteur conducteur) {
        Conducteur saved = conducteurRepository.save(conducteur);
        ConducteurResponse response = toResponse(saved);
        conducteurEventPublisher.publishCreated(response);
        return response;
    }

    public ConducteurResponse update(Integer id, Conducteur conducteur) {
        Conducteur existing = conducteurRepository.findById(id)
                .orElseThrow(() -> new ConducteurNotFoundException(id));
        existing.setPrenom(conducteur.getPrenom());
        existing.setNom(conducteur.getNom());
        existing.setPermis(conducteur.getPermis());
        Conducteur updated = conducteurRepository.save(existing);
        ConducteurResponse response = toResponse(updated);
        conducteurEventPublisher.publishUpdated(response);
        return response;
    }

    public ConducteurResponse patch(Integer id, String prenom, String nom) {
        Conducteur existing = conducteurRepository.findById(id)
                .orElseThrow(() -> new ConducteurNotFoundException(id));
        if (prenom != null && !prenom.isBlank()) {
            existing.setPrenom(prenom);
        }
        if (nom != null && !nom.isBlank()) {
            existing.setNom(nom);
        }
        Conducteur updated = conducteurRepository.save(existing);
        ConducteurResponse response = toResponse(updated);
        conducteurEventPublisher.publishUpdated(response);
        return response;
    }

    public void delete(Integer id) {
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new ConducteurNotFoundException(id));
        conducteurRepository.delete(conducteur);
        conducteurEventPublisher.publishDeleted(id);
    }

    private ConducteurResponse toResponse(Conducteur conducteur) {
        return new ConducteurResponse(
                conducteur.getId(),
                conducteur.getPrenom(),
                conducteur.getNom(),
                conducteur.getPermis()
        );
    }
}
