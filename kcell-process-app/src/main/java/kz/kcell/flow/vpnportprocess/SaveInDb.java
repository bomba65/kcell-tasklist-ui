package kz.kcell.flow.vpnportprocess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kcell.flow.assets.client.VpnPortClient;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.utils.Pair;
import kz.kcell.flow.vpnportprocess.mapper.VpnPortProcessMapper;
import kz.kcell.flow.vpnportprocess.service.IpVpnConnectService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

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
            List<Pair<String,Integer>> addedIpVpnRowNumbers = objectMapper.readValue(execution.getVariable("addedIpVpnRowNumbers").toString(),  new TypeReference<List<Pair<String,Integer>>>(){});
            for(int i = 0; i < vpns.length; i++) {
                VpnOutputDto vpn = vpns[i];
                if (vpnHashCodeList[i] != vpn.hashCode()) {
                    vpnPortClient.updateVpn(mapper.mapFromVpnOutputDto(vpn), vpn.getId());
                    if (addedIpVpnRowNumbers.size() > 0) {
                        ipVpnConnectService.makeChangesToAddedService(vpn, addedIpVpnRowNumbers.get(i));
                    }
                }
            }
        }
    }
}
