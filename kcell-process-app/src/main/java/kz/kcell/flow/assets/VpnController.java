package kz.kcell.flow.assets;

import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.VpnInputDto;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/vpn")
public class VpnController {
    private final VpnPortClient vpnPortClient;


    @PostMapping
    public VpnOutputDto createNewVpn(@RequestBody VpnInputDto vpnInputDto) {
        return vpnPortClient.createNewVpn(vpnInputDto);
    }

    @GetMapping("/{id}")
    public VpnOutputDto getVpnById(@PathVariable Long id) {
        return vpnPortClient.getVpnById(id);
    }

    @GetMapping("/vpn_number/{vpnNumber}")
    public VpnOutputDto getVpnByVpnNumber(@PathVariable String vpnNumber) {
        return vpnPortClient.getVpnByVpnNumber(vpnNumber);
    }

    @GetMapping("/port_number/{portNumber}")
    public List<VpnOutputDto> getVpnsByPortNumber(@PathVariable String portNumber) {
        return vpnPortClient.getVpnsByPortNumber(portNumber);
    }

    @GetMapping("/far_end_city_id/{farEndCityId}")
    public List<VpnOutputDto> getVpnsByFarEndAddress(@PathVariable Long farEndCityId) {
        return vpnPortClient.getVpnsByFarEndAddress(farEndCityId);
    }

    @GetMapping("/near_end_city_id/{nearEndCityId}")
    public List<VpnOutputDto> getVpnsByNearEndAddress(@PathVariable Long nearEndCityId) {
        return vpnPortClient.getVpnsByNearEndAddress(nearEndCityId);
    }

    @PutMapping("/{id}")
    public VpnOutputDto updateVpn(@RequestBody VpnInputDto vpnInputDto, @PathVariable Long id) {
        return vpnPortClient.updateVpn(vpnInputDto, id);
    }

    @DeleteMapping("/{id}")
    public void deleteVpn(@PathVariable Long id) {
        vpnPortClient.deleteVpn(id);
    }
}
