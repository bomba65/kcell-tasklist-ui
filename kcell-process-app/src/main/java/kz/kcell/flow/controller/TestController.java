package kz.kcell.flow.controller;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import kz.kcell.flow.vpnportprocess.service.SambaServiceTest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "test.controller.enabled", havingValue = "true")
public class TestController {

    @Value("${ipvpn.samba.url}")
    private String sambaUrl;

    private final SambaServiceTest sambaService;

    @GetMapping("/ip_vpn_connect")
    public ResponseEntity<Resource> getIpVpnConnectFile() {
        try (InputStream in = sambaService.readIpVpnConnect()) {
            File targetFile = new File("/tmp/IPVPN CONNECT.xlsm");
            FileUtils.copyInputStreamToFile(in, targetFile);

            FileSystemResource fileResource = new FileSystemResource(targetFile);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"IPVPN CONNECT.xlsm\"");

            return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel.sheet.macroEnabled.12"))
                .body(fileResource);
        } catch (IOException e) {
            throw new RuntimeException("Failed getIpVpnConnectFile", e);
        }
    }

    @GetMapping("/ip_vpn_statistics")
    public ResponseEntity<Resource> getIpVpnStatisticsFile() {
        try (InputStream in = sambaService.readIpVpnStatistics()) {
            File targetFile = new File("/tmp/IPVPN statistics.xlsx");
            FileUtils.copyInputStreamToFile(in, targetFile);

            FileSystemResource fileResource = new FileSystemResource(targetFile);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"IPVPN statistics.xlsx\"");

            return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileResource);
        } catch (IOException e) {
            throw new RuntimeException("Failed getIpVpnStatisticsFile", e);
        }
    }

    @SneakyThrows
    @PostMapping("/ip_vpn_statistics")
    public ResponseEntity<Resource> uploadIpVpnStatisticsFile(@RequestParam("file") MultipartFile file) {
        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.badRequest().build();
        }

        sambaService.uploadIpVpnStatistics(file.getOriginalFilename(), file.getBytes());
        return ResponseEntity.ok().build();
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
