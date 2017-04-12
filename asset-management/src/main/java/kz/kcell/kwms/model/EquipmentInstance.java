package kz.kcell.kwms.model;

import lombok.*;
import org.hibernate.annotations.SortComparator;

import javax.persistence.*;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Table(name = "equipment_instance")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"connections", "version"})
public class EquipmentInstance implements Instance<EquipmentDefinition>, Comparable<EquipmentInstance> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String sn;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    EquipmentDefinition definition;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @ManyToMany(targetEntity = ConnectionInstance.class, mappedBy = "equipments")
    @OrderBy("id")
    SortedSet<ConnectionInstance> connections = new TreeSet<>();

    @Version
    long version;

    public static Comparator<EquipmentInstance> compareById = Comparator.comparing(EquipmentInstance::getId);

    @Override
    public int compareTo(EquipmentInstance o) {
        return compareById.compare(this, o);
    }
}
