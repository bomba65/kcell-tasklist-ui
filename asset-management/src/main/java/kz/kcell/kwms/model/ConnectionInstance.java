package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "connection_instance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"definition", "version"})
public class ConnectionInstance implements Instance<ConnectionDefinition> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    ConnectionDefinition definition;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @ManyToOne(optional = false)
    EquipmentInstance equipmentA;

    @ManyToOne(optional = false)
    EquipmentInstance equipmentB;

    @Version
    long version;
}
