package dtos.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.core.CostType;
import models.core.Currency;
import models.core.Work;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkDto {

    public Long id;
    public CostType costType;
    public ContractorDto contractor;
    public RegionDto region;
    public ServiceDto service;
    public String sapServiceNumber;
    public String sapPOServiceName;
    public Long netPrice;
    public Currency currency;
    public String faClass;
    public String materialGroup;
    public Long year;
    public String spp;
    public String sppSao;
    public Date createDate;
    public String units;

    public WorkDto() {
    }

    public WorkDto(Work work) {
        if (work != null) {
            this.id = work.getId();
            this.costType = work.getCostType();
            this.contractor = new ContractorDto(work.getContractor());
            this.region = new RegionDto(work.getRegion());
            this.service = new ServiceDto(work.getService());
            this.sapServiceNumber = work.getSapServiceNumber();
            this.sapPOServiceName = work.getSapPOServiceName();
            this.netPrice = work.getNetPrice();
            this.currency = work.getCurrency();
            this.faClass = work.getFaClass();
            this.materialGroup = work.getMaterialGroup();
            this.year = work.getYear();
            this.spp = work.getSpp();
            this.sppSao = work.getSppSao();
            this.createDate = work.getCreateDate();
            this.units = work.getUnits();
        }
    }
}
