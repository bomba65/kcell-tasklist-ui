package models.core;

import javax.persistence.*;

@Entity
@Table(name = "A_CONTRACTOR")
public class Contractor {

    private Long id;
    private String name;
    private ContractorType type;

    @Id
    @GeneratedValue
    @Column(name = "ID_")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME_", columnDefinition = "TEXT")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_")
    public ContractorType getType() {
        return type;
    }

    public void setType(ContractorType type) {
        this.type = type;
    }
}