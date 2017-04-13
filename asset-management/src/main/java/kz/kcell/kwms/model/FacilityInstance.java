package kz.kcell.kwms.model;

import com.vividsolutions.jts.geom.Point;
import lombok.*;

import javax.persistence.*;
import java.util.*;


@Entity
@Table(name = "facility_instance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"sites", "version"})
@ToString(exclude = "sites")
public class FacilityInstance implements Instance<FacilityDefinition>, Comparable<FacilityInstance> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    FacilityDefinition definition;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @ManyToMany(mappedBy = "facilities")
    @OrderBy("id")
    SortedSet<Site> sites = new TreeSet<>();

    Point location;

    @Version
    long version;

    public static Comparator<FacilityInstance> compareById = Comparator.comparing(FacilityInstance::getId);

    @Override
    public int compareTo(FacilityInstance o) {
        return compareById.compare(this, o);
    }
}
