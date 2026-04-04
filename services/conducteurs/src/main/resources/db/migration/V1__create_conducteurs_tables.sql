CREATE TABLE IF NOT EXISTS permis (
    id SERIAL PRIMARY KEY,
    date_validite DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS conducteurs (
    id SERIAL PRIMARY KEY,
    prenom VARCHAR(255) NOT NULL,
    nom VARCHAR(255) NOT NULL,
    permis_id INTEGER UNIQUE,
    CONSTRAINT fk_conducteurs_permis
        FOREIGN KEY (permis_id)
        REFERENCES permis(id)
);

CREATE TABLE IF NOT EXISTS permis_types (
    permis_id INTEGER NOT NULL,
    type VARCHAR(255) NOT NULL,
    CONSTRAINT pk_permis_types PRIMARY KEY (permis_id, type),
    CONSTRAINT fk_permis_types_permis
        FOREIGN KEY (permis_id)
        REFERENCES permis(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_permis_types_permis_id ON permis_types(permis_id);
