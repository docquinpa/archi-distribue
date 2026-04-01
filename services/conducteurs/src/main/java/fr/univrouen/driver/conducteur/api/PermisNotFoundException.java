package fr.univrouen.driver.conducteur.api;

public class PermisNotFoundException extends RuntimeException {

    public PermisNotFoundException(Integer id) {
        super("Permis non trouvé: " + id);
    }
}
