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
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
        List<VpnCamVar> modifyServices = Arrays.stream(objectMapper.readValue(
            execution.getVariable("automodifyServices").toString(), VpnCamVar[].class
        ))
            .filter(VpnCamVar::getConfirmed)
            .collect(Collectors.toList());

        for (VpnCamVar vpn : modifyServices) {
            vpnPortClient.updateVpn(vpnPortProcessMapper.mapFromModifiedVpn(vpn, "In Process", vpn.getServiceCapacity()), vpn.getId());
            ipVpnConnectService.changeStatusAndCapacity(vpn.getVpnNumber(), "In Process", vpn.getModifiedServiceCapacity());
        }

        execution.setVariable("automodifyServices", SpinValues.jsonValue(objectMapper.writeValueAsString(modifyServices)));
    }
}
