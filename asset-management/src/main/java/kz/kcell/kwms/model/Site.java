package kz.kcell.kwms.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import kz.kcell.kwms.jackson.JsonAsStringDeserializer;
import lombok.*;
import org.hibernate.annotations.Tables;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "site",
        uniqueConstraints = @UniqueConstraint(name = "unique_name", columnNames = "name")
)
public @Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"facilities", "farEndCandidates", "version"})
class Site {
    @Id
    String id;

    @Size(min = 5)
    String name;

    @NotBlank
    @JsonRawValue
    @JsonDeserialize(using = JsonAsStringDeserializer.class)
    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @ManyToMany
    @JoinTable(name = "site_facility",
            joinColumns = @JoinColumn(name = "site_id"),
            inverseJoinColumns = @JoinColumn(name = "facility_id")
    )
    Set<FacilityInstance> facilities = new HashSet<>();

    @ManyToMany(targetEntity = Site.class)
    @JoinTable(name = "site_far_end_candidate",
        joinColumns = @JoinColumn(name = "site_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "far_end_site_id", referencedColumnName = "id")
    )
    Set<Site> farEndCandidates = new HashSet<>();

    @Version long version;

}
