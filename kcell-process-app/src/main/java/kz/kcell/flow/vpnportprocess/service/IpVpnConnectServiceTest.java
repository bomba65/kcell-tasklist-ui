package kz.kcell.flow.vpnportprocess.service;

import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.utils.Pair;
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
    public Pair<String, Integer> addNewVpnToIpVpnConnectFile(VpnOutputDto vpn, String serviceType, String vlan) {
        return new Pair<>("VPN", 0);
    }

    @Override
    public void changeStatus(String vpnNumber, String status) {}

    @Override
    public void changeAddedServiceStatus(VpnCamVar vpn, Pair<String, Integer> rowNumber, String status) {}

    @Override
    public void changeStatusAndCapacity(String vpnNumber, String status, Integer modifiedCapacity) {}

    @Override
    public void changeStatusAndCapacityVpn(String portNumber, String status, Integer modifiedCapacity) {}

    @Override
    public boolean checkUtilization(String vpnNumber, String serviceType) {
        return true;
    }

    @Override
    public void makeChangesToAddedService(VpnOutputDto vpn, Pair<String, Integer> rowNumber) {}

    @Override
    public void deleteAddedService(VpnCamVar vpn, Pair<String, Integer> rowNumber) {}

    public void deleteDisbandedVpn(String vpnNumber) {}

    @Override
    public Map<String,Double> findVpnNumbersThatMeetUtilizationCriteria() {
        return new HashMap<String,Double>() {{
            put("vpn1908", 0.83);
        }};
    }

    @Override
    public void changePortCapacity(String portNumber, String portCapacity, String status) {
    }
}
