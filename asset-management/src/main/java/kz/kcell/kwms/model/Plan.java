package kz.kcell.kwms.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import kz.kcell.kwms.jackson.JsonAsStringDeserializer;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Comparator;
import java.util.Date;

@Entity
@Table(
        name = "address_plan"
)
public  @Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"version"})
class Plan implements Comparable<Plan> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Site site;

    @Size(min = 4)
    String status;

    Date acceptance_date;

    Boolean is_current;

    @NotBlank
    @JsonRawValue
    @JsonDeserialize(using = JsonAsStringDeserializer.class)
    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @Version long version;

    public static Comparator<Plan> compareById = Comparator.comparing(Plan::getId);

    @Override
    public int compareTo(Plan o) {
        return compareById.compare(this, o);
    }
}
