package kz.kcell.flow.vpnportprocess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.PortOutputDto;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.utils.Pair;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            try {
                PortOutputDto existingPort = vpnPortClient.getPortsByPortNumber(port.getPortNumber(), new HashMap<String,Object>(){{put("status","Ordered");}}).get(0);
                vpnPortClient.updateAddress(vpnPortProcessMapper.map(port.getFarEndAddress()), existingPort.getFarEndAddress().getId());
                return vpnPortClient.updatePort(vpnPortProcessMapper.map(port, existingPort.getFarEndAddress().getId(), "Ordered"), existingPort.getId());
            } catch (FeignException exception) {
                if (exception.status() == 404) {
                    long addressId = vpnPortClient.createNewAddress(vpnPortProcessMapper.map(port.getFarEndAddress())).getId();
                    return vpnPortClient.createNewPort(vpnPortProcessMapper.map(port, addressId, "Ordered"));
                } else {
                    throw exception;
                }
            }

        }).collect(Collectors.toList());

        execution.setVariable("addedPorts", SpinValues.jsonValue(objectMapper.writeValueAsString(createdPorts)).create());
    }

    private void disbandPort(DelegateExecution execution) throws IOException {
        PortCamVar[] disbandPorts = objectMapper.readValue(execution.getVariable("disbandPorts").toString(), PortCamVar[].class);

        for (PortCamVar port : disbandPorts) {
            try {
                vpnPortClient.getVpnsByPortNumber(port.getPortNumber(), new HashMap<String,Object>(){{put("status","Active");}});
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

            long totalAddedServiceCapacity = entry.getValue().stream().mapToLong(VpnCamVar::getServiceCapacity).sum();

            if ((totalServiceCapacity + totalAddedServiceCapacity) * 100 / portCapacity >= 90) {
                execution.setVariable("calculateAndReservePortCapacityReject", "port capacity is not enough for new service");
                return;
            }
        }

        VpnCamVar[] preModifiedAddedServices = execution.getVariable("preModifiedAddedServices") != null ?
            objectMapper.readValue(execution.getVariable("preModifiedAddedServices").toString(), VpnCamVar[].class) :
            null;
        List<Pair<String,Integer>> preModifiedAddedIpVpnRowNumbers = execution.getVariable("addedIpVpnRowNumbers") != null ?
            objectMapper.readValue(execution.getVariable("addedIpVpnRowNumbers").toString(),  new TypeReference<List<Pair<String,Integer>>>(){}) :
            null;
        if (preModifiedAddedServices != null && preModifiedAddedIpVpnRowNumbers != null) {
            for (int i = 0; i < preModifiedAddedServices.length; i++) {
                vpnPortClient.deleteVpn(preModifiedAddedServices[i].getId());
                ipVpnConnectService.deleteAddedService(preModifiedAddedServices[i], preModifiedAddedIpVpnRowNumbers.get(i));
            }
        }

        List<Pair<String,Integer>> addedIpVpnRowNumbers = new ArrayList<>();
        List<VpnOutputDto> createdVpns = Arrays.stream(addedServices).map(vpn -> {
            long addressId = vpnPortClient.createNewAddress(vpnPortProcessMapper.map(vpn.getNearEndAddress())).getId();
            String vlan = null;
            // write to IPVPN CONNECT.xlsm to VLAN sheet and get a vlan value for the newly created vpn
            if (vpn.getService().equals("L2")) {
                vlan = ipVpnConnectService.addNewVlanToIpVpnConnectFile(vpn.getServiceTypeTitle());
            }
            // post the vpn to assets
            VpnOutputDto createdVpn = vpnPortClient.createNewVpn(vpnPortProcessMapper.mapFromAddedVpn(vpn, addressId, vlan, "Ordered"));

            // write the created vpn to IPVPN CONNECT.xlsm
            Pair<String, Integer> sheetAndRowNumber = ipVpnConnectService.addNewVpnToIpVpnConnectFile(createdVpn, vpn.getServiceTypeTitle(), vlan);
            addedIpVpnRowNumbers.add(sheetAndRowNumber);
            return createdVpn;
        }).collect(Collectors.toList());
        List<Integer> createdVpnHashCodeList = createdVpns.stream().map(VpnOutputDto::hashCode).collect(Collectors.toList());

        execution.setVariable("addedServiceIdList", SpinValues.jsonValue(objectMapper.writeValueAsString(createdVpns.stream().map(VpnOutputDto::getId).collect(Collectors.toList()))));
        execution.setVariable("addedServiceHashCodeList", SpinValues.jsonValue(objectMapper.writeValueAsString(createdVpnHashCodeList)));
        execution.setVariable("addedIpVpnRowNumbers", SpinValues.jsonValue(objectMapper.writeValueAsString(addedIpVpnRowNumbers)));
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
                totalServiceCapacityOnPort = vpnPortClient.getVpnsByPortNumber(port.getPortNumber(), new HashMap<String,Object>(){{put("status","Active");}})
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
            long totalModifiedServiceCapacity = entry.getValue().stream().mapToLong(VpnCamVar::getModifiedServiceCapacity).sum();
            long capacityDiff = totalModifiedServiceCapacity - totalExistingServiceCapacity;

            if ((totalServiceCapacityOnPort + capacityDiff) * 100 / portCapacity >= 90) {
                execution.setVariable("calculateAndReservePortCapacityReject", "port capacity is not enough for modified service");
                return;
            }
        }

        for (VpnCamVar vpn : modifyServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromModifiedVpn(vpn, "In Process", vpn.getServiceCapacity()), vpn.getId());
            ipVpnConnectService.changeStatusAndCapacity(vpn.getVpnNumber(), "In Process", vpn.getModifiedServiceCapacity());
        }
    }
}
