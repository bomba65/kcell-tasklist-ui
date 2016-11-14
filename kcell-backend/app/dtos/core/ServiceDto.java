package dtos.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.core.Service;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceDto {

    public Long id;
    public String name;
    public String sapCode;

    public ServiceDto(Service service) {
        if (service != null) {
            this.id = service.getId();
            this.name = service.getName();
            this.sapCode = service.getSapCode();
        }
    }
}
