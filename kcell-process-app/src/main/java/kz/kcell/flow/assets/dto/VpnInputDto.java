package kz.kcell.flow.assets.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@Builder
public class VpnInputDto {
    private String vpnNumber;
    private Long portId;
    private String service;
    private Long serviceTypeId;
    private Long serviceTypeCatalogId;
    private String providerIp;
    private String kcellIp;
    private String vlan;
    private Integer serviceCapacity;
    private String providerAs;
    private String kcellAs;
    private Long nearEndAddressId;
    private String status;
}
