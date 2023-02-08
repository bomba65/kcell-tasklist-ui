package kz.kcell.flow.vpnportprocess.variable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityCamVar {
    Long id;
}
