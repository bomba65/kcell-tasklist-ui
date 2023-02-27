package kz.kcell.flow.vpnportprocess.service;

import kz.kcell.flow.assets.dto.PortOutputDto;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name="ipvpn.connect.file.enabled", havingValue = "false")
public class IpVpnConnectServiceTest implements IpVpnConnectService {

    @Override
    public String addNewVlanToIpVpnConnectFile(String serviceType) {
        return "";
    }

    @Override
    public void addNewVpnToIpVpnConnectFile(VpnOutputDto vpn, String serviceType, String vlan) {}

    @Override
    public void changeStatus(String vpnNumber, String status) {}

    @Override
    public void changeStatusAndCapacity(String vpnNumber, String status, Integer modifiedCapacity) {}

    @Override
    public boolean checkUtilization(String vpnNumber, String serviceType) {
        return true;
    }

    @Override
    public void makeChangesToAddedService(VpnOutputDto vpn) {}

    @Override
    public void deleteVpn(VpnCamVar vpn) {}

    @Override
    public Map<String,Double> findVpnNumbersThatMeetUtilizationCriteria() {
        return new HashMap<String,Double>() {{
            put("vpn1908", 0.83);
        }};
    }

    @Override
    public void changePortCapacity(PortOutputDto port) {
    }
}
