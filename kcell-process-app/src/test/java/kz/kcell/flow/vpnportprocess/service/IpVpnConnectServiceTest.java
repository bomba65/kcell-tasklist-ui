package kz.kcell.flow.vpnportprocess.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kcell.flow.assets.dto.VpnOutputDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import static kz.kcell.Util.writeToFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IpVpnConnectServiceTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SambaServiceProd sambaService;

    @InjectMocks
    private IpVpnConnectService ipVpnConnectService;

    @Test
    public void testAddNewVlanToIpVpnConnectFile() {
        String serviceType = "IuB";
        InputStream in = getClass().getClassLoader().getResourceAsStream("vpn-port-process/files001/td_home/MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm");
        when(sambaService.readIpVpnConnect()).thenReturn(in);

        String vlan = ipVpnConnectService.addNewVlanToIpVpnConnectFile(serviceType);
        assertThat(vlan).isEqualTo("2599");

        ArgumentCaptor<ByteArrayOutputStream> argumentCaptor = ArgumentCaptor.forClass(ByteArrayOutputStream.class);
        verify(sambaService).writeIpVpnConnect(argumentCaptor.capture());
        ByteArrayOutputStream capturedOut = argumentCaptor.getValue();

        writeToFile(capturedOut, "./test-output/IPVPN CONNECT_new_vlan.xlsm");
    }

    @Test
    public void testAddNewVpnToIpVpnConnectFile() throws Exception {
        String serviceType = "IuB";
        InputStream in = getClass().getClassLoader().getResourceAsStream("vpn-port-process/files001/td_home/MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm");
        when(sambaService.readIpVpnConnect()).thenReturn(in);

        VpnOutputDto vpn = objectMapper.readValue(
            getClass().getClassLoader().getResourceAsStream("vpn-port-process/dto/vpn-output-dto.json"),
            VpnOutputDto.class
        );

        ipVpnConnectService.addNewVpnToIpVpnConnectFile(vpn, serviceType, "2599");

        ArgumentCaptor<ByteArrayOutputStream> argumentCaptor = ArgumentCaptor.forClass(ByteArrayOutputStream.class);
        verify(sambaService).writeIpVpnConnect(argumentCaptor.capture());
        ByteArrayOutputStream capturedOut = argumentCaptor.getValue();

        writeToFile(capturedOut, "./test-output/IPVPN CONNECT_new_vpn.xlsm");
    }


    @Test
    public void testChangeStatus() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("vpn-port-process/files001/td_home/MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm");
        when(sambaService.readIpVpnConnect()).thenReturn(in);

        ipVpnConnectService.changeStatusAndCapacity("VPN0484", "In Process", 150);

        ArgumentCaptor<ByteArrayOutputStream> argumentCaptor = ArgumentCaptor.forClass(ByteArrayOutputStream.class);
        verify(sambaService).writeIpVpnConnect(argumentCaptor.capture());
        ByteArrayOutputStream capturedOut = argumentCaptor.getValue();

        writeToFile(capturedOut, "./test-output/IPVPN CONNECT_changed_status.xlsm");
    }

    @Test
    public void testCheckUtilization() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("vpn-port-process/files001/IP VPN Statistics/IPVPN's 2023.01.04.xlsx");
        when(sambaService.readIpVpnStatistics()).thenReturn(in);

        boolean isAvailableForModification = ipVpnConnectService.checkUtilization("VPN0409", "ABIS");
        assertThat(isAvailableForModification).isTrue();
    }

    @Test
    public void testFindVpnNumbersThatMeetUtilizationCriteria() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("vpn-port-process/files001/IP VPN Statistics/IPVPN's 2023.01.04.xlsx");
        when(sambaService.readIpVpnStatistics()).thenReturn(in);

        Map<String, Double> result = ipVpnConnectService.findVpnNumbersThatMeetUtilizationCriteria();
        assertThat(result).hasSize(73);
    }

    @Test
    public void testChangePortCapacity() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("vpn-port-process/files001/td_home/MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm");
        when(sambaService.readIpVpnConnect()).thenReturn(in);

        ipVpnConnectService.changePortCapacity("АктауCE2", "40GB", "In Process");

        ArgumentCaptor<ByteArrayOutputStream> argumentCaptor = ArgumentCaptor.forClass(ByteArrayOutputStream.class);
        verify(sambaService).writeIpVpnConnect(argumentCaptor.capture());
        ByteArrayOutputStream capturedOut = argumentCaptor.getValue();

        writeToFile(capturedOut, "./test-output/IPVPN CONNECT_changed_port_capacity.xlsm");
    }
}
