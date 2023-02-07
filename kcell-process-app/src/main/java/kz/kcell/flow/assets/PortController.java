package kz.kcell.flow.assets;

import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.PortInputDto;
import kz.kcell.flow.assets.dto.PortOutputDto;
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
@RequestMapping("/port")
public class PortController {
    private final VpnPortClient vpnPortClient;

    @PostMapping
    public PortOutputDto createNewPort(@RequestBody PortInputDto portDto) {
        return vpnPortClient.createNewPort(portDto);
    }

    @GetMapping("/id/{id}")
    public PortOutputDto getPortById(@PathVariable Long id) {
        return vpnPortClient.getPortById(id);
    }

    @GetMapping("/port_number/{portNumber}")
    public List<PortOutputDto> getPortsByPortNumber(@PathVariable String portNumber) {
        return vpnPortClient.getPortsByPortNumber(portNumber);
    }

    @GetMapping("/city_id/{cityId}")
    public List<PortOutputDto> getPortsByCityId(@PathVariable Long cityId) {
        return vpnPortClient.getPortsByCityId(cityId);
    }

    @PutMapping("/id/{id}")
    public PortOutputDto updatePort(@RequestBody PortInputDto portDto, @PathVariable Long id) {
        return vpnPortClient.updatePort(portDto, id);
    }

    @DeleteMapping("/{id}")
    public void deletePorts(@PathVariable Long id) {
        vpnPortClient.deletePorts(id);
    }
}
