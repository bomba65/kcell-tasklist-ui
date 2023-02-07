package kz.kcell.flow.assets.dto;

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
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VpnOutputDto {
    private String vpnNumber;
    private PortOutputDto port;
    private String service;
    private Long serviceTypeId;
    private Long serviceTypeCatalogId;
    private String providerIp;
    private String kcellIp;
    private String vlan;
    private String serviceCapacity;
    private String providerAs;
    private String kcellAs;
    private AdressesOutputDto nearEndAddress;
    private String status;
}
