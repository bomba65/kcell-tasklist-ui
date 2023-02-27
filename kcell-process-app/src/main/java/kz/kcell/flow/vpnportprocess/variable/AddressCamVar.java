package kz.kcell.flow.vpnportprocess.variable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressCamVar {
    private Long id;
    private CityCamVar cityId;
    private boolean addressNotFull;
    private String street;
    private String building;
    private String cadastralNumber;
    private String note;
}
