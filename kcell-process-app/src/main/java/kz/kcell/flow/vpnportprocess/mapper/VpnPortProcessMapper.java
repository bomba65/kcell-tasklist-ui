package kz.kcell.flow.vpnportprocess.mapper;

import kz.kcell.flow.assets.dto.*;
import kz.kcell.flow.vpnportprocess.variable.*;
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
            .notFullAddress(address.getNotFullAddress())
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
            .portTerminationPointId(addressId)
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
            .portTerminationPointId(port.getPortTerminationPoint().getId())
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
            .portTerminationPointId(port.getPortTerminationPoint().getId())
            .status(status)
            .build();
    }

    public PortInputDto revertModifiedPort(PortCamVar port, String status) {
        return PortInputDto.builder()
            .portNumber(port.getPortNumber())
            .portCapacity(port.getPortCapacity())
            .portCapacityUnit(port.getPortCapacityUnit())
            .channelType(port.getChannelType())
            .portType(port.getPortType())
            .portTerminationPointId(port.getPortTerminationPoint().getId())
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
            .providerAs(vpn.getProviderAs() == null && vpn.getService().equals("L3") ? "9198" : vpn.getProviderAs())
            .kcellAs(vpn.getKcellAs() == null && vpn.getService().equals("L3") ? "29355" : vpn.getKcellAs())
            .vpnTerminationPoint2Id(addressId)
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
            .vpnTerminationPoint2Id(vpn.getVpnTerminationPoint2().getId())
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
            .vpnTerminationPoint2Id(vpn.getVpnTerminationPoint2().getId())
            .status(status)
            .build();
    }

    public VpnInputDto mapFromModifiedVpn(VpnCamVar vpn, String status, Integer serviceCapacity) {
        return VpnInputDto.builder()
            .vpnNumber(vpn.getVpnNumber())
            .portId(vpn.getPort().getId())
            .service(vpn.getService())
            .serviceTypeId(vpn.getServiceTypeId())
            .serviceTypeCatalogId(vpn.getServiceTypeCatalogId())
            .providerIp(vpn.getProviderIp())
            .kcellIp(vpn.getKcellIp())
            .vlan(vpn.getVlan())
            .serviceCapacity(serviceCapacity)
            .providerAs(vpn.getProviderAs())
            .kcellAs(vpn.getKcellAs())
            .vpnTerminationPoint2Id(vpn.getVpnTerminationPoint2().getId())
            .status(status)
            .build();
    }

    public VpnInputDto revertModifiedVpn(VpnCamVar vpn, String status) {
        return VpnInputDto.builder()
            .vpnNumber(vpn.getVpnNumber())
            .portId(vpn.getPort().getId())
            .service(vpn.getService())
            .serviceTypeId(vpn.getServiceTypeId())
            .serviceTypeCatalogId(vpn.getServiceTypeCatalogId())
            .providerIp(vpn.getProviderIp())
            .kcellIp(vpn.getKcellIp())
            .vlan(vpn.getVlan())
            .serviceCapacity(vpn.getServiceCapacity()) // set modified service capacity
            .providerAs(vpn.getProviderAs())
            .kcellAs(vpn.getKcellAs())
            .vpnTerminationPoint2Id(vpn.getVpnTerminationPoint2().getId())
            .status(status)
            .build();
    }

    public VpnInputDto mapFromVpn(VpnCamVar vpn, String status) {
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
            .vpnTerminationPoint2Id(vpn.getVpnTerminationPoint2().getId())
            .status(status)
            .build();
    }

    public VpnCamVar map(VpnOutputDto vpn, int modifiedServiceCapacity) {
        return VpnCamVar.builder()
            .id(vpn.getId())
            .vpnNumber(vpn.getVpnNumber())
            .port(map(vpn.getPort()))
            .service(vpn.getService())
            .serviceTypeId(vpn.getServiceTypeId())
            .serviceTypeCatalogId(vpn.getServiceTypeCatalogId())
            .providerIp(vpn.getProviderIp())
            .kcellIp(vpn.getKcellIp())
            .vlan(vpn.getVlan())
            .serviceCapacity(vpn.getServiceCapacity())
            .modifiedServiceCapacity(modifiedServiceCapacity)
            .providerAs(vpn.getProviderAs())
            .kcellAs(vpn.getKcellAs())
            .vpnTerminationPoint2(map(vpn.getVpnTerminationPoint2()))
            .build();
    }

    public PortCamVar map(PortOutputDto port) {
        return PortCamVar.builder()
            .id(port.getId())
            .portNumber(port.getPortNumber())
            .portCapacity(port.getPortCapacity())
            .portCapacityUnit(port.getPortCapacityUnit())
            .channelType(port.getChannelType())
            .portType(port.getPortType())
            .portTerminationPoint(map(port.getPortTerminationPoint()))
            .build();
    }

    public AddressCamVar map(AdressesOutputDto address) {
        return AddressCamVar.builder()
            .id(address.getId())
            .cityId(map(address.getCityId()))
            .street(address.getStreet())
            .building(address.getBuilding())
            .cadastralNumber(address.getCadastralNumber())
            .notFullAddress(address.getNotFullAddress())
            .note(address.getNote())
            .build();
    }

    public CityCamVar map(AddressCityDto city) {
        return CityCamVar.builder()
            .id(city.getId())
            .name(city.getName())
            .districtId(map(city.getDistrictId()))
            .build();
    }

    public DistrictCamVar map(AddressDistrictDto district) {
        return DistrictCamVar.builder()
            .id(district.getId())
            .name(district.getName())
            .oblastId(map(district.getOblastId()))
            .build();
    }

    public OblastCamVar map(AddressOblastDto oblast) {
        return OblastCamVar.builder()
            .id(oblast.getId())
            .name(oblast.getName())
            .build();
    }
}
