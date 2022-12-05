package kz.kcell.kwms.model;

import lombok.*;
import org.hibernate.annotations.SortComparator;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "connection_instance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"equipments", "version"})
public class ConnectionInstance implements Instance<ConnectionDefinition>, Comparable<ConnectionInstance> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    ConnectionDefinition definition;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @ManyToMany(targetEntity = EquipmentInstance.class)
    @JoinTable(name = "connection_equipment",
            joinColumns = @JoinColumn(name = "connection_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id", referencedColumnName = "id")
    )
    @OrderBy("id")
    SortedSet<EquipmentInstance> equipments = new TreeSet<>();

    @Version
    long version;

    public static Comparator<ConnectionInstance> compareById = Comparator.comparing(ConnectionInstance::getId);

    @Override
    public int compareTo(ConnectionInstance o) {
        return compareById.compare(this, o);
    }
}
