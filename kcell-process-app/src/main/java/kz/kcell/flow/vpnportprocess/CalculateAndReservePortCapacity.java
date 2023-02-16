package kz.kcell.flow.vpnportprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.PortOutputDto;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.vpnportprocess.mapper.VpnPortProcessMapper;
import kz.kcell.flow.vpnportprocess.service.IpVpnConnectService;
import kz.kcell.flow.vpnportprocess.variable.PortCamVar;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CalculateAndReservePortCapacity implements JavaDelegate {

    private final VpnPortClient vpnPortClient;
    private final VpnPortProcessMapper vpnPortProcessMapper;
    private final IpVpnConnectService ipVpnConnectService;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    synchronized public void execute(DelegateExecution execution) throws Exception {
        String channel = execution.getVariable("channel").toString();
        String requestType = execution.getVariable("request_type").toString();

        if (requestType.equals("Organize") && channel.equals("Port")) {
            organizePort(execution);
        } else if (requestType.equals("Disband") && channel.equals("Port")) {
            disbandPort(execution);
        } else if (requestType.equals("Modify") && channel.equals("Port")) {
            modifyPort(execution);
        } else if (requestType.equals("Organize") && channel.equals("VPN")) {
            organizeVpn(execution);
        } else if (requestType.equals("Disband") && channel.equals("VPN")) {
            disbandVpn(execution);
        } else if (requestType.equals("Modify") && channel.equals("VPN")) {
            modifyVpn(execution);
        }
    }

    private void organizePort(DelegateExecution execution) throws IOException {
        PortCamVar[] addedPorts = objectMapper.readValue(execution.getVariable("addedPorts").toString(), PortCamVar[].class);

        List<PortOutputDto> createdPorts = Arrays.stream(addedPorts).map((port) -> {
            long addressId = vpnPortClient.createNewAddress(vpnPortProcessMapper.map(port.getFarEndAddress())).getId();
            return vpnPortClient.createNewPort(vpnPortProcessMapper.map(port, addressId, "Ordered"));
        }).collect(Collectors.toList());

        execution.setVariable("addedPorts", SpinValues.jsonValue(objectMapper.writeValueAsString(createdPorts)).create());
    }

    private void disbandPort(DelegateExecution execution) throws IOException {
        PortCamVar[] disbandPorts = objectMapper.readValue(execution.getVariable("disbandPorts").toString(), PortCamVar[].class);

        for (PortCamVar port : disbandPorts) {
            try {
                vpnPortClient.getVpnsByPortNumber(port.getPortNumber());
                execution.setVariable("calculateAndReservePortCapacityReject", "there are existed vpns");
                return;
            } catch (FeignException exception) {
                if (exception.status() != 404) {
                    throw exception;
                }
            }
        }

        for (PortCamVar port : disbandPorts) {
            vpnPortClient.updatePort(vpnPortProcessMapper.map(port, "In Process"), port.getId());
        }

    }

    private void modifyPort(DelegateExecution execution) throws IOException {
        PortCamVar[] modifyPorts = objectMapper.readValue(execution.getVariable("modifyPorts").toString(), PortCamVar[].class);

        for (PortCamVar port : modifyPorts) {
            long totalServiceCapacity;
            try {
                totalServiceCapacity = vpnPortClient.getVpnsByPortNumber(port.getPortNumber()).stream().mapToLong(VpnOutputDto::getServiceCapacity).sum();
            } catch (FeignException exception) {
                if (exception.status() == 404) {
                    totalServiceCapacity = 0;
                } else {
                    throw exception;
                }
            }
            long portCapacity = port.getPortCapacityUnit().equals("Gb") ? port.getPortCapacity() * 1000L : port.getPortCapacity();
            long modifiedPortCapacity = port.getModifiedPortCapacityUnit().equals("Gb") ? port.getModifiedPortCapacity() * 1000L : port.getModifiedPortCapacity();

            if (modifiedPortCapacity < portCapacity && totalServiceCapacity * 100 / modifiedPortCapacity >= 90) {
                execution.setVariable("calculateAndReservePortCapacityReject", "modified port capacity is not enough");
                return;
            }
        }

        for (PortCamVar port : modifyPorts) {
            vpnPortClient.updatePort(vpnPortProcessMapper.map(port, "In Process"), port.getId());
        }
    }

    private void organizeVpn(DelegateExecution execution) throws Exception {
        VpnCamVar[] addedServices = objectMapper.readValue(execution.getVariable("addedServices").toString(), VpnCamVar[].class);

        // group newly added vpns by port
        Map<PortCamVar, List<VpnCamVar>> portToVpns = Arrays.stream(addedServices).collect(Collectors.groupingBy(VpnCamVar::getPort));

        // check if port capacity is enough
        for (Map.Entry<PortCamVar, List<VpnCamVar>> entry : portToVpns.entrySet()) {
            PortCamVar port = entry.getKey();
            long totalServiceCapacity;
            try {
                totalServiceCapacity = vpnPortClient.getVpnsByPortNumber(port.getPortNumber()).stream().mapToLong(VpnOutputDto::getServiceCapacity).sum();
            } catch (FeignException exception) {
                if (exception.status() == 404) {
                    totalServiceCapacity = 0;
                } else {
                    throw exception;
                }
            }

            long portCapacity = port.getPortCapacityUnit().equals("Gb") ? port.getPortCapacity() * 1000L : port.getPortCapacity();

            long totalAddedServiceCapacity = entry.getValue().stream().mapToLong(VpnCamVar::getServiceCapacity).sum();

            if ((totalServiceCapacity + totalAddedServiceCapacity) * 100 / portCapacity >= 90) {
                execution.setVariable("calculateAndReservePortCapacityReject", "port capacity is not enough for new service");
                return;
            }
        }

        List<VpnOutputDto> createdVpns = Arrays.stream(addedServices).map((vpn) -> {
            long addressId = vpnPortClient.createNewAddress(vpnPortProcessMapper.map(vpn.getNearEndAddress())).getId();
            String vlan = null;
            // write to IPVPN CONNECT.xlsm to VLAN sheet and get a vlan value for the newly created vpn
            if (vpn.getService().equals("L2")) {
                vlan = ipVpnConnectService.addNewVlanToIpVpnConnectFile(vpn.getServiceTypeTitle());
            }
            // post the vpn to assets
            VpnOutputDto createdVpn = vpnPortClient.createNewVpn(vpnPortProcessMapper.mapFromAddedVpn(vpn, addressId, vlan, "Ordered"));

            // write the created vpn to IPVPN CONNECT.xlsm
            ipVpnConnectService.addNewVpnToIpVpnConnectFile(createdVpn, vpn.getServiceTypeTitle(), vlan);
            return createdVpn;
        }).collect(Collectors.toList());

        execution.setVariable("addedServices", SpinValues.jsonValue(objectMapper.writeValueAsString(createdVpns)).create());
    }

    private void disbandVpn(DelegateExecution execution) throws IOException {
        VpnCamVar[] disbandServices = objectMapper.readValue(execution.getVariable("disbandServices").toString(), VpnCamVar[].class);

        for (VpnCamVar vpn : disbandServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromDisbandedVpn(vpn, "In Process"), vpn.getId());
            ipVpnConnectService.changeStatus(vpn.getVpnNumber(), "In Process");
        }
    }

    private void modifyVpn(DelegateExecution execution) throws IOException {
        VpnCamVar[] modifyServices = objectMapper.readValue(execution.getVariable("modifyServices").toString(), VpnCamVar[].class);

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
                execution.setVariable("calculateAndReservePortCapacityReject", "port capacity is not enough for modified service");
                return;
            }
        }

        for (VpnCamVar vpn : modifyServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromModifiedVpn(vpn, "In Process"), vpn.getId());
            ipVpnConnectService.changeStatusAndCapacity(vpn.getVpnNumber(), "In Process", vpn.getModifiedServiceCapacity());
        }
    }
}
