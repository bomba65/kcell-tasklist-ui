package kz.kcell.kwms.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import kz.kcell.kwms.jackson.JsonAsStringDeserializer;
import lombok.*;
import org.hibernate.annotations.SortComparator;
import org.hibernate.annotations.SortNatural;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Table(
        name = "site",
        uniqueConstraints = @UniqueConstraint(name = "unique_name", columnNames = "name")
)
public @Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"facilities", "farEndCandidates", "installations", "powerSources", "version"})
class Site implements Comparable<Site> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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
    @OrderBy("id")
    SortedSet<FacilityInstance> facilities = new TreeSet<>();

    @ManyToMany(targetEntity = Site.class)
    @JoinTable(name = "site_far_end_candidate",
        joinColumns = @JoinColumn(name = "site_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "far_end_site_id", referencedColumnName = "id")
    )
    @OrderBy("id")
    SortedSet<Site> farEndCandidates = new TreeSet<>();

    @OneToMany(mappedBy = "site")
    @OrderBy("id")
    SortedSet<InstallationInstance> installations = new TreeSet<>();

    @OneToMany(mappedBy = "site")
    @OrderBy("id")
    SortedSet<PowerSource> powerSources = new TreeSet<>();

    @Version long version;

    public static Comparator<Site> compareById = Comparator.comparing(Site::getId);

    @Override
    public int compareTo(Site o) {
        return compareById.compare(this, o);
    }

}
