package kz.kcell.flow.vpnportprocess;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.vpnportprocess.mapper.VpnPortProcessMapper;
import kz.kcell.flow.vpnportprocess.service.IpVpnConnectService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class SaveInDb implements JavaDelegate {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final VpnPortClient vpnPortClient;
    private final VpnPortProcessMapper mapper;
    private final IpVpnConnectService ipVpnConnectService;

    @Override
    synchronized public void execute(DelegateExecution execution) throws Exception {
        String channel = execution.getVariable("channel").toString();
        String requestType = execution.getVariable("request_type").toString();

        if (requestType.equals("Organize") && channel.equals("VPN")) {
            VpnOutputDto[] vpns = objectMapper.readValue(execution.getVariable("addedServices").toString(), VpnOutputDto[].class);
            Integer[] vpnHashCodeList = objectMapper.readValue(execution.getVariable("addedServiceHashCodeList").toString(), Integer[].class);
            for(int i = 0; i < vpns.length; i++) {
                VpnOutputDto vpn = vpns[i];
                if (vpnHashCodeList[i] != vpn.hashCode()) {
                    vpnPortClient.updateVpn(mapper.mapFromVpnOutputDto(vpn), vpn.getId());
                    ipVpnConnectService.makeChangesToAddedService(vpn);
                }
            }
        }
    }
}
