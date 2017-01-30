package kz.kcell.kwms.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@EqualsAndHashCode
@Entity
public class Region {
    @Id
    Long id;

    String name;
}
