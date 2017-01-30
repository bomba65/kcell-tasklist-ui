package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "equipment_instance")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"definition", "version"})
public class EquipmentInstance implements Instance<EquipmentDefinition> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String sn;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    EquipmentDefinition definition;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @Version
    long version;
}
