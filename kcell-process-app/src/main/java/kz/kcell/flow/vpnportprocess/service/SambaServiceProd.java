package kz.kcell.flow.vpnportprocess.service;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Service
@ConditionalOnProperty(name = "ipvpn.connect.file.enabled", havingValue = "true")
public class SambaServiceProd implements SambaService {

    @Value("${ipvpn.samba.url}")
    private String sambaUrl;  
    
    @Value("${ipvpn.samba.domain}")
    private String sambaDomain;   
    
    @Value("${ipvpn.samba.username}")
    private String sambaUsername;
    
    @Value("${ipvpn.samba.password}")
    private String sambaPassword;

    @Override
    public InputStream readIpVpnConnect() {
        try {
            NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(sambaDomain, sambaUsername, sambaPassword);
            CIFSContext ct = SingletonContext.getInstance().withCredentials(auth);

            SmbFile smbFile = new SmbFile(sambaUrl + "MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm", ct);
            return new SmbFileInputStream(smbFile);
        } catch(IOException e) {
            throw new RuntimeException("Error while reading \"IPVPN CONNECT.xlsm\" file from files001 samba server.", e);
        }
    }

    @Override
    public InputStream readIpVpnStatistics() {
        try {
            NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(sambaDomain, sambaUsername, sambaPassword);
            CIFSContext ct = SingletonContext.getInstance().withCredentials(auth);
            
            SmbFile directory = new SmbFile(sambaUrl + "IP VPN Statistics/", ct);
            SmbFile[] files = directory.listFiles();

            Arrays.sort(files, (file1, file2) -> Long.compare(file2.getLastModified(), file1.getLastModified()));
            SmbFile smbFile = files[0];

            return new SmbFileInputStream(smbFile);
        } catch(IOException e) {
            throw new RuntimeException("Error while reading last \"IP VPN Statistics\" file from files001 samba server", e);
        }
    }

    @Override
    public void writeIpVpnConnect(ByteArrayOutputStream byteArrayOutputStream) {
        try {
            NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(sambaDomain, sambaUsername, sambaPassword);
            CIFSContext ct = SingletonContext.getInstance().withCredentials(auth);
            
            SmbFile smbFile = new SmbFile(sambaUrl + "MSC_Data/TRUNK COMMUNICATIONS/IP Core/CORE_NETWORK/IPVPN CONNECT.xlsm", ct);
            SmbFileOutputStream outputStream = new SmbFileOutputStream(smbFile);

            byte[] byteArray = byteArrayOutputStream.toByteArray();
            outputStream.write(byteArray);

            outputStream.close();
        } catch(IOException e) {
            throw new RuntimeException("Error while writing \"IPVPN CONNECT.xlsm\" file to files001 samba server.", e);
        }
    }
}
