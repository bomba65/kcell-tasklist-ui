package kz.kcell.flow.vpnportprocess.service;

import kz.kcell.flow.assets.dto.VpnOutputDto;

public interface IpVpnConnectService {
    String addNewVlanToIpVpnConnectFile(String serviceType);

    void addNewVpnToIpVpnConnectFile(VpnOutputDto vpn, String serviceType, String vlan);

    void changeStatus(String vpnNumber, String status);

    void changeStatusAndCapacity(String vpnNumber, String status, Integer modifiedCapacity);

    boolean checkUtilization(String vpnNumber, String serviceType);
}
