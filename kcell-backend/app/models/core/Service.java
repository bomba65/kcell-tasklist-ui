package models.core;

import javax.persistence.*;

@Entity
@Table(name = "A_SERVICE")
public class Service {

    private Long id;
    private String name;
    private String sapCode;

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


    @Column(name = "SAP_CODE_")
    public String getSapCode() {
        return sapCode;
    }

    public void setSapCode(String sapCode) {
        this.sapCode = sapCode;
    }
}
