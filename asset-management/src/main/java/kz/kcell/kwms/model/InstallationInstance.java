package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.*;
import java.util.Comparator;

@Entity
@Table(name = "installation_instance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"version"})
public class InstallationInstance implements Instance<InstallationDefinition>, Comparable<InstallationInstance> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    InstallationDefinition definition;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @ManyToOne(optional = false)
    Site site;

    @ManyToOne
    FacilityInstance facility;

    @OneToOne(optional = false)
    EquipmentInstance equipment;

    @Version
    long version;

    public static Comparator<InstallationInstance> compareById = Comparator.comparing(InstallationInstance::getId);

    @Override
    public int compareTo(InstallationInstance o) {
        return compareById.compare(this, o);
    }
}
