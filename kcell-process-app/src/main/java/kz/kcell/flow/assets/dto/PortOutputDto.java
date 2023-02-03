package kz.kcell.flow.assets.dto;

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
public class PortOutputDto {
    private Long id;
    private String portNumber;
    private Integer portCapacity;
    private String portCapacityUnit;
    private String channelType;
    private String portType;
    private AdressesOutputDto farEndAddress;
    private String status;
}
