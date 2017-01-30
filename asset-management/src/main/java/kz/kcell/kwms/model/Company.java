package kz.kcell.kwms.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
public class Company {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    boolean special;
}
