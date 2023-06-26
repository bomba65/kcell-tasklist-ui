package kz.kcell.flow.vpnportprocess.variable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class VpnCamVar {
    private Long id;
    private String vpnNumber;
    private PortCamVar port;
    private String service;
    private Long serviceTypeId;
    private Long serviceTypeCatalogId;
    private String serviceTypeTitle;
    private String providerIp;
    private String kcellIp;
    private String vlan;
    private Integer serviceCapacity;
    private Integer modifiedServiceCapacity;
    private String providerAs;
    private String kcellAs;
    @JsonProperty("vpn_termination_point_2")
    private AddressCamVar vpnTerminationPoint2;
    private Boolean confirmed;
    private Boolean providerConfirmed;
    private Boolean modifyConfirmed;
}
