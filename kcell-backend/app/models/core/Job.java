package models.core;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "A_JOB")
public class Job {

    private Long id;
    private Date requestedDate = new Date();
    private String soaComplaintId;
    private Site site;
    private Reason reason;
    private Contract contract;
    private Contractor contractor;
    private String materialsRequired;
    private String leasingRequired;

    @Id
    @GeneratedValue
    @Column(name = "ID_")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "REQUESTED_DATE_")
    public Date getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(Date requestedDate) {
        this.requestedDate = requestedDate;
    }

    @Column(name = "SOA_COMPLAINT_ID_")
    public String getSoaComplaintId() {
        return soaComplaintId;
    }

    public void setSoaComplaintId(String soaComplaintId) {
        this.soaComplaintId = soaComplaintId;
    }


    @ManyToOne
    @JoinColumn(name = "SITE_")
    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    @ManyToOne
    @JoinColumn(name = "REASON_")
    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    @ManyToOne
    @JoinColumn(name = "CONTRACT_")
    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    @ManyToOne
    @JoinColumn(name = "CONTRACTOR_")
    public Contractor getContractor() {
        return contractor;
    }

    public void setContractor(Contractor contractor) {
        this.contractor = contractor;
    }

    @Column(name = "MATERIALS_REQUIRED_")
    public String getMaterialsRequired() {
        return materialsRequired;
    }

    public void setMaterialsRequired(String materialsRequired) {
        this.materialsRequired = materialsRequired;
    }

    @Column(name = "LEASING_REQUIRED_")
    public String getLeasingRequired() {
        return leasingRequired;
    }

    public void setLeasingRequired(String leasingRequired) {
        this.leasingRequired = leasingRequired;
    }
}
