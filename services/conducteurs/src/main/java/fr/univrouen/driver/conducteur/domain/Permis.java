package fr.univrouen.driver.conducteur.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "permis")
public class Permis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDate dateValidite;

    @ElementCollection(targetClass = PermisTypes.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "permis_types", joinColumns = @JoinColumn(name = "permis_id"))
    @Column(name = "type", nullable = false)
    private Set<PermisTypes> types;

    public Integer getId() {
        return id;
    }

    public LocalDate getDateValidite() {
        return dateValidite;
    }

    public Set<PermisTypes> getTypes() {
        return types;
    }

    public void setTypes(Set<PermisTypes> types) {
        this.types = types;
    }

    public void setDateValidite(LocalDate dateValidite) {
        this.dateValidite = dateValidite;
    }
}
