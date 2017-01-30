package kz.kcell.kwms.model;

import com.vividsolutions.jts.geom.Point;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "facility")
@Inheritance(strategy = InheritanceType.JOINED)
public @Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"sites", "version"})
@ToString(exclude = "sites")
class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Size(min = 5)
    String name;

    Point location;

    @ManyToMany
    @JoinTable(name = "site_facility",
            joinColumns = @JoinColumn(name = "facility_id"),
            inverseJoinColumns = @JoinColumn(name = "site_id")
    )
    Set<Site> sites = new HashSet<>();

    @Version long version;
}
