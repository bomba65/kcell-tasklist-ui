package kz.kcell.flow.assets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VpnOutputDto {
    private Long id;
    private String vpnNumber;
    private PortOutputDto port;
    private String service;
    private Long serviceTypeId;
    private Long serviceTypeCatalogId;
    private String providerIp;
    private String kcellIp;
    private String vlan;
    private Integer serviceCapacity;
    private String providerAs;
    private String kcellAs;
    @JsonProperty("vpn_termination_point_2")
    private AdressesOutputDto vpnTerminationPoint2;
    private String status;
}
