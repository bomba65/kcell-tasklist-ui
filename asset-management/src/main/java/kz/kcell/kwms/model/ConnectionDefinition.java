package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "connection_definition")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"version"})
public class ConnectionDefinition implements Definition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String schema = "{}";

    @Version
    long version;
}
