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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeleteInDbAuto implements JavaDelegate {

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final VpnPortClient vpnPortClient;
    private final VpnPortProcessMapper vpnPortProcessMapper;
    private final IpVpnConnectService ipVpnConnectService;

    @Override
    synchronized public void execute(DelegateExecution execution) throws Exception {
        revertVpnModification(execution);
    }

    private void revertVpnModification(DelegateExecution execution) throws IOException {
        VpnCamVar[] allServices=null;
        if (!(execution.getVariable("automodifyServices") == null)) {
             allServices = objectMapper.readValue(execution.getVariable("automodifyServices").toString(), VpnCamVar[].class);
        }
        if( allServices != null)
        for (VpnCamVar vpn : allServices) {
                vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromVpn(vpn, "Active"), vpn.getId());
                ipVpnConnectService.changeStatusAndCapacityVpn(vpn.getPort().getPortNumber(), "Active", vpn.getServiceCapacity());
            }
    }
}
