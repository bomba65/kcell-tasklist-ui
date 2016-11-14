package dtos.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.core.Reason;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReasonDto {

    public Long id;
    public String name;

    public ReasonDto(Reason reason) {
        if (reason != null) {
            this.id = reason.getId();
            this.name = reason.getName();
        }
    }
}
