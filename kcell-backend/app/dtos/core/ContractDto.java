package dtos.core;

import models.core.Contract;

/**
 * Created by meta4 on 10.11.2016.
 */
public class ContractDto {

    public Long id;
    public String sapId;
    public ContractorDto contractor;
    public ServiceDto service;

    public ContractDto(Contract contract) {
        if (contract != null) {
            this.id = contract.getId();
            this.sapId = contract.getSapId();
            this.contractor = new ContractorDto(contract.getContractor());
            this.service = new ServiceDto(contract.getService());
        }
    }
}
