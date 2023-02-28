package kz.kcell.flow.controller;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/test")
@ConditionalOnProperty(name = "test.controller.enabled", havingValue = "true")
public class TestController {

    @Value("${ipvpn.samba.url}")
    private String sambaUrl;

    @GetMapping("/ip_vpn_connect")
    public ResponseEntity<Resource> getIpVpnConnectFile() throws IOException {
        String filePath = "/files001/td_home/MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm";
        ByteArrayResource fileResource = new ByteArrayResource(Files.readAllBytes(Paths.get(filePath)));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"IPVPN CONNECT.xlsm\"");

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType("application/vnd.ms-excel.sheet.macroEnabled.12"))
            .body(fileResource);
    }

    @GetMapping("/ip_vpn_statistics")
    public ResponseEntity<Resource> getIpVpnStatisticsFile() throws IOException {
        String filePath = "/files001/IP VPN Statistics/IPVPN's 2023.01.04.xlsx";
        ByteArrayResource fileResource = new ByteArrayResource(Files.readAllBytes(Paths.get(filePath)));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"IPVPN statistics.xlsx\"");

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(fileResource);
    }

    @GetMapping("/test_ip_vpn_samba_connection")
    public ResponseEntity<String> testIpVpnSambaConnection() throws IOException {
        String filePath = sambaUrl + "/td_home/MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm";
        SmbFile smbFile = new SmbFile(filePath);
        smbFile.connect();
        InputStream res = new SmbFileInputStream(smbFile);
        return ResponseEntity.ok("Test successful");
    }
}
