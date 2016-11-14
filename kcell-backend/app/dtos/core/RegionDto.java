package dtos.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.core.Region;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegionDto {

    public Long id;
    public String name;

    public RegionDto(Region region) {
        if (region != null) {
            this.id = region.getId();
            this.name = region.getName();
        }
    }
}
