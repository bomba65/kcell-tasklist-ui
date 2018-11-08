package kz.kcell.kwms.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import kz.kcell.kwms.jackson.JsonAsStringDeserializer;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Comparator;

@Entity
@Table(
        name = "location",
        uniqueConstraints = {
            @UniqueConstraint(name = "location_unique_name", columnNames = "name"),
            @UniqueConstraint(name = "location_unique_sitename", columnNames = "sitename")
        }
)
public  @Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"version"})
class Location implements Comparable<Location> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Size(min = 4)
    String name;

    @Size(min = 5)
    String sitename;

    @NotBlank
    @JsonRawValue
    @JsonDeserialize(using = JsonAsStringDeserializer.class)
    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @Version long version;

    public static Comparator<Location> compareById = Comparator.comparing(Location::getId);

    @Override
    public int compareTo(Location o) {
        return compareById.compare(this, o);
    }
}
