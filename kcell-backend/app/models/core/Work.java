package models.core;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "A_WORK")
public class Work {

    private Long id;
    private CostType costType;
    private Contractor contractor;
    private Region region;
    private Service service;
    private String sapServiceNumber;
    private String sapPOServiceName;
    private Long netPrice;
    private Currency currency;
    private String faClass;
    private String materialGroup;
    private Long year;
    private String spp;
    private String sppSao;
    private Date createDate;

    @Id
    @GeneratedValue
    @Column(name = "ID_")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "COST_TYPE_")
    public CostType getCostType() {
        return costType;
    }

    public void setCostType(CostType costType) {
        this.costType = costType;
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
    @JoinColumn(name = "REGION_")
    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_")
    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    @Column(name = "SAP_SERVICE_NUMBER_")
    public String getSapServiceNumber() {
        return sapServiceNumber;
    }

    public void setSapServiceNumber(String sapServiceNumber) {
        this.sapServiceNumber = sapServiceNumber;
    }

    @Column(name = "SAP_PO_SERVICE_NAME_")
    public String getSapPOServiceName() {
        return sapPOServiceName;
    }

    public void setSapPOServiceName(String sapPOServiceName) {
        this.sapPOServiceName = sapPOServiceName;
    }

    @Column(name = "NET_PRICE_")
    public Long getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(Long netPrice) {
        this.netPrice = netPrice;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "CURRENCY_")
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @Column(name = "FA_CLASS_")
    public String getFaClass() {
        return faClass;
    }

    public void setFaClass(String faClass) {
        this.faClass = faClass;
    }

    @Column(name = "MATERIAL_GROUP_")
    public String getMaterialGroup() {
        return materialGroup;
    }

    public void setMaterialGroup(String materialGroup) {
        this.materialGroup = materialGroup;
    }

    @Column(name = "YEAR_")
    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    @Column(name = "SPP_")
    public String getSpp() {
        return spp;
    }

    public void setSpp(String spp) {
        this.spp = spp;
    }

    @Column(name = "SPP_SAO_")
    public String getSppSao() {
        return sppSao;
    }

    public void setSppSao(String sppSao) {
        this.sppSao = sppSao;
    }

    @Column(name = "CREATE_DATE_")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
