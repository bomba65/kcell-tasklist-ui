package kz.kcell.flow.vpnportprocess.auto;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.vpnportprocess.mapper.VpnPortProcessMapper;
import kz.kcell.flow.vpnportprocess.service.IpVpnConnectService;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DeleteInDbRejectedByProvider implements JavaDelegate {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final VpnPortClient vpnPortClient;
    private final VpnPortProcessMapper vpnPortProcessMapper;
    private final IpVpnConnectService ipVpnConnectService;

    @Override
    synchronized public void execute(DelegateExecution execution) throws Exception {
        List<VpnCamVar> allServices = Arrays.stream(objectMapper.readValue(execution.getVariable("automodifyServices").toString(), VpnCamVar[].class))
            .filter(vpn -> !vpn.getProviderConfirmed()).collect(Collectors.toList());

        for (VpnCamVar vpn : allServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromVpn(vpn, "Active"), vpn.getId());
            ipVpnConnectService.changeStatusAndCapacityVpn(vpn.getPort().getPortNumber(), "Active", vpn.getServiceCapacity());
        }

    }
}
