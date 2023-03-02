package kz.kcell.flow.vpnportprocess.auto;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.vpnportprocess.mapper.VpnPortProcessMapper;
import kz.kcell.flow.vpnportprocess.service.IpVpnConnectService;
import kz.kcell.flow.vpnportprocess.variable.PortCamVar;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CalculateAndReservePortCapacityAuto implements JavaDelegate {

    private final VpnPortClient vpnPortClient;
    private final VpnPortProcessMapper vpnPortProcessMapper;
    private final IpVpnConnectService ipVpnConnectService;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    synchronized public void execute(DelegateExecution execution) throws Exception {
        VpnCamVar[] modifyServices = objectMapper.readValue(execution.getVariable("automodifyServices").toString(), VpnCamVar[].class);

        // group vpns to modify by port
        Map<PortCamVar, List<VpnCamVar>> portToVpns = Arrays.stream(modifyServices).collect(Collectors.groupingBy(VpnCamVar::getPort));

        for (Map.Entry<PortCamVar, List<VpnCamVar>> entry : portToVpns.entrySet()) {
            PortCamVar port = entry.getKey();
            long totalServiceCapacityOnPort;
            try {
                totalServiceCapacityOnPort = vpnPortClient.getVpnsByPortNumber(port.getPortNumber()).stream().mapToLong(VpnOutputDto::getServiceCapacity).sum();
            } catch (FeignException exception) {
                if (exception.status() == 404) {
                    totalServiceCapacityOnPort = 0;
                } else {
                    throw exception;
                }
            }

            long portCapacity = port.getPortCapacityUnit().equals("Gb") ? port.getPortCapacity() * 1000L : port.getPortCapacity();

            long totalExistingServiceCapacity = entry.getValue().stream().mapToLong(VpnCamVar::getServiceCapacity).sum();
            long totalModifiedServiceCapacity = entry.getValue().stream().mapToLong(VpnCamVar::getModifiedServiceCapacity).sum();
            long capacityDiff = totalModifiedServiceCapacity - totalExistingServiceCapacity;

            if ((totalServiceCapacityOnPort + capacityDiff) * 100 / portCapacity >= 90) {
                execution.setVariable("calculateAndReservePortCapacityReject", "port capacity is not enough for automodified service");
                return;
            }
        }

        for (VpnCamVar vpn : modifyServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromModifiedVpn(vpn, "In Process"), vpn.getId());
            ipVpnConnectService.changeStatusAndCapacity(vpn.getVpnNumber(), "In Process", vpn.getModifiedServiceCapacity());
        }
    }
}
