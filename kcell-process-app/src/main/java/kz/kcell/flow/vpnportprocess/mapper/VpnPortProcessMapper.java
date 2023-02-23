package kz.kcell.flow.vpnportprocess.mapper;

import kz.kcell.flow.assets.dto.AddressesInputDto;
import kz.kcell.flow.assets.dto.CatalogObjectDto;
import kz.kcell.flow.assets.dto.PortInputDto;
import kz.kcell.flow.assets.dto.VpnInputDto;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.vpnportprocess.variable.AddressCamVar;
import kz.kcell.flow.vpnportprocess.variable.PortCamVar;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import org.springframework.stereotype.Service;

@Service
public class VpnPortProcessMapper {
    public AddressesInputDto map(AddressCamVar address) {
        CatalogObjectDto cityId = CatalogObjectDto.builder()
            .catalogId(32L)
            .id(address.getCityId().getId())
            .build();

        return AddressesInputDto.builder()
            .cityId(cityId)
            .notFullAddress(address.isAddressNotFull())
            .street(address.getStreet())
            .building(address.getBuilding())
            .cadastralNumber(address.getCadastralNumber())
            .note(address.getNote())
            .build();
    }

    public PortInputDto map(PortCamVar port, long addressId, String status) {
        return PortInputDto.builder()
            .portNumber(port.getPortNumber())
            .portCapacity(port.getPortCapacity())
            .portCapacityUnit(port.getPortCapacityUnit())
            .channelType(port.getChannelType())
            .portType(port.getPortType())
            .farEndAddressId(addressId)
            .status(status)
            .build();
    }

    public PortInputDto map(PortCamVar port, String status) {
        return PortInputDto.builder()
            .portNumber(port.getPortNumber())
            .portCapacity(port.getPortCapacity())
            .portCapacityUnit(port.getPortCapacityUnit())
            .channelType(port.getChannelType())
            .portType(port.getPortType())
            .farEndAddressId(port.getFarEndAddress().getId())
            .status(status)
            .build();
    }

    public PortInputDto mapModifiedPort(PortCamVar port, String status) {
        return PortInputDto.builder()
            .portNumber(port.getPortNumber())
            .portCapacity(port.getModifiedPortCapacity())
            .portCapacityUnit(port.getModifiedPortCapacityUnit())
            .channelType(port.getChannelType())
            .portType(port.getPortType())
            .farEndAddressId(port.getFarEndAddress().getId())
            .status(status)
            .build();
    }

    public VpnInputDto mapFromAddedVpn(VpnCamVar vpn, long addressId, String vlan, String status) {
        return VpnInputDto.builder()
            .vpnNumber(vpn.getVpnNumber())
            .portId(vpn.getPort().getId())
            .service(vpn.getService())
            .serviceTypeId(vpn.getServiceTypeId())
            .serviceTypeCatalogId(vpn.getServiceTypeCatalogId())
            .providerIp(vpn.getProviderIp())
            .kcellIp(vpn.getKcellIp())
            .vlan(vlan)
            .serviceCapacity(vpn.getServiceCapacity())
            .providerAs(vpn.getService().equals("L3") ? "9198" : null)
            .kcellAs(vpn.getService().equals("L3") ? "29355" : null)
            .nearEndAddressId(addressId)
            .status(status)
            .build();
    }

    public VpnInputDto mapFromVpnOutputDto(VpnOutputDto vpn) {
        return VpnInputDto.builder()
            .vpnNumber(vpn.getVpnNumber())
            .portId(vpn.getPort().getId())
            .service(vpn.getService())
            .serviceTypeId(vpn.getServiceTypeId())
            .serviceTypeCatalogId(vpn.getServiceTypeCatalogId())
            .providerIp(vpn.getProviderIp())
            .kcellIp(vpn.getKcellIp())
            .vlan(vpn.getVlan())
            .serviceCapacity(vpn.getServiceCapacity())
            .providerAs(vpn.getProviderAs())
            .kcellAs(vpn.getKcellAs())
            .nearEndAddressId(vpn.getNearEndAddress().getId())
            .status(vpn.getStatus())
            .build();
    }

    public VpnInputDto mapFromDisbandedVpn(VpnCamVar vpn, String status) {
        return VpnInputDto.builder()
            .vpnNumber(vpn.getVpnNumber())
            .portId(vpn.getPort().getId())
            .service(vpn.getService())
            .serviceTypeId(vpn.getServiceTypeId())
            .serviceTypeCatalogId(vpn.getServiceTypeCatalogId())
            .providerIp(vpn.getProviderIp())
            .kcellIp(vpn.getKcellIp())
            .vlan(vpn.getVlan())
            .serviceCapacity(vpn.getServiceCapacity())
            .providerAs(vpn.getProviderAs())
            .kcellAs(vpn.getKcellAs())
            .nearEndAddressId(vpn.getNearEndAddress().getId())
            .status(status)
            .build();
    }

    public VpnInputDto mapFromModifiedVpn(VpnCamVar vpn, String status) {
        return VpnInputDto.builder()
            .vpnNumber(vpn.getVpnNumber())
            .portId(vpn.getPort().getId())
            .service(vpn.getService())
            .serviceTypeId(vpn.getServiceTypeId())
            .serviceTypeCatalogId(vpn.getServiceTypeCatalogId())
            .providerIp(vpn.getProviderIp())
            .kcellIp(vpn.getKcellIp())
            .vlan(vpn.getVlan())
            .serviceCapacity(vpn.getModifiedServiceCapacity()) // set modified service capacity
            .providerAs(vpn.getProviderAs())
            .kcellAs(vpn.getKcellAs())
            .nearEndAddressId(vpn.getNearEndAddress().getId())
            .status(status)
            .build();
    }
}
