package kz.kcell.flow.assets.client;

import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import kz.kcell.flow.assets.dto.AddressesInputDto;
import kz.kcell.flow.assets.dto.AdressesOutputDto;
import kz.kcell.flow.assets.dto.PortInputDto;
import kz.kcell.flow.assets.dto.PortOutputDto;
import kz.kcell.flow.assets.dto.VpnInputDto;
import kz.kcell.flow.assets.dto.VpnOutputDto;

import java.util.List;
import java.util.Map;

public interface VpnPortClient {
    @RequestLine("POST /vpn")
    VpnOutputDto createNewVpn(VpnInputDto vpnInputDto);

    @RequestLine("GET /vpn/{id}")
    VpnOutputDto getVpnById(@Param("id") Long id, @QueryMap Map<String, Object> queryMap);

    @RequestLine("GET /vpn/vpn_number/{vpnNumber}")
    VpnOutputDto getVpnByVpnNumber(@Param("vpnNumber") String vpnNumber, @QueryMap Map<String, Object> queryMap);

    @RequestLine("GET /vpn/port_number/{portNumber}")
    List<VpnOutputDto> getVpnsByPortNumber(@Param("portNumber") String portNumber, @QueryMap Map<String, Object> queryMap);

    @RequestLine("GET /vpn/far_end_city_id/{farEndCityId}")
    List<VpnOutputDto> getVpnsByFarEndAddress(@Param("farEndCityId") Long farEndCityId, @QueryMap Map<String, Object> queryMap);

    @RequestLine("GET /vpn/near_end_city_id/{nearEndCityId}")
    List<VpnOutputDto> getVpnsByNearEndAddress(@Param("nearEndCityId") Long nearEndCityId, @QueryMap Map<String, Object> queryMap);

    @RequestLine("PUT /vpn/{id}")
    VpnOutputDto updateVpn(VpnInputDto vpnInputDto, @Param("id") Long id);

    @RequestLine("DELETE /vpn/{id}")
    void deleteVpn(@Param("id") Long id);

    @RequestLine("POST /port")
    PortOutputDto createNewPort(PortInputDto portDto);

    @RequestLine("GET /port/id/{id}")
    PortOutputDto getPortById(@Param("id") Long id, @QueryMap Map<String, Object> queryMap);

    @RequestLine("GET /port/port_number/{portNumber}")
    List<PortOutputDto> getPortsByPortNumber(@Param("portNumber") String portNumber, @QueryMap Map<String, Object> queryMap);

    @RequestLine("GET /port/city_id/{cityId}")
    List<PortOutputDto> getPortsByCityId(@Param("cityId") Long cityId, @QueryMap Map<String, Object> queryMap);

    @RequestLine("PUT /port/id/{id}")
    PortOutputDto updatePort(PortInputDto portDto, @Param("id") Long id);

    @RequestLine("DELETE /port/{id}")
    void deletePorts(@Param("id") Long id);

    @RequestLine("POST /adresses")
    AdressesOutputDto createNewAddress(AddressesInputDto inputDto);

    @RequestLine("PUT /addresses/id/{id}")
    AdressesOutputDto updateAddress(AddressesInputDto inputDto, @Param("id") Long id);
}
