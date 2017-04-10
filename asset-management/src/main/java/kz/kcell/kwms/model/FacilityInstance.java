package kz.kcell.kwms.model;

import com.vividsolutions.jts.geom.Point;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "facility_instance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"definition", "sites", "version"})
@ToString(exclude = {"definition", "sites"})
public class FacilityInstance implements Instance<FacilityDefinition> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    FacilityDefinition definition;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @ManyToMany(mappedBy = "facilities")
    @OrderBy("id")
    List<Site> sites = new ArrayList<>();

    Point location;

    @Version
    long version;

}
