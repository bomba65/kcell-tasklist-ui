package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "equipment_definition")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"version"})
public class EquipmentDefinition implements Definition {
    @Id
    String id;

    String name;

    String vendor;

    String gtin;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String schema = "{}";

    @Version
    long version;

}
