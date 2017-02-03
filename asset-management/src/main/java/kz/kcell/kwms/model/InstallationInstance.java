package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "installation_instance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"definition", "version"})
public class InstallationInstance implements Instance<InstallationDefinition> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    InstallationDefinition definition;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @ManyToOne(optional = false)
    Site site;

    @ManyToOne(optional = false)
    Facility facility;

    @OneToOne(optional = false)
    EquipmentInstance equipment;

    @Version
    long version;
}
