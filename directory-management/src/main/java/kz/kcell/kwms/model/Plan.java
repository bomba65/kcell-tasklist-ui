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
        name = "address_plan", schema = "netsharing"
)
public  @Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"version"})
class Plan implements Comparable<Plan> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Integer position_number;

    @Size(min = 1)
    String status;

    Boolean is_current;

    @NotBlank
    @JsonRawValue
    @JsonDeserialize(using = JsonAsStringDeserializer.class)
    @Column(columnDefinition = "jsonb default '{}'", nullable = false)
    String params = "{}";

    @Version long version;

    String acceptance_date;

    Boolean start_and_finish;

    public static Comparator<Plan> compareById = Comparator.comparing(Plan::getId);

    @Override
    public int compareTo(Plan o) {
        return compareById.compare(this, o);
    }
}