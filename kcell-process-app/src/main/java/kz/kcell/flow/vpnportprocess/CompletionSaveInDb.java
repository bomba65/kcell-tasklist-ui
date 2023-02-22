package kz.kcell.flow.vpnportprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.PortOutputDto;
import kz.kcell.flow.vpnportprocess.mapper.VpnPortProcessMapper;
import kz.kcell.flow.vpnportprocess.service.IpVpnConnectService;
import kz.kcell.flow.vpnportprocess.variable.PortCamVar;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CompletionSaveInDb implements JavaDelegate {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final VpnPortClient vpnPortClient;
    private final VpnPortProcessMapper vpnPortProcessMapper;
    private final IpVpnConnectService ipVpnConnectService;

    @Override
    synchronized public void execute(DelegateExecution execution) throws Exception {
        String channel = execution.getVariable("channel").toString();
        String requestType = execution.getVariable("request_type").toString();

        if (requestType.equals("Organize") && channel.equals("Port")) {
            setAddedPortsStatusToActive(execution);
        } else if (requestType.equals("Disband") && channel.equals("Port")) {
            deleteDisbandedPorts(execution);
        } else if (requestType.equals("Modify") && channel.equals("Port")) {
            saveModifiedPorts(execution);
        } else if (requestType.equals("Organize") && channel.equals("VPN")) {
            setAddedServicesStatusToActive(execution);
        } else if (requestType.equals("Disband") && channel.equals("VPN")) {
            deleteDisbandedServices(execution);
        } else if (requestType.equals("Modify") && channel.equals("VPN")) {
            saveModifiedServices(execution);
        }
    }

    private void setAddedPortsStatusToActive(DelegateExecution execution) throws IOException {
        PortCamVar[] addedPorts = objectMapper.readValue(execution.getVariable("addedPorts").toString(), PortCamVar[].class);
        for(PortCamVar port: addedPorts) {
            vpnPortClient.updatePort(vpnPortProcessMapper.map(port, "Active"), port.getId());
        }
    }

    private void deleteDisbandedPorts(DelegateExecution execution) throws IOException {
        PortCamVar[] disbandPorts = objectMapper.readValue(execution.getVariable("disbandPorts").toString(), PortCamVar[].class);

        for (PortCamVar port : disbandPorts) {
            vpnPortClient.deletePorts(port.getId());
        }
    }

    private void saveModifiedPorts(DelegateExecution execution) throws IOException {
        PortCamVar[] disbandPorts = objectMapper.readValue(execution.getVariable("disbandPorts").toString(), PortCamVar[].class);

        for (PortCamVar port : disbandPorts) {
            PortOutputDto portOutputDto = vpnPortClient.updatePort(vpnPortProcessMapper.mapModifiedPort(port, "Active"), port.getId());
            ipVpnConnectService.changePortCapacity(portOutputDto);
        }
    }


    private void setAddedServicesStatusToActive(DelegateExecution execution) throws Exception {
        VpnCamVar[] addedServices = objectMapper.readValue(execution.getVariable("addedServices").toString(), VpnCamVar[].class);

        for(VpnCamVar vpn: addedServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromAddedVpn(vpn, vpn.getNearEndAddress().getId(), vpn.getVlan(), "Active"), vpn.getId());
            ipVpnConnectService.changeStatus(vpn.getVpnNumber(), "Active");
        }
    }

    private void deleteDisbandedServices(DelegateExecution execution) throws IOException {
        VpnCamVar[] disbandServices = objectMapper.readValue(execution.getVariable("disbandServices").toString(), VpnCamVar[].class);

        for (VpnCamVar vpn : disbandServices) {
            vpnPortClient.deleteVpn(vpn.getId());
            ipVpnConnectService.deleteVpn(vpn);
        }
    }

    private void saveModifiedServices(DelegateExecution execution) throws IOException {
        VpnCamVar[] modifyServices = objectMapper.readValue(execution.getVariable("modifyServices").toString(), VpnCamVar[].class);

        for (VpnCamVar vpn : modifyServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromModifiedVpn(vpn, "Active"), vpn.getId());
            ipVpnConnectService.changeStatusAndCapacity(vpn.getVpnNumber(), "Active", vpn.getServiceCapacity());
        }
    }
}
