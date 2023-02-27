package kz.kcell.flow.vpnportprocess.service;

import kz.kcell.flow.assets.dto.PortOutputDto;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;

import java.util.Map;

public interface IpVpnConnectService {
    String addNewVlanToIpVpnConnectFile(String serviceType);

    void addNewVpnToIpVpnConnectFile(VpnOutputDto vpn, String serviceType, String vlan);

    void changeStatus(String vpnNumber, String status);

    void changeStatusAndCapacity(String vpnNumber, String status, Integer modifiedCapacity);

    boolean checkUtilization(String vpnNumber, String serviceType);

    void makeChangesToAddedService(VpnOutputDto vpn);

    void deleteVpn(VpnCamVar vpn);

    Map<String, Double> findVpnNumbersThatMeetUtilizationCriteria();

    void changePortCapacity(PortOutputDto port);
}
