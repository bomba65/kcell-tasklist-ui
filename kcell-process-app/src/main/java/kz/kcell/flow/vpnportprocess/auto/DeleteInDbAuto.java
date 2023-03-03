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

    private void revertVpnModification(DelegateExecution execution) throws Exception {
        VpnCamVar[] modifyServices = null;
        if (!(execution.getVariable("modifyServices") == null))
            modifyServices = objectMapper.readValue(execution.getVariable("modifyServices").toString(), VpnCamVar[].class);
        VpnCamVar[] disbandServices = null;
        if (!(execution.getVariable("disbandServices") == null))
            disbandServices = objectMapper.readValue(execution.getVariable("disbandServices").toString(), VpnCamVar[].class);
        VpnCamVar[] addedServices = null;
        if (!(execution.getVariable("addedServices") == null))
            addedServices = objectMapper.readValue(execution.getVariable("addedServices").toString(), VpnCamVar[].class);
        List<VpnCamVar> allServices = new ArrayList<>();
        if (modifyServices != null) allServices.addAll(Arrays.asList(modifyServices));
        if (disbandServices != null) allServices.addAll(Arrays.asList(disbandServices));
        if (addedServices != null) allServices.addAll(Arrays.asList(addedServices));
        for (VpnCamVar vpn : allServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromVpn(vpn, "Active"), vpn.getId());
            ipVpnConnectService.changeStatusAndCapacity(vpn.getVpnNumber(), "Active", vpn.getServiceCapacity());
        }
    }
}
