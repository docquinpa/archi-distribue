package fr.univrouen.driver.conducteur.api;

public class ConducteurNotFoundException extends RuntimeException {

    public ConducteurNotFoundException(Integer id) {
        super("Conducteur non trouvé: " + id);
    }
}
