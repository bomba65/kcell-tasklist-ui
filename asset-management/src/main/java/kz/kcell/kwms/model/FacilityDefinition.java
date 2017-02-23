package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "facility_definition")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"version"})
public class FacilityDefinition implements Definition {

    @Id
    String id;

    String name;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String schema = "{}";

    @Version
    long version;
}
