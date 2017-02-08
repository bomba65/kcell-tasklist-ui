package kz.kcell.kwms.model;

import com.vividsolutions.jts.geom.Point;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "facility_instance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"definition", "version"})
public class FacilityInstance implements Instance<FacilityDefinition> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    FacilityDefinition definition;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @ManyToOne(optional = false)
    Site site;

    Point location;

    @Version
    long version;

    @Override
    public FacilityDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public String getParams() {
        return this.params;
    }
}
