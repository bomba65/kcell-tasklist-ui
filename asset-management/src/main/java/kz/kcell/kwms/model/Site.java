package kz.kcell.kwms.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_name", columnNames = "name"))
public class Site {
    @Id
    String id;

    @Size(min = 5)
    String name;

    @ManyToMany
    @JoinTable(name = "site_facility",
            joinColumns = @JoinColumn(name = "site_id"),
            inverseJoinColumns = @JoinColumn(name = "facility_id")
    )
    Set<Facility> facilities;

    @Enumerated(EnumType.STRING)
    FanceType fanceType;

    @ManyToMany
    @JoinTable(name = "site_far_end_candidate",
        joinColumns = @JoinColumn(name = "site_id"),
        inverseJoinColumns = @JoinColumn(name = "far_end_site_id")
    )
    Set<Site> farEndCandidates;

    @Version long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Facility> getFacilities() {
        return facilities;
    }

    public void setFacilities(Set<Facility> facilities) {
        this.facilities = facilities;
    }

    public FanceType getFanceType() {
        return fanceType;
    }

    public void setFanceType(FanceType fanceType) {
        this.fanceType = fanceType;
    }

    public Set<Site> getFarEndCandidates() {
        return farEndCandidates;
    }

    public void setFarEndCandidates(Set<Site> farEndCandidates) {
        this.farEndCandidates = farEndCandidates;
    }

}
