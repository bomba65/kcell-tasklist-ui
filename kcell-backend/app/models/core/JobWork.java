package models.core;

import javax.persistence.*;

@Entity
@Table(name = "A_JOB_WORK")
public class JobWork {

    private Long id;
    private Job job;
    private Work work;
    private Long quantity;
    private Long materialQuantity;
    private Unit unit;
    private Site site;

    @Id
    @GeneratedValue
    @Column(name = "ID_")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "JOB_")
    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    @ManyToOne
    @JoinColumn(name = "WORK_")
    public Work getWork() {
        return work;
    }

    public void setWork(Work work) {
        this.work = work;
    }

    @Column(name = "QUANTITY_")
    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    @Column(name = "MATERIAL_QUANTITY_")
    public Long getMaterialQuantity() {
        return materialQuantity;
    }

    public void setMaterialQuantity(Long materialQuantity) {
        this.materialQuantity = materialQuantity;
    }

    @ManyToOne
    @JoinColumn(name = "UNIT_")
    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @ManyToOne
    @JoinColumn(name = "SITE_")
    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
}
