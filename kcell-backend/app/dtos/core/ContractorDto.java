package dtos.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.core.Contractor;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractorDto {

    public Long id;
    public String name;

    public ContractorDto(Contractor contractor) {
        if (contractor != null) {
            this.id = contractor.getId();
            this.name = contractor.getName();
        }
    }
}
