package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public @Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"version"})
class Sector {
    @Id
    Long id;

    String name;

    @Version long version;

}
