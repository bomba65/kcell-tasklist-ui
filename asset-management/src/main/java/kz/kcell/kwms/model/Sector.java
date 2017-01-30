package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
public @Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"version"})
class Sector {
    @Id
    Long id;

    String name;

    @Version long version;

}
