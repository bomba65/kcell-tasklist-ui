package kz.kcell.flow.assets.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PortInputDto {
    private String portNumber;
    private Integer portCapacity;
    private String portCapacityUnit;
    private String channelType;
    private String portType;
    private Long portTerminationPointId;
    private String status;
}
