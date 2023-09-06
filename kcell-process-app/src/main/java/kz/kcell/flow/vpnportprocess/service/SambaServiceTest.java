package kz.kcell.flow.vpnportprocess.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

@Service
@ConditionalOnProperty(name = "ipvpn.connect.file.enabled", havingValue = "false")
public class SambaServiceTest implements SambaService {

    @Value("${ipvpn.local.path}")
    private String path;

    @Override
    public InputStream readIpVpnConnect() {
        try {
            File file = new File(path + "/td_home/MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm");
            return Files.newInputStream(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error while reading \"IPVPN CONNECT.xlsm\" file from the host machine.", e);
        }
    }

    @Override
    public InputStream readIpVpnStatistics() {
        try {
            File directory = new File(path + "/IP VPN Statistics/");
            File[] files = directory.listFiles();

            Arrays.sort(files, (file1, file2) -> Long.compare(file2.lastModified(), file1.lastModified()));
            File file = files[0];

            return Files.newInputStream(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error while reading the last \"IP VPN Statistics\" file from the host machine.", e);
        }
    }

    @SneakyThrows
    public void uploadIpVpnStatistics(String filename, byte[] bytes) {
        File localFile = new File(path +  "/IP VPN Statistics/" + filename);
        FileOutputStream fileOutputStream = new FileOutputStream(localFile);
        fileOutputStream.write(bytes);
        fileOutputStream.close();
    }

    @Override
    public void writeIpVpnConnect(ByteArrayOutputStream byteArrayOutputStream) {
        try {
            File file = new File(path + "/td_home/MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] byteArray = byteArrayOutputStream.toByteArray();
            outputStream.write(byteArray);

            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error while writing \"IPVPN CONNECT.xlsm\" file to the host machine.", e);
        }
    }

    @Override
    public boolean canWrite() {
        return true;
    }
}
