package kz.kcell.kwms.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import kz.kcell.kwms.jackson.JsonAsStringDeserializer;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.util.Comparator;

@Entity
@Table(name = "power_source")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"version"})
public class PowerSource implements Comparable<PowerSource> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional = false)
    Site site;

    @NotBlank
    @JsonRawValue
    @JsonDeserialize(using = JsonAsStringDeserializer.class)
    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @Version
    long version;

    public static Comparator<PowerSource> compareById = Comparator.comparing(PowerSource::getId);

    @Override
    public int compareTo(PowerSource o) {
        return compareById.compare(this, o);
    }
}
