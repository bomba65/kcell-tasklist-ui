package kz.kcell.flow.assets;

import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.PortInputDto;
import kz.kcell.flow.assets.dto.PortOutputDto;
import kz.kcell.flow.vpnportprocess.service.PortCapacityService;
import kz.kcell.flow.vpnportprocess.variable.PortCamVar;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/port")
public class PortController {
    private final VpnPortClient vpnPortClient;
    private final PortCapacityService portCapacityService;

    @PostMapping
    public PortOutputDto createNewPort(@RequestBody PortInputDto portDto) {
        return vpnPortClient.createNewPort(portDto);
    }

    @GetMapping("/id/{id}")
    public PortOutputDto getPortById(@PathVariable Long id, @RequestParam(required=false) String status) {
        if (status == null) {
            return vpnPortClient.getPortById(id, new HashMap<>());
        } else {
            return vpnPortClient.getPortById(id, new HashMap<String, Object>() {{
                put("status", status);
            }});
        }

    }

    @GetMapping("/port_number/{portNumber}")
    public List<PortOutputDto> getPortsByPortNumber(@PathVariable String portNumber, @RequestParam(required=false) String status) {
        if (status == null) {
            return vpnPortClient.getPortsByPortNumber(portNumber, new HashMap<>());
        } else {
            return vpnPortClient.getPortsByPortNumber(portNumber, new HashMap<String, Object>() {{
                put("status", status);
            }});
        }
    }

    @GetMapping("/city_id/{cityId}")
    public List<PortOutputDto> getPortsByCityId(@PathVariable Long cityId, @RequestParam(required=false) String status) {
        if (status == null) {
            return vpnPortClient.getPortsByCityId(cityId, new HashMap<>());
        } else {
            return vpnPortClient.getPortsByCityId(cityId, new HashMap<String, Object>() {{
                put("status", status);
            }});
        }
    }

    @PutMapping("/id/{id}")
    public PortOutputDto updatePort(@RequestBody PortInputDto portDto, @PathVariable Long id) {
        return vpnPortClient.updatePort(portDto, id);
    }

    @DeleteMapping("/{id}")
    public void deletePorts(@PathVariable Long id) {
        vpnPortClient.deletePorts(id);
    }

    @PostMapping("/port-capacity-enough")
    public ResponseEntity<Boolean> checkPortCapacity(@RequestBody List<PortCamVar> ports) {
        return ResponseEntity.ok(portCapacityService.portCapacityEnough(ports));
    }
}
