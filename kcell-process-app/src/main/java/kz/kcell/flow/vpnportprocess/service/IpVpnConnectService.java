package kz.kcell.flow.vpnportprocess.service;

import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.utils.Pair;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;

import java.util.Map;

public interface IpVpnConnectService {
    String addNewVlanToIpVpnConnectFile(String serviceType);

    Pair<String, Integer> addNewVpnToIpVpnConnectFile(VpnOutputDto vpn, String serviceType, String vlan);

    void changeStatus(String vpnNumber, String status);

    void changeAddedServiceStatus(VpnCamVar vpn, Pair<String, Integer> rowNumber, String status);

    void changeStatusAndCapacity(String vpnNumber, String status, Integer modifiedCapacity);
    void changeStatusAndCapacityVpn(String portNumber, String status, Integer modifiedCapacity);
    boolean checkUtilization(String vpnNumber, String serviceType);

    void makeChangesToAddedService(VpnOutputDto vpn, Pair<String, Integer> rowNumber);

    void deleteAddedService(VpnCamVar vpn, Pair<String, Integer> rowNumber);

    void deleteDisbandedVpn(String vpnNumber);

    Map<String, Double> findVpnNumbersThatMeetUtilizationCriteria();

    void changePortCapacity(String portNumber, String portCapacity, String status);
}
