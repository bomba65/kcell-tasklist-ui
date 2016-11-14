package models.core;

import javax.persistence.*;

@Entity
@Table(name = "A_CONTRACT")
public class Contract {

    private Long id;
    private String sapId;
    private Contractor contractor;
    private Service service;

    @Id
    @GeneratedValue
    @Column(name = "ID_")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "SAP_ID_")
    public String getSapId() {
        return sapId;
    }

    public void setSapId(String sapId) {
        this.sapId = sapId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONTRACTOR_")
    public Contractor getContractor() {
        return contractor;
    }

    public void setContractor(Contractor contractor) {
        this.contractor = contractor;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_")
    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
