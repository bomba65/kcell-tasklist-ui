package kz.kcell.flow.vpnportprocess.service;

import jcifs.smb1.smb1.SmbFile;
import jcifs.smb1.smb1.SmbFileInputStream;
import jcifs.smb1.smb1.SmbFileOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Service
public class SambaService {

    @Value("${ipvpn.samba.url}")
    private String sambaUrl;

    InputStream readIpVpnConnect() {
        try {
            SmbFile smbFile = new SmbFile(sambaUrl + "/td_home/MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm");
            return new SmbFileInputStream(smbFile);
        } catch(IOException e) {
            throw new RuntimeException("Error while reading \"IPVPN CONNECT.xlsm\" file from files001 samba server.", e);
        }
    }

    InputStream readIpVpnStatistics() {
        try {
            SmbFile directory = new SmbFile(sambaUrl + "/IP VPN Statistics/");
            SmbFile[] files = directory.listFiles();

            Arrays.sort(files, (file1, file2) -> Long.compare(file2.getLastModified(), file1.getLastModified()));
            SmbFile smbFile = files[0];

            return new SmbFileInputStream(smbFile);
        } catch(IOException e) {
            throw new RuntimeException("Error while reading last \"IP VPN Statistics\" file from files001 samba server", e);
        }
    }

    void writeIpVpnConnect(ByteArrayOutputStream byteArrayOutputStream ) {
        try {
            SmbFile smbFile = new SmbFile(sambaUrl + "/td_home/MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm");
            SmbFileOutputStream outputStream = new SmbFileOutputStream(smbFile);

            byte[] byteArray = byteArrayOutputStream.toByteArray();
            outputStream.write(byteArray);

            outputStream.close();
        } catch(IOException e) {
            throw new RuntimeException("Error while writing \"IPVPN CONNECT.xlsm\" file to files001 samba server.", e);
        }
    }
}
