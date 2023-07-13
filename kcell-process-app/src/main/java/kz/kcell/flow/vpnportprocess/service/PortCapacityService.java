package kz.kcell.flow.vpnportprocess.service;

import feign.FeignException;
import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.vpnportprocess.variable.PortCamVar;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortCapacityService {
    private final VpnPortClient vpnPortClient;


    public boolean portCapacityEnoughForVpns(List<VpnCamVar> services) {
        // group vpns to modify by port
        Map<PortCamVar, List<VpnCamVar>> portToVpns = services.stream().collect(Collectors.groupingBy(VpnCamVar::getPort));

        for (Map.Entry<PortCamVar, List<VpnCamVar>> entry : portToVpns.entrySet()) {
            PortCamVar port = entry.getKey();
            long totalServiceCapacityOnPort;
            try {
                totalServiceCapacityOnPort = vpnPortClient.getVpnsByPortNumber(port.getPortNumber(), new HashMap<String, Object>() {{
                        put("status", "Active");
                    }})
                    .stream().mapToLong(VpnOutputDto::getServiceCapacity).sum();
            } catch (FeignException exception) {
                if (exception.status() == 404) {
                    totalServiceCapacityOnPort = 0;
                } else {
                    throw exception;
                }
            }

            long portCapacity = port.getPortCapacityUnit().equals("Gb") ? port.getPortCapacity() * 1000L : port.getPortCapacity();

            long totalExistingServiceCapacity = entry.getValue().stream().mapToLong(VpnCamVar::getServiceCapacity).sum();

            Long totalModifiedServiceCapacity = 0L;
            for (VpnCamVar vpn : entry.getValue()) {
                if (vpn.getModifiedServiceCapacity() == null) {
                    totalModifiedServiceCapacity = null;
                    break;
                } else {
                    totalModifiedServiceCapacity+=vpn.getModifiedServiceCapacity();
                }
            }

            long capacityDiff = 0;
            if (totalModifiedServiceCapacity != null) {
                capacityDiff = totalModifiedServiceCapacity - totalExistingServiceCapacity;
            } else {
                capacityDiff = totalExistingServiceCapacity;
            }

            if ((totalServiceCapacityOnPort + capacityDiff) > portCapacity) {
                return false;
            }
        }
        return true;
    }

    public boolean portCapacityEnough(List<PortCamVar> ports) {
        for (PortCamVar port : ports) {
            long totalServiceCapacity;
            try {
                totalServiceCapacity = vpnPortClient.getVpnsByPortNumber(port.getPortNumber(), new HashMap<String,Object>(){{put("status","Active");}})
                    .stream().mapToLong(VpnOutputDto::getServiceCapacity).sum();
            } catch (FeignException exception) {
                if (exception.status() == 404) {
                    totalServiceCapacity = 0;
                } else {
                    throw exception;
                }
            }
            long portCapacity = port.getPortCapacityUnit().equals("Gb") ? port.getPortCapacity() * 1000L : port.getPortCapacity();
            long modifiedPortCapacity = port.getModifiedPortCapacityUnit().equals("Gb") ? port.getModifiedPortCapacity() * 1000L : port.getModifiedPortCapacity();

            if (modifiedPortCapacity < portCapacity && totalServiceCapacity > modifiedPortCapacity) {
                return false;
            }
        }
        return true;
    }
}
