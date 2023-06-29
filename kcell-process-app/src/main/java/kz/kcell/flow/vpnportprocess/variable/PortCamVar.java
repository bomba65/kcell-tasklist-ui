package kz.kcell.flow.vpnportprocess.variable;

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
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortCamVar {
    private Long id;
    private String portNumber;
    private String channelType;
    private String portType;
    private Integer portCapacity;
    private Integer modifiedPortCapacity;
    private String portCapacityUnit;
    private String modifiedPortCapacityUnit;
    private AddressCamVar portTerminationPoint;
}
