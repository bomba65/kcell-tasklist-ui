package kz.kcell.flow.assets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PortOutputDto {
    private Long id;
    private String portNumber;
    private Integer portCapacity;
    private String portCapacityUnit;
    private String channelType;
    private String portType;
    private AdressesOutputDto portTerminationPoint;
    private String status;
}
