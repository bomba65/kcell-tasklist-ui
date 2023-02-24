package kz.kcell.flow.assets.client;

import feign.Param;
import feign.RequestLine;
import kz.kcell.flow.assets.dto.AddressesInputDto;
import kz.kcell.flow.assets.dto.AdressesOutputDto;
import kz.kcell.flow.assets.dto.PortInputDto;
import kz.kcell.flow.assets.dto.PortOutputDto;
import kz.kcell.flow.assets.dto.VpnInputDto;
import kz.kcell.flow.assets.dto.VpnOutputDto;

import java.util.List;

public interface VpnPortClient {
    @RequestLine("POST /vpn")
    VpnOutputDto createNewVpn(VpnInputDto vpnInputDto);

    @RequestLine("GET /vpn/{id}")
    VpnOutputDto getVpnById(@Param("id") Long id);

    @RequestLine("GET /vpn/vpn_number/{vpnNumber}")
    VpnOutputDto getVpnByVpnNumber(@Param("vpnNumber") String vpnNumber);

    @RequestLine("GET /vpn/port_number/{portNumber}")
    List<VpnOutputDto> getVpnsByPortNumber(@Param("portNumber") String portNumber);

    @RequestLine("GET /vpn/far_end_city_id/{farEndCityId}")
    List<VpnOutputDto> getVpnsByFarEndAddress(@Param("farEndCityId") Long farEndCityId);

    @RequestLine("GET /vpn/near_end_city_id/{nearEndCityId}")
    List<VpnOutputDto> getVpnsByNearEndAddress(@Param("nearEndCityId") Long nearEndCityId);

    @RequestLine("PUT /vpn/{id}")
    VpnOutputDto updateVpn(VpnInputDto vpnInputDto, @Param("id") Long id);

    @RequestLine("DELETE /vpn/{id}")
    void deleteVpn(@Param("id") Long id);

    @RequestLine("POST /port")
    PortOutputDto createNewPort(PortInputDto portDto);

    @RequestLine("GET /port/id/{id}")
    PortOutputDto getPortById(@Param("id") Long id);

    @RequestLine("GET /port/port_number/{portNumber}")
    List<PortOutputDto> getPortsByPortNumber(@Param("portNumber") String portNumber);

    @RequestLine("GET /port/city_id/{cityId}")
    List<PortOutputDto> getPortsByCityId(@Param("cityId") Long cityId);

    @RequestLine("PUT /port/id/{id}")
    PortOutputDto updatePort(PortInputDto portDto, @Param("id") Long id);

    @RequestLine("DELETE /port/{id}")
    void deletePorts(@Param("id") Long id);

    @RequestLine("POST /adresses")
    AdressesOutputDto createNewAddress(AddressesInputDto inputDto);
}
