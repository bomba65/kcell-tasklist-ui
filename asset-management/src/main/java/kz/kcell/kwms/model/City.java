package kz.kcell.kwms.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class City {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @ManyToOne
    Region region;

}
