package kz.kcell.flow.vpnportprocess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.utils.Pair;
import kz.kcell.flow.vpnportprocess.mapper.VpnPortProcessMapper;
import kz.kcell.flow.vpnportprocess.service.IpVpnConnectService;
import kz.kcell.flow.vpnportprocess.variable.PortCamVar;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeleteInDb implements JavaDelegate {

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final VpnPortClient vpnPortClient;
    private final VpnPortProcessMapper vpnPortProcessMapper;
    private final IpVpnConnectService ipVpnConnectService;

    @Override
    synchronized public void execute(DelegateExecution execution) throws Exception {
        String channel = execution.getVariable("channel").toString();
        String requestType = execution.getVariable("request_type").toString();

        if (requestType.equals("Organize") && channel.equals("Port")) {
            deleteAddedPorts(execution);
        } else if (requestType.equals("Disband") && channel.equals("Port")) {
            revertPortDisbandment(execution);
        } else if (requestType.equals("Modify") && channel.equals("Port")) {
            revertPortDisbandment(execution);
        } else if (requestType.equals("Organize") && channel.equals("VPN")) {
            deleteAddedServices(execution);
        } else if (requestType.equals("Disband") && channel.equals("VPN")) {
            revertVpnDisbandment(execution);
        } else if (requestType.equals("Modify") && channel.equals("VPN")) {
            revertVpnModification(execution);
        }
    }

    private void deleteAddedPorts(DelegateExecution execution) throws IOException {
        PortCamVar[] addedPorts = objectMapper.readValue(execution.getVariable("addedPorts").toString(), PortCamVar[].class);
        for(PortCamVar port: addedPorts) {
            vpnPortClient.deletePorts(port.getId());
        }
    }

    private void revertPortDisbandment(DelegateExecution execution) throws IOException {
        PortCamVar[] disbandPorts = objectMapper.readValue(execution.getVariable("disbandPorts").toString(), PortCamVar[].class);

        for (PortCamVar port : disbandPorts) {
            vpnPortClient.updatePort(vpnPortProcessMapper.map(port, "Active"), port.getId());
        }

    }

    private void deleteAddedServices(DelegateExecution execution) throws Exception {
        VpnCamVar[] addedServices = objectMapper.readValue(execution.getVariable("addedServices").toString(), VpnCamVar[].class);
        List<Pair<String,Integer>> addedIpVpnRowNumbers = objectMapper.readValue(execution.getVariable("addedIpVpnRowNumbers").toString(),  new TypeReference<List<Pair<String,Integer>>>(){});

        for(int i = 0; i < addedServices.length; i++) {
            vpnPortClient.deleteVpn(addedServices[i].getId());
            ipVpnConnectService.deleteAddedService(addedServices[i], addedIpVpnRowNumbers.get(i));
        }
    }

    private void revertVpnDisbandment(DelegateExecution execution) throws IOException {
        VpnCamVar[] disbandServices = objectMapper.readValue(execution.getVariable("disbandServices").toString(), VpnCamVar[].class);

        for (VpnCamVar vpn : disbandServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromDisbandedVpn(vpn, "Active"), vpn.getId());
            ipVpnConnectService.changeStatus(vpn.getVpnNumber(), "Active");
        }
    }

    private void revertVpnModification(DelegateExecution execution) throws IOException {
        VpnCamVar[] modifyServices = objectMapper.readValue(execution.getVariable("modifyServices").toString(), VpnCamVar[].class);

        for (VpnCamVar vpn : modifyServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromModifiedVpn(vpn, "Active"), vpn.getId());
            ipVpnConnectService.changeStatusAndCapacity(vpn.getVpnNumber(), "Active", vpn.getServiceCapacity());
        }
    }
}
