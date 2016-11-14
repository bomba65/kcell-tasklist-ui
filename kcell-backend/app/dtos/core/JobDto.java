package dtos.core;

import models.core.Job;

import java.util.Date;

public class JobDto {

    public Long id;
    public Date requestedDate;
    public String soaComplaintId;
    public SiteDto site;
    public ReasonDto reason;
    public ContractDto contract;
    public ContractorDto contractor;
    public String materialsRequired;
    public String leasingRequired;

    public JobDto(){}

    public JobDto(Job job){
        if(job!=null){
            this.id = job.getId();
            this.requestedDate = job.getRequestedDate();
            this.soaComplaintId = job.getSoaComplaintId();
            this.site = new SiteDto(job.getSite());
            this.reason = new ReasonDto(job.getReason());
            this.contract = new ContractDto(job.getContract());
            this.contractor = new ContractorDto(job.getContractor());
            this.materialsRequired = job.getMaterialsRequired();
            this.leasingRequired = job.getLeasingRequired();
        }
    }
}
