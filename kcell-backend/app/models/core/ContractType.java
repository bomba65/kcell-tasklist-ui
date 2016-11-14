package models.core;

import javax.persistence.*;

@Entity
@Table(name = "A_CONTRACT_TYPE")
public class ContractType {

    private Long id;
    private String name;

    @Id
    @GeneratedValue
    @Column(name = "ID_")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME_")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
